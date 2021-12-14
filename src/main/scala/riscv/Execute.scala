package riscv

import chisel3._
import chisel3.util._

class Execute extends Module {
  val io = IO(new Bundle {
    val instruction = Input(UInt(32.W))
    val instruction_address = Input(UInt(32.W))
    val write_enable = Input(Bool())
    val write_address = Input(UInt(32.W))
    val reg1 = Input(UInt(32.W))
    val reg2 = Input(UInt(32.W))
    val op1 = Input(UInt(32.W))
    val op2 = Input(UInt(32.W))
    val op1_jump = Input(UInt(32.W))
    val op2_jump = Input(UInt(32.W))

    val data = Input(UInt(32.W))

    val mem_write_enable = Output(Bool())
    val mem_read_address = Output(UInt(32.W))
    val mem_write_address = Output(UInt(32.W))
    val mem_req = Output(Bool())

    val reg_write_enable = Output(Bool())
    val reg_write_address = Output(UInt(32.W))
    val reg_write_data = Output(UInt(32.W))

    val ctrl_hold_flag = Output(Bool())
    val ctrl_jump_flag = Output(Bool())
    val ctrl_jump_address = Output(UInt(32.W))
  })

  val opcode = io.instruction(6, 0)
  val funct3 = io.instruction(14, 12)
  val funct7 = io.instruction(31, 25)
  val rd = io.instruction(11, 7)
  val uimm = io.instruction(19, 15)


}
