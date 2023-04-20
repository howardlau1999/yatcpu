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

package peripheral

import bus.{AXI4LiteChannels, AXI4LiteSlave}
import chisel3._
import chisel3.util._
import riscv.Parameters

class Timer extends Module {
  val io = IO(new Bundle {
    val channels = Flipped(new AXI4LiteChannels(8, Parameters.DataBits))
    val signal_interrupt = Output(Bool())

    val debug_limit = Output(UInt(Parameters.DataWidth))
    val debug_enabled = Output(Bool())
  })
  val slave = Module(new AXI4LiteSlave(8, Parameters.DataBits))
  slave.io.channels <> io.channels

  val count = RegInit(0.U(32.W))
  val limit = RegInit(100000000.U(32.W))
  io.debug_limit := limit
  val enabled = RegInit(true.B)
  io.debug_enabled := enabled

  slave.io.bundle.read_data := 0.U
  slave.io.bundle.read_valid := true.B
  when(slave.io.bundle.read) {
    slave.io.bundle.read_data := MuxLookup(slave.io.bundle.address, 0.U)(
      IndexedSeq(
        0x4.U -> limit,
        0x8.U -> enabled.asUInt,
      )
    )
  }
  when(slave.io.bundle.write) {
    when(slave.io.bundle.address === 0x4.U) {
      limit := slave.io.bundle.write_data
      count := 0.U
    }.elsewhen(slave.io.bundle.address === 0x8.U) {
      enabled := slave.io.bundle.write_data =/= 0.U
    }
  }

  io.signal_interrupt := enabled && (count >= (limit - 10.U))

  when(count >= limit) {
    count := 0.U
  }.otherwise {
    count := count + 1.U
  }
}
