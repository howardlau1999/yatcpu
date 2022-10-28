// Copyright 2022 Canbin Huang
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

class EX2MEM extends Module {
  val io = IO(new Bundle() {
    val stall_flag = Input(Bool())
    val flush_enable = Input(Bool())
    val regs_write_enable = Input(Bool())
    val regs_write_source = Input(UInt(2.W))
    val regs_write_address = Input(UInt(Parameters.AddrWidth))
    val instruction_address = Input(UInt(Parameters.AddrWidth))
    val instruction = Input(UInt(Parameters.DataWidth))
    val reg1_data = Input(UInt(Parameters.DataWidth))
    val reg2_data = Input(UInt(Parameters.DataWidth))
    val memory_read_enable = Input(Bool())
    val memory_write_enable = Input(Bool())
    val alu_result = Input(UInt(Parameters.DataWidth))
    val csr_read_data = Input(UInt(Parameters.DataWidth))

    val output_regs_write_enable = Output(Bool())
    val output_regs_write_source = Output(UInt(2.W))
    val output_regs_write_address = Output(UInt(Parameters.AddrWidth))
    val output_instruction_address = Output(UInt(Parameters.AddrWidth))
    val output_instruction = Output(UInt(Parameters.DataWidth))
    val output_reg1_data = Output(UInt(Parameters.DataWidth))
    val output_reg2_data = Output(UInt(Parameters.DataWidth))
    val output_memory_read_enable = Output(Bool())
    val output_memory_write_enable = Output(Bool())
    val output_alu_result = Output(UInt(Parameters.DataWidth))
    val output_csr_read_data = Output(UInt(Parameters.DataWidth))
  })
  val write_enable = !io.stall_flag

  val regs_write_enable = Module(new PipelineRegister(1))
  regs_write_enable.io.in := io.regs_write_enable
  regs_write_enable.io.write_enable := write_enable
  regs_write_enable.io.flush_enable := io.flush_enable
  io.output_regs_write_enable := regs_write_enable.io.out

  val regs_write_source = Module(new PipelineRegister(2))
  regs_write_source.io.in := io.regs_write_source
  regs_write_source.io.write_enable := write_enable
  regs_write_source.io.flush_enable := io.flush_enable
  io.output_regs_write_source := regs_write_source.io.out

  val regs_write_address = Module(new PipelineRegister(Parameters.PhysicalRegisterAddrBits))
  regs_write_address.io.in := io.regs_write_address
  regs_write_address.io.write_enable := write_enable
  regs_write_address.io.flush_enable := io.flush_enable
  io.output_regs_write_address := regs_write_address.io.out

  val instruction_address = Module(new PipelineRegister(Parameters.AddrBits))
  instruction_address.io.in := io.instruction_address
  instruction_address.io.write_enable := write_enable
  instruction_address.io.flush_enable := io.flush_enable
  io.output_instruction_address := instruction_address.io.out

  val instruction = Module(new PipelineRegister(Parameters.InstructionBits))
  instruction.io.in := io.instruction
  instruction.io.write_enable := write_enable
  instruction.io.flush_enable := io.flush_enable
  io.output_instruction := instruction.io.out

  val reg1_data = Module(new PipelineRegister())
  reg1_data.io.in := io.reg1_data
  reg1_data.io.write_enable := write_enable
  reg1_data.io.flush_enable := io.flush_enable
  io.output_reg1_data := reg1_data.io.out

  val reg2_data = Module(new PipelineRegister())
  reg2_data.io.in := io.reg2_data
  reg2_data.io.write_enable := write_enable
  reg2_data.io.flush_enable := io.flush_enable
  io.output_reg2_data := reg2_data.io.out

  val alu_result = Module(new PipelineRegister())
  alu_result.io.in := io.alu_result
  alu_result.io.write_enable := write_enable
  alu_result.io.flush_enable := io.flush_enable
  io.output_alu_result := alu_result.io.out

  val memory_read_enable = Module(new PipelineRegister(1))
  memory_read_enable.io.in := io.memory_read_enable
  memory_read_enable.io.write_enable := write_enable
  memory_read_enable.io.flush_enable := io.flush_enable
  io.output_memory_read_enable := memory_read_enable.io.out

  val memory_write_enable = Module(new PipelineRegister(1))
  memory_write_enable.io.in := io.memory_write_enable
  memory_write_enable.io.write_enable := write_enable
  memory_write_enable.io.flush_enable := io.flush_enable
  io.output_memory_write_enable := memory_write_enable.io.out

  val csr_read_data = Module(new PipelineRegister())
  csr_read_data.io.in := io.csr_read_data
  csr_read_data.io.write_enable := write_enable
  csr_read_data.io.flush_enable := io.flush_enable
  io.output_csr_read_data := csr_read_data.io.out
}
