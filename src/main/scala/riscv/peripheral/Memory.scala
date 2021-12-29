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

package riscv.peripheral

import chisel3._
import chisel3.util._
import chisel3.experimental._
import chisel3.util.experimental._
import firrtl.annotations.MemorySynthInit
import riscv.Parameters
import riscv.bus.{AXI4LiteChannels, AXI4LiteSlave}
import riscv.core.ProgramCounter

import java.io.FileWriter
import java.nio.file.Paths
import java.nio.{ByteBuffer, ByteOrder}

// Main memory
class Memory(capacity: Int) extends Module {
  val io = IO(new Bundle {
    val channels = Flipped(new AXI4LiteChannels(Parameters.AddrBits, Parameters.DataBits))

    val debug_read_address = Input(UInt(Parameters.AddrWidth))
    val char_read_address = Input(UInt(Parameters.AddrWidth))
    val instruction_read_address = Input(UInt(Parameters.AddrWidth))

    val debug_read_data = Output(UInt(Parameters.DataWidth))
    val char_read_data = Output(UInt(Parameters.DataWidth))
    val instruction_read_data = Output(UInt(Parameters.DataWidth))
  })

  val mem = SyncReadMem(capacity, UInt(Parameters.DataWidth))
  val slave = Module(new AXI4LiteSlave(Parameters.AddrBits, Parameters.DataBits))

  slave.io.channels <> io.channels

  when(slave.io.bundle.write) {
    mem.write(
      (slave.io.bundle.address >> log2Up(Parameters.WordSize)).asUInt(),
      slave.io.bundle.write_data
    )
  }

  slave.io.bundle.read_data := mem.read(
    (slave.io.bundle.address >> log2Up(Parameters.WordSize)).asUInt(),
    slave.io.bundle.read,
  ).asUInt()

  io.debug_read_data := mem.read((io.debug_read_address >> log2Up(Parameters.WordSize)).asUInt(), true.B)
    .asUInt()
  io.char_read_data := mem.read((io.char_read_address >> log2Up(Parameters.WordSize)).asUInt(), true.B)
    .asUInt()
  io.instruction_read_data := mem.read((io.instruction_read_address >> log2Up(Parameters.WordSize)).asUInt(), true.B)
    .asUInt()
}
