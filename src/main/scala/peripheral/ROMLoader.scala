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

import bus.{AXI4LiteChannels, AXI4LiteMaster}
import chisel3._
import riscv.Parameters

class ROMLoader(capacity: Int) extends Module {
  val io = IO(new Bundle {
    val channels = new AXI4LiteChannels(Parameters.AddrBits, Parameters.InstructionBits)

    val rom_address = Output(UInt(Parameters.AddrWidth))
    val rom_data = Input(UInt(Parameters.InstructionWidth))

    val load_start = Input(Bool())
    val load_address = Input(UInt(Parameters.AddrWidth))
    val load_finished = Output(Bool())
  })
  val master = Module(new AXI4LiteMaster(Parameters.AddrBits, Parameters.InstructionBits))
  master.io.channels <> io.channels

  val address = RegInit(0.U(32.W))
  val valid = RegInit(false.B)
  val loading = RegInit(false.B)

  master.io.bundle.read := false.B
  io.load_finished := false.B

  when(io.load_start) {
    valid := false.B
    loading := true.B
    address := 0.U
  }

  master.io.bundle.write := false.B
  master.io.bundle.write_data := 0.U
  master.io.bundle.write_strobe := VecInit(Seq.fill(Parameters.WordSize)(false.B))
  master.io.bundle.address := 0.U

  when(!loading && !master.io.bundle.busy && address >= (capacity - 1).U) {
    io.load_finished := true.B
  }
  when(loading) {
    valid := true.B
    when(!master.io.bundle.busy && !master.io.bundle.write_valid) {
      when(valid) {
        master.io.bundle.write := true.B
        master.io.bundle.write_data := io.rom_data
        master.io.bundle.write_strobe := VecInit(Seq.fill(Parameters.WordSize)(true.B))
        master.io.bundle.address := (address << 2.U).asUInt + io.load_address
      }
    }
    when(master.io.bundle.write_valid) {
      when(address >= (capacity - 1).U) {
        loading := false.B
      }.otherwise {
        loading := true.B
        address := address + 1.U
        valid := false.B
      }
    }.otherwise {
      address := address
    }
  }
  io.rom_address := address
}
