// Copyright 2021 Howard Lau
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package riscv.core.threestage

import chisel3._
import chisel3.experimental.ChiselEnum
import chisel3.util._
import riscv.Parameters
import riscv.core.BusBundle

object CacheState extends ChiselEnum {
  val Idle, Compare, Compared, Missed, Evicting = Value
}

class Cache(cacheline_bytes: Int, cacheline_count: Int, associativity: Int) extends Module {
  val io = IO(new Bundle {
    val cpu_side = Flipped(new BusBundle)
    val memory_side = new BusBundle
  })

  // Always granted :)
  io.cpu_side.granted := true.B

  // Cache FSM
  val state = RegInit(CacheState.Idle)

  val set_count = cacheline_count / associativity
  val set_index_bits = log2Up(set_count)
  val block_offset_bits = log2Up(cacheline_bytes)
  val tag_bits = Parameters.AddrBits - set_index_bits - block_offset_bits
  val cacheline_bits = cacheline_bytes * Parameters.ByteBits
  val cachelines = SyncReadMem(cacheline_count, Vec(associativity, UInt((2 + tag_bits + cacheline_bits).W)))
  val valid_bit_index = block_offset_bits + tag_bits
  val dirty_bit_index = block_offset_bits + tag_bits + 1
  val set_index = io.cpu_side.address(block_offset_bits + set_index_bits - 1, block_offset_bits)
  // Align to word (4 bytes in RV32)
  val block_offset = (io.cpu_side.address(block_offset_bits - 1, log2Up(Parameters.WordSize)) << log2Up(Parameters.WordSize)).asUInt
  // Tag for the address
  val tag = io.cpu_side.address(Parameters.AddrBits - 1, block_offset_bits + set_index_bits)

  val read_enable = state === CacheState.Idle && (io.cpu_side.read || io.cpu_side.write)
  val cachelines_in_set = cachelines.read(set_index)
  val hit = RegInit(false.B)
  val hit_index = RegInit(0.U)
  val hit_cacheline = RegInit(0.U)

  when(io.cpu_side.read || io.cpu_side.write) {
    when(state === CacheState.Idle) {
      state := CacheState.Compare
    }.elsewhen(state === CacheState.Compare) {
      for (i <- 0 until  associativity) {
        val cacheline_tag = cachelines_in_set(i)(cacheline_bits + tag_bits - 1, cacheline_bits)
        val cacheline_valid_bit = cachelines_in_set(i)(valid_bit_index)
        when(tag === cacheline_tag && cacheline_valid_bit) {
          hit := true.B
          hit_index := i.U
          hit_cacheline := cachelines_in_set(i)
        }
      }
      state := CacheState.Compared
    }
  }

  // How many bytes have we transferred?
  val fetched = RegInit(0.U)
  // Align to cacheline
  val fetching_start = (io.cpu_side.address(Parameters.AddrBits - 1, block_offset_bits) << block_offset_bits).asUInt
  // Temporary space for the fetching cache line
  val fetching_cacheline = RegInit(0.U((cacheline_bytes * Parameters.ByteBits).W))

  when(io.cpu_side.write) {

  } otherwise {
    when(hit) {
      // Read hit
      io.cpu_side.read_valid := true.B
      val read_mask = Fill(Parameters.WordSize * Parameters.ByteBits, 1.U(1.W)) << (block_offset << log2Up(Parameters.ByteBits))
      io.cpu_side.read_data := (hit_cacheline & read_mask) >> (block_offset << log2Up(Parameters.ByteBits))
    } otherwise {
      // Read Miss
      when(state === CacheState.Idle) {
        fetched := 0.U
        io.memory_side.request := true.B
        io.memory_side.read := true.B
        io.memory_side.address := fetching_start
        when(io.memory_side.granted) {
          state := CacheState.Missed
        }
      }.elsewhen(state === CacheState.Missed) {
        io.memory_side.request := true.B
        when(fetched < cacheline_bytes.U) {
          when(io.memory_side.read_valid) {
            fetched := fetched + Parameters.WordSize.U
            fetching_cacheline := fetching_cacheline | (io.memory_side.read_data << (fetched << log2Up(Parameters.ByteBits)))
          }
        }.otherwise {

        }
      }
    }
  }
}
