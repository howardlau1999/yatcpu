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

package riscv

import chisel3._
import chisel3.util.{MuxCase}
import chisel3.stage.ChiselStage

object ProgramCounter {
  val EntryAddress = 0x1000.U(32.W)
}

class ProgramCounter extends Module {
  val io = IO(new Bundle {
    val jump_enable = Input(Bool())
    val jump_address = Input(UInt(32.W))
    val hold_flag = Input(UInt(3.W))

    val pc = Output(UInt(32.W))
  })

  val pc = RegInit(ProgramCounter.EntryAddress)

  pc := MuxCase(
    pc + 4.U,
    Array(
      io.jump_enable -> io.jump_address,
      (io.hold_flag >= HoldStates.PC) -> pc
    )
  )

  io.pc := pc
}