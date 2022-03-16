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
  val Idle, Missed, Evicting = Value
}

class Cache(cacheLineBytes: Int, cacheLineCount: Int, associativity: Int) extends Module {
  val io = IO(new Bundle {
    val cpu_side = Flipped(new BusBundle)
    val memory_side = new BusBundle
  })

  val setCount = cacheLineCount / associativity
  val setIndexBits = log2Up(setCount)
  val blockOffsetBits = log2Up(cacheLineBytes)
  val tagBits = Parameters.AddrBits - setIndexBits - blockOffsetBits
  val cacheLines = SyncReadMem(cacheLineCount, UInt((2 + tagBits + cacheLineBytes * Parameters.ByteBits).W))
  val validBitIndex = blockOffsetBits + tagBits
  val dirtyBitIndex = blockOffsetBits + tagBits + 1
  val setIndex = io.cpu_side.address(blockOffsetBits + setIndexBits - 1, blockOffsetBits)
  // Align to word (4 bytes in RV32)
  val blockOffset = (io.cpu_side.address(blockOffsetBits - 1, log2Up(Parameters.WordSize)) << log2Up(Parameters.WordSize)).asUInt

  val cacheLine = cacheLines.read(setIndex)
  val validBit = cacheLine(validBitIndex)
  val dirtyBit = cacheLine(dirtyBitIndex)

  // Cache FSM
  val state = RegInit(CacheState.Idle)
  // How many bytes have we transferred?
  val fetched = RegInit(0.U)
  // Temporary space for the fetching cache line
  val fetchingCacheLine = RegInit(0.U((cacheLineBytes * Parameters.ByteBits).W))

  when(io.cpu_side.write) {

  } otherwise {
    when(validBit) {
      io.cpu_side.read_valid := true.B
      val read_mask = Fill(Parameters.WordSize * Parameters.ByteBits, 1.U(1.W)) << (blockOffset << log2Up(Parameters.ByteBits))
      io.cpu_side.read_data := (cacheLine & read_mask) >> (blockOffset << log2Up(Parameters.ByteBits))
    } otherwise {
      // Read Miss
      state := CacheState.Missed
      io.memory_side.read := true.B
      io.memory_side.address := io.cpu_side.address + fetched
      when(io.memory_side.read_valid) {
        when(fetched < cacheLineBytes.U) {
          fetched := fetched + Parameters.WordSize.U
        }
        fetchingCacheLine := fetchingCacheLine | (io.memory_side.read_data << (fetched << log2Up(Parameters.ByteBits)))
      }
    }
  }
}
