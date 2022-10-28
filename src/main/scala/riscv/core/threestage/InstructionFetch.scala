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
import chisel3.util.MuxCase
import riscv.Parameters

object ProgramCounter {
  val EntryAddress = Parameters.EntryAddress
}

class InstructionFetch extends Module {
  val io = IO(new Bundle {
    val stall_flag_ctrl = Input(Bool())
    val jump_flag_ctrl = Input(Bool())
    val jump_address_ctrl = Input(UInt(Parameters.AddrWidth))
    val instruction_valid = Input(Bool())

    val bus_request = Output(Bool())
    val bus_address = Output(UInt(Parameters.AddrWidth))
    val bus_data = Input(UInt(Parameters.InstructionWidth))
    val bus_read = Output(Bool())

    val ctrl_stall_flag = Output(Bool())
    val id_instruction_address = Output(UInt(Parameters.AddrWidth))
    val id_instruction = Output(UInt(Parameters.InstructionWidth))
  })
  val instruction_address = RegInit(ProgramCounter.EntryAddress)
  val pending_jump = RegInit(false.B)
  val pc = RegInit(ProgramCounter.EntryAddress)
  io.bus_read := true.B
  io.bus_request := true.B

  pc := MuxCase(
    pc + 4.U,
    IndexedSeq(
      io.jump_flag_ctrl -> io.jump_address_ctrl,
      (io.stall_flag_ctrl >= StallStates.PC) -> pc
    )
  )
  when(!io.instruction_valid) {
    when(io.jump_flag_ctrl) {
      pending_jump := true.B
    }
  }
  when(io.instruction_valid) {
    when(pending_jump) {
      pending_jump := false.B
    }
  }
  io.id_instruction := Mux(io.instruction_valid && !io.jump_flag_ctrl && !pending_jump, io.bus_data,
    InstructionsNop.nop)
  io.ctrl_stall_flag := !io.instruction_valid || pending_jump
  io.id_instruction_address := pc
  io.bus_address := pc
}
