package riscv

import chisel3._
import chisel3.util._

object InstructionTypes extends Enumeration {
  type InstructionType = Value
  //          0b0010011
  val I = Value(0x13)

}

object Instructions extends Enumeration {
  type Instruction = Value
  //          0b0110111
  val lui = Value(0x37)
  //          0b0000001
  val nop = Value(0x01)
  //          0b1101111
  val jal = Value(0x6f)
}

object InstructionsTypeI extends Enumeration {
  type InstructionTypeI = Value
  // 0b000
  val addi = Value
  // 0b001
  val slli = Value
  // 0b010
  val slti = Value
  // 0b011
  val sltiu = Value
  // 0b100
  val xori = Value
  // 0b101
  val sri = Value
  // 0b110
  val ori = Value
  // 0b111
  val andi = Value
}

class InstructionDecode extends Module {
  val io = IO(new Bundle {
    val instruction = Input(UInt(32.W))
    val instruction_address = Input(UInt(32.W))

    val reg1 = Input(UInt(32.W))
    val reg2 = Input(UInt(32.W))

    val regs_reg1_read_address = Output(UInt(32.W))
    val regs_reg2_read_address = Output(UInt(32.W))

    val ex_op1 = Output(UInt(32.W))
    val ex_op2 = Output(UInt(32.W))
    val ex_op1_jump = Output(UInt(32.W))
    val ex_op2_jump = Output(UInt(32.W))
    val ex_instruction = Output(UInt(32.W))
    val ex_instruction_address = Output(UInt(32.W))
    val ex_reg1 = Output(UInt(32.W))
    val ex_reg2 = Output(UInt(32.W))
    val ex_reg_write_enable = Output(UInt(32.W))
    val ex_reg_write_address = Output(UInt(5.W))
  })

  val opcode = io.instruction(6, 0)
  val funct3 = io.instruction(14, 12)
  val funct7 = io.instruction(31, 25)
  val rd = io.instruction(11, 7)
  val rs1 = io.instruction(19, 15)
  val rs2 = io.instruction(24, 20)

  io.ex_instruction := io.instruction
  io.ex_instruction_address := io.instruction_address
  io.ex_reg1 := io.reg1
  io.ex_reg2 := io.reg2
  io.ex_op1 := 0.U
  io.ex_op2 := 0.U
  io.ex_op1_jump := 0.U
  io.ex_op2_jump := 0.U

  when(opcode === InstructionTypes.I.id.U) {
    io.ex_reg_write_enable := true.B
    io.ex_reg_write_address := rd
    io.regs_reg1_read_address := rs1
    io.regs_reg2_read_address := 0.U
    io.ex_op1 := io.reg1
    io.ex_op2 := Cat(Fill(20, io.instruction(31)), io.instruction(31, 20))
  }.elsewhen(opcode === Instructions.jal.id.U) {
    io.ex_reg_write_enable := true.B
    io.ex_reg_write_address := rd
    io.regs_reg1_read_address := 0.U
    io.regs_reg2_read_address := 0.U
    io.ex_op1 := io.instruction_address
    io.ex_op2 := 4.U
    io.ex_op1_jump := io.instruction_address
    io.ex_op2_jump := Cat(Fill(12, io.instruction(31)), io.instruction(19, 12), io.instruction(20), io.instruction(30, 21), 0.U(1.W))
  }.elsewhen(opcode === Instructions.lui.id.U) {
    io.ex_reg_write_enable := true.B
    io.ex_reg_write_address := rd
    io.regs_reg1_read_address := 0.U
    io.regs_reg2_read_address := 0.U
    io.ex_op1 := Cat(io.instruction(31, 12), Fill(12, 0.U(1.W)))
    io.ex_op2 := 0.U
  }.elsewhen(opcode === Instructions.nop.id.U) {
    io.ex_reg_write_enable := false.B
    io.ex_reg_write_address := 0.U
    io.regs_reg1_read_address := 0.U
    io.regs_reg2_read_address := 0.U
  }.otherwise {
    io.ex_reg_write_enable := false.B
    io.ex_reg_write_address := 0.U
    io.regs_reg1_read_address := 0.U
    io.regs_reg2_read_address := 0.U
  }

}
