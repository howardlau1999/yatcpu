package riscv

import chisel3._
import chisel3.util._

class CPU extends Module {
  val io = IO(new Bundle {
    val interrupt_flag = Input(UInt(32.W))
    val debug_read_address = Input(UInt(32.W))
    val debug_read_data = Output(UInt(32.W))

    val instruction_read_data = Input(UInt(32.W))
    val instruction_read_address = Output(UInt(32.W))

    val mem_write_enable = Output(UInt(32.W))
    val mem_write_address = Output(UInt(32.W))
    val mem_write_data = Output(UInt(32.W))
    val mem_read_data = Input(UInt(32.W))
    val mem_read_address = Output(UInt(32.W))
  })

  val pc = Module(new ProgramCounter)
  val ctrl = Module(new Control)
  val regs = Module(new RegisterFile)
  val inst_fetch = Module(new InstructionFetch)
  val if2id = Module(new IF2ID)
  val id = Module(new InstructionDecode)
  val id2ex = Module(new ID2EX)
  val ex = Module(new Execute)
  val clint = Module(new CLINT)
  val csr_regs = Module(new CSR)

  pc.io.hold_flag := ctrl.io.output_hold_flag
  pc.io.jump_enable := ctrl.io.pc_jump_flag
  pc.io.jump_address := ctrl.io.pc_jump_address

  ctrl.io.jump_flag := ex.io.ctrl_jump_flag
  ctrl.io.jump_address := ex.io.ctrl_jump_address
  ctrl.io.hold_flag_ex := ex.io.ctrl_hold_flag
  ctrl.io.hold_flag_id := id.io.ctrl_hold_flag
  ctrl.io.hold_flag_clint := clint.io.ctrl_hold_flag

  regs.io.write_enable := ex.io.regs_write_enable
  regs.io.write_address := ex.io.regs_write_address
  regs.io.write_data := ex.io.regs_write_data
  regs.io.read_address1 := id.io.regs_reg1_read_address
  regs.io.read_address2 := id.io.regs_reg2_read_address

  regs.io.debug_read_address := io.debug_read_address
  io.debug_read_data := regs.io.debug_read_data

  inst_fetch.io.pc_pc := pc.io.pc
  io.instruction_read_address := inst_fetch.io.mem_instruction_address
  inst_fetch.io.instruction_mem := io.instruction_read_data
  inst_fetch.io.hold_flag_ctrl := ctrl.io.output_hold_flag
  inst_fetch.io.jump_flag_ctrl := ctrl.io.pc_jump_flag
  inst_fetch.io.jump_address_ctrl := ctrl.io.pc_jump_address

  if2id.io.instruction := inst_fetch.io.id_instruction
  if2id.io.instruction_address := inst_fetch.io.id_instruction_address
  if2id.io.hold_flag := ctrl.io.output_hold_flag
  if2id.io.interrupt_flag := io.interrupt_flag

  id.io.reg1 := regs.io.read_data1
  id.io.reg2 := regs.io.read_data2
  id.io.instruction := if2id.io.output_instruction
  id.io.instruction_address := if2id.io.output_instruction_address
  id.io.jump_flag := ex.io.ctrl_jump_flag
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
  id2ex.io.reg1 := id.io.ex_reg1
  id2ex.io.reg2 := id.io.ex_reg2
  id2ex.io.write_enable := id.io.ex_reg_write_enable
  id2ex.io.write_address := id.io.ex_reg_write_address
  id2ex.io.hold_flag := ctrl.io.output_hold_flag

  ex.io.instruction := id2ex.io.output_instruction
  ex.io.instruction_address := id2ex.io.output_instruction_address
  ex.io.csr_reg_data_id := id2ex.io.output_csr_read_data
  ex.io.csr_reg_write_enable_id := id2ex.io.output_csr_write_enable
  ex.io.csr_reg_write_address_id := id2ex.io.output_csr_write_address
  ex.io.op1 := id2ex.io.output_op1
  ex.io.op2 := id2ex.io.output_op2
  ex.io.op1_jump := id2ex.io.output_op1_jump
  ex.io.op2_jump := id2ex.io.output_op2_jump
  ex.io.reg1 := id2ex.io.output_reg1
  ex.io.reg2 := id2ex.io.output_reg2
  ex.io.write_enable := id2ex.io.output_write_enable
  ex.io.write_address := id2ex.io.output_write_address
  ex.io.data := io.mem_read_data
  ex.io.interrupt_assert := clint.io.ex_interrupt_assert
  ex.io.interrupt_handler_address := clint.io.ex_interrupt_handler_address

  io.mem_write_enable := ex.io.mem_write_enable
  io.mem_write_address := ex.io.mem_write_address
  io.mem_write_data := ex.io.mem_write_data
  io.mem_read_address := id.io.ex_mem_read_address


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
