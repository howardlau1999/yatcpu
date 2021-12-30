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

import bus.{AXI4LiteChannels, AXI4LiteMaster}
import chisel3._
import riscv.Parameters

class CPU extends Module {
  val io = IO(new Bundle {
    val axi4_channels = new AXI4LiteChannels(Parameters.AddrBits, Parameters.DataBits)
    val bus_address = Output(UInt(Parameters.AddrWidth))
    val interrupt_flag = Input(UInt(Parameters.InterruptFlagWidth))
    val stall_flag_bus = Input(Bool())
    val debug_read_address = Input(UInt(Parameters.PhysicalRegisterAddrWidth))
    val debug_read_data = Output(UInt(Parameters.DataWidth))

    val instruction_valid = Input(Bool())
  })

  val ctrl = Module(new Control)
  val regs = Module(new RegisterFile)
  val inst_fetch = Module(new InstructionFetch)
  val if2id = Module(new IF2ID)
  val id = Module(new InstructionDecode)
  val id2ex = Module(new ID2EX)
  val ex = Module(new Execute)
  val clint = Module(new CLINT)
  val csr_regs = Module(new CSR)
  val axi4_master = Module(new AXI4LiteMaster(Parameters.AddrBits, Parameters.DataBits))

  axi4_master.io.channels <> io.axi4_channels

  // The EX module takes precedence over IF (but let the previous fetch finish)
  val ex_granted = RegInit(false.B)
  when(ex_granted) {
    inst_fetch.io.instruction_valid := false.B
    io.bus_address := ex.io.bus_address
    axi4_master.io.bundle.read := ex.io.bus_read
    axi4_master.io.bundle.address := ex.io.bus_address
    axi4_master.io.bundle.write := ex.io.bus_write
    axi4_master.io.bundle.write_data := ex.io.bus_write_data
    axi4_master.io.bundle.write_strobe := ex.io.bus_write_strobe
    when(!ex.io.bus_request) {
      ex_granted := false.B
    }
  }.otherwise {
    // Default to fetch instructions from main memory
    ex_granted := false.B
    axi4_master.io.bundle.read := !axi4_master.io.bundle.busy && !axi4_master.io.bundle.read_valid && !ex.io.bus_request
    axi4_master.io.bundle.address := inst_fetch.io.bus_address
    io.bus_address := inst_fetch.io.bus_address
    axi4_master.io.bundle.write := false.B
    axi4_master.io.bundle.write_data := 0.U
    axi4_master.io.bundle.write_strobe := VecInit(Seq.fill(Parameters.WordSize)(false.B))
  }

  when(ex.io.bus_request) {
    when(!axi4_master.io.bundle.busy && !axi4_master.io.bundle.read_valid) {
      ex_granted := true.B
    }
  }

  inst_fetch.io.instruction_valid := io.instruction_valid && axi4_master.io.bundle.read_valid && !ex_granted
  inst_fetch.io.bus_data := axi4_master.io.bundle.read_data

  ex.io.bus_read_data := axi4_master.io.bundle.read_data
  ex.io.bus_read_valid := axi4_master.io.bundle.read_valid
  ex.io.bus_write_valid := axi4_master.io.bundle.write_valid
  ex.io.bus_busy := axi4_master.io.bundle.busy
  ex.io.bus_granted := ex_granted

  ctrl.io.jump_flag := ex.io.ctrl_jump_flag
  ctrl.io.jump_address := ex.io.ctrl_jump_address
  ctrl.io.stall_flag_if := inst_fetch.io.ctrl_stall_flag
  ctrl.io.stall_flag_ex := ex.io.ctrl_stall_flag
  ctrl.io.stall_flag_id := id.io.ctrl_stall_flag
  ctrl.io.stall_flag_clint := clint.io.ctrl_stall_flag
  ctrl.io.stall_flag_bus := io.stall_flag_bus

  regs.io.write_enable := ex.io.regs_write_enable
  regs.io.write_address := ex.io.regs_write_address
  regs.io.write_data := ex.io.regs_write_data
  regs.io.read_address1 := id.io.regs_reg1_read_address
  regs.io.read_address2 := id.io.regs_reg2_read_address

  regs.io.debug_read_address := io.debug_read_address
  io.debug_read_data := regs.io.debug_read_data

  inst_fetch.io.jump_flag_ctrl := ctrl.io.pc_jump_flag
  inst_fetch.io.jump_address_ctrl := ctrl.io.pc_jump_address
  inst_fetch.io.stall_flag_ctrl := ctrl.io.output_stall_flag

  if2id.io.instruction := inst_fetch.io.id_instruction
  if2id.io.instruction_address := inst_fetch.io.id_instruction_address
  if2id.io.stall_flag := ctrl.io.output_stall_flag
  if2id.io.jump_flag := ctrl.io.pc_jump_flag
  if2id.io.interrupt_flag := io.interrupt_flag

  id.io.reg1_data := regs.io.read_data1
  id.io.reg2_data := regs.io.read_data2
  id.io.instruction := if2id.io.output_instruction
  id.io.instruction_address := if2id.io.output_instruction_address
  id.io.csr_read_data := csr_regs.io.id_reg_data

  id2ex.io.instruction := id.io.ex_instruction
  id2ex.io.instruction_address := id.io.ex_instruction_address
  id2ex.io.csr_read_data := id.io.ex_csr_read_data
  id2ex.io.csr_write_enable := id.io.ex_csr_write_enable
  id2ex.io.csr_write_address := id.io.ex_csr_write_address
  id2ex.io.op1 := id.io.ex_op1
  id2ex.io.op2 := id.io.ex_op2
  id2ex.io.op1_jump := id.io.ex_op1_jump
  id2ex.io.op2_jump := id.io.ex_op2_jump
  id2ex.io.reg1_data := id.io.ex_reg1_data
  id2ex.io.reg2_data := id.io.ex_reg2_data
  id2ex.io.regs_write_enable := id.io.ex_reg_write_enable
  id2ex.io.regs_write_address := id.io.ex_reg_write_address
  id2ex.io.stall_flag := ctrl.io.output_stall_flag
  id2ex.io.jump_flag := ctrl.io.pc_jump_flag

  ex.io.instruction := id2ex.io.output_instruction
  ex.io.instruction_address := id2ex.io.output_instruction_address
  ex.io.csr_reg_data_id := id2ex.io.output_csr_read_data
  ex.io.csr_reg_write_enable_id := id2ex.io.output_csr_write_enable
  ex.io.csr_reg_write_address_id := id2ex.io.output_csr_write_address
  ex.io.op1 := id2ex.io.output_op1
  ex.io.op2 := id2ex.io.output_op2
  ex.io.op1_jump := id2ex.io.output_op1_jump
  ex.io.op2_jump := id2ex.io.output_op2_jump
  ex.io.reg1_data := id2ex.io.output_reg1_data
  ex.io.reg2_data := id2ex.io.output_reg2_data
  ex.io.regs_write_enable_id := id2ex.io.output_regs_write_enable
  ex.io.regs_write_address_id := id2ex.io.output_regs_write_address
  ex.io.interrupt_assert := clint.io.ex_interrupt_assert
  ex.io.interrupt_handler_address := clint.io.ex_interrupt_handler_address

  clint.io.instruction := id.io.ex_instruction
  clint.io.instruction_address_id := id.io.instruction_address
  clint.io.jump_flag := ex.io.ctrl_jump_flag
  clint.io.jump_address := ex.io.ctrl_jump_address
  clint.io.csr_mepc := csr_regs.io.clint_csr_mepc
  clint.io.csr_mtvec := csr_regs.io.clint_csr_mtvec
  clint.io.csr_mstatus := csr_regs.io.clint_csr_mstatus
  clint.io.interrupt_enable := csr_regs.io.interrupt_enable
  clint.io.interrupt_flag := if2id.io.output_interrupt_flag

  csr_regs.io.reg_write_enable_ex := ex.io.csr_reg_write_enable
  csr_regs.io.reg_write_address_ex := ex.io.csr_reg_write_address
  csr_regs.io.reg_write_data_ex := ex.io.csr_reg_write_data
  csr_regs.io.reg_read_address_id := id.io.csr_read_address
  csr_regs.io.reg_write_enable_clint := clint.io.csr_reg_write_enable
  csr_regs.io.reg_write_address_clint := clint.io.csr_reg_write_address
  csr_regs.io.reg_write_data_clint := clint.io.csr_reg_write_data
  csr_regs.io.reg_read_address_clint := 0.U
}
