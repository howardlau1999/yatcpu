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
import chisel3.util._

class InstructionFetch extends Module {
  val io = IO(new Bundle {
    val pc_pc = Input(UInt(Parameters.AddrWidth))
    val stall_flag_ctrl = Input(UInt(Parameters.StallStateWidth))
    val jump_flag_ctrl = Input(Bool())
    val jump_address_ctrl = Input(UInt(Parameters.AddrWidth))
    val instruction_mem = Input(UInt(Parameters.DataWidth))

    val mem_instruction_address = Output(UInt(Parameters.AddrWidth))
    val id_instruction_address = Output(UInt(Parameters.AddrWidth))
    val id_instruction = Output(UInt(Parameters.DataWidth))
  })
  val instruction_address = RegInit(ProgramCounter.EntryAddress)
  val instruction_valid = RegInit(false.B)
  instruction_valid := true.B

  io.id_instruction := Mux(instruction_valid, io.instruction_mem, InstructionsNop.nop)
  io.id_instruction_address := instruction_address
  when(io.jump_flag_ctrl) {
    io.mem_instruction_address := io.jump_address_ctrl
    instruction_address := io.jump_address_ctrl
    instruction_valid := false.B
  }.elsewhen(io.stall_flag_ctrl >= StallStates.IF) {
    io.mem_instruction_address := instruction_address
  }.otherwise {
    instruction_address := io.pc_pc
    io.mem_instruction_address := io.pc_pc
  }
}
