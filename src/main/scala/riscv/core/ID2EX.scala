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

package riscv.core

import chisel3._
import riscv.Parameters

class ID2EX extends Module {
  val io = IO(new Bundle {
    val instruction = Input(UInt(Parameters.DataWidth))
    val instruction_address = Input(UInt(Parameters.AddrWidth))
    val regs_write_enable = Input(Bool())
    val regs_write_address = Input(UInt(Parameters.PhysicalRegisterAddrWidth))
    val reg1_data = Input(UInt(Parameters.DataWidth))
    val reg2_data = Input(UInt(Parameters.DataWidth))
    val op1 = Input(UInt(Parameters.DataWidth))
    val op2 = Input(UInt(Parameters.DataWidth))
    val op1_jump = Input(UInt(Parameters.DataWidth))
    val op2_jump = Input(UInt(Parameters.DataWidth))
    val csr_write_enable = Input(Bool())
    val csr_write_address = Input(UInt(Parameters.CSRRegisterAddrWidth))
    val csr_read_data = Input(UInt(Parameters.DataWidth))
    val stall_flag = Input(UInt(Parameters.StallStateWidth))
    val jump_flag = Input(Bool())

    val output_instruction = Output(UInt(Parameters.DataWidth))
    val output_instruction_address = Output(UInt(Parameters.AddrWidth))
    val output_regs_write_enable = Output(Bool())
    val output_regs_write_address = Output(UInt(Parameters.PhysicalRegisterAddrWidth))
    val output_reg1_data = Output(UInt(Parameters.DataWidth))
    val output_reg2_data = Output(UInt(Parameters.DataWidth))
    val output_op1 = Output(UInt(Parameters.DataWidth))
    val output_op2 = Output(UInt(Parameters.DataWidth))
    val output_op1_jump = Output(UInt(Parameters.DataWidth))
    val output_op2_jump = Output(UInt(Parameters.DataWidth))
    val output_csr_write_enable = Output(Bool())
    val output_csr_write_address = Output(UInt(Parameters.CSRRegisterAddrWidth))
    val output_csr_read_data = Output(UInt(Parameters.DataWidth))
  })
  val write_enable = io.stall_flag < StallStates.ID
  val flush_enable = io.jump_flag

  val instruction = Module(new PipelineRegister(defaultValue = InstructionsNop.nop))
  instruction.io.in := io.instruction
  instruction.io.write_enable := write_enable
  instruction.io.flush_enable := flush_enable
  io.output_instruction := instruction.io.out

  val instruction_address = Module(new PipelineRegister(defaultValue = ProgramCounter.EntryAddress))
  instruction_address.io.in := io.instruction_address
  instruction_address.io.write_enable := write_enable
  instruction_address.io.flush_enable := flush_enable
  io.output_instruction_address := instruction_address.io.out

  val regs_write_enable = Module(new PipelineRegister(1))
  regs_write_enable.io.in := io.regs_write_enable
  regs_write_enable.io.write_enable := write_enable
  regs_write_enable.io.flush_enable := flush_enable
  io.output_regs_write_enable := regs_write_enable.io.out

  val regs_write_address = Module(new PipelineRegister(Parameters.PhysicalRegisterAddrBits))
  regs_write_address.io.in := io.regs_write_address
  regs_write_address.io.write_enable := write_enable
  regs_write_address.io.flush_enable := flush_enable
  io.output_regs_write_address := regs_write_address.io.out

  val reg1_data = Module(new PipelineRegister())
  reg1_data.io.in := io.reg1_data
  reg1_data.io.write_enable := write_enable
  reg1_data.io.flush_enable := flush_enable
  io.output_reg1_data := reg1_data.io.out

  val reg2_data = Module(new PipelineRegister())
  reg2_data.io.in := io.reg2_data
  reg2_data.io.write_enable := write_enable
  reg2_data.io.flush_enable := flush_enable
  io.output_reg2_data := reg2_data.io.out

  val op1 = Module(new PipelineRegister())
  op1.io.in := io.op1
  op1.io.write_enable := write_enable
  op1.io.flush_enable := flush_enable
  io.output_op1 := op1.io.out

  val op2 = Module(new PipelineRegister())
  op2.io.in := io.op2
  op2.io.write_enable := write_enable
  op2.io.flush_enable := flush_enable
  io.output_op2 := op2.io.out

  val op1_jump = Module(new PipelineRegister())
  op1_jump.io.in := io.op1_jump
  op1_jump.io.write_enable := write_enable
  op1_jump.io.flush_enable := flush_enable
  io.output_op1_jump := op1_jump.io.out

  val op2_jump = Module(new PipelineRegister())
  op2_jump.io.in := io.op2_jump
  op2_jump.io.write_enable := write_enable
  op2_jump.io.flush_enable := flush_enable
  io.output_op2_jump := op2_jump.io.out

  val csr_write_enable = Module(new PipelineRegister())
  csr_write_enable.io.in := io.csr_write_enable
  csr_write_enable.io.write_enable := write_enable
  csr_write_enable.io.flush_enable := flush_enable
  io.output_csr_write_enable := csr_write_enable.io.out

  val csr_write_address = Module(new PipelineRegister())
  csr_write_address.io.in := io.csr_write_address
  csr_write_address.io.write_enable := write_enable
  csr_write_address.io.flush_enable := flush_enable
  io.output_csr_write_address := csr_write_address.io.out

  val csr_read_data = Module(new PipelineRegister())
  csr_read_data.io.in := io.csr_read_data
  csr_read_data.io.write_enable := write_enable
  csr_read_data.io.flush_enable := flush_enable
  io.output_csr_read_data := csr_read_data.io.out
}
