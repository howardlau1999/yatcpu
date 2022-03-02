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

package riscv.core.fivestage

import chisel3._
import riscv.Parameters

class IF2ID extends Module {
  val io = IO(new Bundle {
    val stall_flag = Input(Bool())
    val flush_enable = Input(Bool())
    val instruction = Input(UInt(Parameters.InstructionWidth))
    val instruction_address = Input(UInt(Parameters.AddrWidth))
    val interrupt_flag = Input(UInt(Parameters.InterruptFlagWidth))

    val output_instruction = Output(UInt(Parameters.DataWidth))
    val output_instruction_address = Output(UInt(Parameters.AddrWidth))
    val output_interrupt_flag = Output(UInt(Parameters.InterruptFlagWidth))
  })

  val write_enable = !io.stall_flag

  val instruction = Module(new PipelineRegister(defaultValue = InstructionsNop.nop))
  instruction.io.in := io.instruction
  instruction.io.write_enable := write_enable
  instruction.io.flush_enable := io.flush_enable
  io.output_instruction := instruction.io.out

  val instruction_address = Module(new PipelineRegister(defaultValue = ProgramCounter.EntryAddress))
  instruction_address.io.in := io.instruction_address
  instruction_address.io.write_enable := write_enable
  instruction_address.io.flush_enable := io.flush_enable
  io.output_instruction_address := instruction_address.io.out

  val interrupt_flag = Module(new PipelineRegister(Parameters.InterruptFlagBits))
  interrupt_flag.io.in := io.interrupt_flag
  interrupt_flag.io.write_enable := write_enable
  interrupt_flag.io.flush_enable := io.flush_enable
  io.output_interrupt_flag := interrupt_flag.io.out
}
