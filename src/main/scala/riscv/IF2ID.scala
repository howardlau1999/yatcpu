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

class IF2ID extends Module {
  val io = IO(new Bundle {
    val instruction = Input(UInt(32.W))
    val instruction_address = Input(UInt(32.W))
    val hold_flag = Input(UInt(3.W))
    val interrupt_flag = Input(UInt(32.W))

    val output_instruction = Output(UInt(32.W))
    val output_instruction_address = Output(UInt(32.W))
    val output_interrupt_flag = Output(UInt(32.W))
  })

  val hold_enable = io.hold_flag >= HoldStates.IF

  val instruction = Module(new PipelineRegister(defaultValue = 0x00000013.U))
  instruction.io.in := io.instruction
  instruction.io.hold_enable := hold_enable
  io.output_instruction := instruction.io.out

  val instruction_address = Module(new PipelineRegister(defaultValue = ProgramCounter.EntryAddress))
  instruction_address.io.in := io.instruction_address
  instruction_address.io.hold_enable := hold_enable
  io.output_instruction_address := instruction_address.io.out

  val interrupt_flag = Module(new PipelineRegister())
  interrupt_flag.io.in := io.interrupt_flag
  interrupt_flag.io.hold_enable := hold_enable
  io.output_interrupt_flag := interrupt_flag.io.out
}
