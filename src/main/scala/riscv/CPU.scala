package riscv

import chisel3._
import chisel3.util._

class CPU extends Module {
  val io = IO(new Bundle {
    val instruction = Input(UInt(32.W))
    val instruction_address = Output(UInt(32.W))

    val debug_read_address = Input(UInt(32.W))
    val debug_read_data = Output(UInt(32.W))

    val debug_mem_read_address = Input(UInt(32.W))
    val debug_mem_read_data = Output(UInt(32.W))

    val char_mem_read_address = Input(UInt(32.W))
    val char_mem_read_data = Output(UInt(32.W))
  })

  val data_mem = Module(new Memory(1024))
  val pc = Module(new ProgramCounter)
  val ctrl = Module(new Control)
  val regs = Module(new RegisterFile)
  val if2id = Module(new IF2ID)
  val id = Module(new InstructionDecode)
  val id2ex = Module(new ID2EX)
  val ex = Module(new Execute)

  pc.io.hold_flag := ctrl.io.output_hold_flag
  pc.io.jump_enable := ctrl.io.pc_jump_flag
  pc.io.jump_address := ctrl.io.pc_jump_address

  ctrl.io.jump_flag := ex.io.ctrl_jump_flag
  ctrl.io.jump_address := ex.io.ctrl_jump_address
  ctrl.io.hold_flag_ex := ex.io.ctrl_hold_flag

  regs.io.write_enable := ex.io.regs_write_enable
  regs.io.write_address := ex.io.regs_write_address
  regs.io.write_data := ex.io.regs_write_data
  regs.io.read_address1 := id.io.regs_reg1_read_address
  regs.io.read_address2 := id.io.regs_reg2_read_address

  regs.io.debug_read_address := io.debug_read_address
  io.debug_read_data := regs.io.debug_read_data

  if2id.io.instruction := io.instruction
  if2id.io.instruction_address := pc.io.pc
  if2id.io.hold_flag := ctrl.io.output_hold_flag
  io.instruction_address := pc.io.pc

  id.io.reg1 := regs.io.read_data1
  id.io.reg2 := regs.io.read_data2
  id.io.instruction := if2id.io.output_instruction
  id.io.instruction_address := if2id.io.output_instruction_address
  id.io.jump_flag := ex.io.ctrl_jump_flag

  id2ex.io.instruction := id.io.ex_instruction
  id2ex.io.instruction_address := id.io.ex_instruction_address
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
  ex.io.op1 := id2ex.io.output_op1
  ex.io.op2 := id2ex.io.output_op2
  ex.io.op1_jump := id2ex.io.output_op1_jump
  ex.io.op2_jump := id2ex.io.output_op2_jump
  ex.io.reg1 := id2ex.io.output_reg1
  ex.io.reg2 := id2ex.io.output_reg2
  ex.io.write_enable := id2ex.io.output_write_enable
  ex.io.write_address := id2ex.io.output_write_address
  ex.io.data := data_mem.io.read_data

  data_mem.io.write_enable := ex.io.mem_write_enable
  data_mem.io.write_address := ex.io.mem_write_address
  data_mem.io.write_data := ex.io.mem_write_data
  data_mem.io.read_address := id.io.ex_mem_read_address

  data_mem.io.debug_read_address := io.debug_mem_read_address
  io.debug_mem_read_data := data_mem.io.debug_read_data

  data_mem.io.char_read_address := io.char_mem_read_address
  io.char_mem_read_data := data_mem.io.char_read_data
}
