package riscv

import chisel3._
import chisel3.util._

object InstructionTypes extends Enumeration {
  type InstructionType = Value

  //          0b0000011
  val L = Value(0x03)
  //          0b0010011
  val I = Value(0x13)
  //          0b0100011
  val S = Value(0x23)
  //          0b0110011
  val RM = Value(0x33)
  //          0b1100011
  val B = Value(0x63)
}

object Instructions extends Enumeration {
  type Instruction = Value
  //          0b0110111
  val lui = Value(0x37)
  //          0b0000001
  val nop = Value(0x01)
  //          0b1101111
  val jal = Value(0x6f)
  //          0b1100111
  val jalr = Value(0x67)
}

object InstructionsTypeL extends Enumeration {
  type InstructionTypeL = Value
  // 0b000
  val lb = Value(0)
  // 0b001
  val lh = Value(1)
  // 0b010
  val lw = Value(2)
  // 0b100
  val lbu = Value(4)
  // 0b101
  val lhu = Value(5)
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

object InstructionsTypeS extends Enumeration {
  type InstructionTypeS = Value
  // 0b000
  val sb = Value(0)
  // 0b001
  val sh = Value(1)
  // 0b010
  val sw = Value(2)
}

object InstructionsTypeR extends Enumeration {
  type InstructionTypeR = Value
  val add_sub, sll, slt, sltu, xor, sr, or, and = Value
}

object InstructionsTypeM extends Enumeration {
  type InstructionTypeM = Value
  val mul, mulh, mulhsu, mulhum, div, divu, rem, remu = Value
}

object InstructionsTypeB extends Enumeration {
  type InstructionTypeB = Value
  // 0b000
  val beq = Value(0)
  // 0b001
  val bne = Value(1)
  // 0b100
  val blt = Value(4)
  // 0b101
  val bge = Value(5)
  // 0b110
  val bltu = Value(6)
  // 0b111
  val bgeu = Value(7)
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

  def disable_regs() = {
    disable_write()
    io.regs_reg1_read_address := 0.U
    io.regs_reg2_read_address := 0.U
  }

  def disable_write() = {
    io.ex_reg_write_enable := false.B
    io.ex_reg_write_address := 0.U
  }

  def enable_write(addr: UInt) = {
    io.ex_reg_write_enable := true.B
    io.ex_reg_write_address := addr
  }

  io.ex_instruction := io.instruction
  io.ex_instruction_address := io.instruction_address
  io.ex_reg1 := io.reg1
  io.ex_reg2 := io.reg2
  io.ex_op1 := 0.U
  io.ex_op2 := 0.U
  io.ex_op1_jump := 0.U
  io.ex_op2_jump := 0.U

  when(opcode === InstructionTypes.L.id.U) {
    when(
      funct3 === InstructionsTypeL.lb.id.U ||
        funct3 === InstructionsTypeL.lh.id.U ||
        funct3 === InstructionsTypeL.lw.id.U ||
        funct3 === InstructionsTypeL.lbu.id.U ||
        funct3 === InstructionsTypeL.lhu.id.U
    ) {
      enable_write(rd)
      io.regs_reg1_read_address := rs1
      io.regs_reg2_read_address := 0.U
      io.ex_op1 := io.reg1
      io.ex_op2 := Cat(Fill(20, io.instruction(31)), io.instruction(31, 20))
    }.otherwise {
      disable_regs()
    }
  }.elsewhen(opcode === InstructionTypes.I.id.U) {
    enable_write(rd)
    io.regs_reg1_read_address := rs1
    io.regs_reg2_read_address := 0.U
    io.ex_op1 := io.reg1
    io.ex_op2 := Cat(Fill(20, io.instruction(31)), io.instruction(31, 20))
  }.elsewhen(opcode === InstructionTypes.S.id.U) {
    when(funct3 === InstructionsTypeS.sb.id.U ||
      funct3 === InstructionsTypeS.sh.id.U ||
      funct3 === InstructionsTypeS.sw.id.U) {
      disable_write()
      io.regs_reg1_read_address := rs1
      io.regs_reg2_read_address := rs2
      io.ex_op1 := io.reg1
      io.ex_op2 := Cat(Fill(20, io.instruction(31)), io.instruction(31, 25), io.instruction(11, 7))
    }.otherwise {
      disable_regs()
    }
  }.elsewhen(opcode === InstructionTypes.RM.id.U) {
    when(funct7 === 0.U || funct7 === 0x20.U) {
      enable_write(rd)
      io.regs_reg1_read_address := rs1
      io.regs_reg2_read_address := rs2
      io.ex_op1 := io.reg1
      io.ex_op2 := io.reg2
    }.otherwise {
      // TODO(howard): implement mul and div
      disable_regs()
    }
  }.elsewhen(opcode === InstructionTypes.B.id.U) {
    when(
      funct3 === InstructionsTypeB.beq.id.U ||
        funct3 === InstructionsTypeB.bne.id.U ||
        funct3 === InstructionsTypeB.blt.id.U ||
        funct3 === InstructionsTypeB.bge.id.U ||
        funct3 === InstructionsTypeB.bltu.id.U ||
        funct3 === InstructionsTypeB.bgeu.id.U
    ) {
      disable_write()
      io.regs_reg1_read_address := rs1
      io.regs_reg2_read_address := rs2
      io.ex_op1 := io.reg1
      io.ex_op2 := io.reg2
      io.ex_op1_jump := io.instruction_address
      io.ex_op2_jump := Cat(Fill(20, io.instruction(31)), io.instruction(7), io.instruction(30, 25), io.instruction(11, 8), 0.U(1.W))
    }.otherwise {
      disable_regs()
    }
  }.elsewhen(opcode === Instructions.jal.id.U) {
    enable_write(rd)
    io.regs_reg1_read_address := 0.U
    io.regs_reg2_read_address := 0.U
    io.ex_op1 := io.instruction_address
    io.ex_op2 := 4.U
    io.ex_op1_jump := io.instruction_address
    io.ex_op2_jump := Cat(Fill(12, io.instruction(31)), io.instruction(19, 12), io.instruction(20), io.instruction(30, 21), 0.U(1.W))
  }.elsewhen(opcode === Instructions.jalr.id.U) {
    enable_write(rd)
    io.regs_reg1_read_address := rs1
    io.regs_reg2_read_address := 0.U
    io.ex_op1 := io.instruction_address
    io.ex_op2 := 4.U
    io.ex_op1_jump := io.reg1
    io.ex_op2_jump := Cat(Fill(20, io.instruction(31)), io.instruction(31, 20))
  }.elsewhen(opcode === Instructions.lui.id.U) {
    enable_write(rd)
    io.regs_reg1_read_address := 0.U
    io.regs_reg2_read_address := 0.U
    io.ex_op1 := Cat(io.instruction(31, 12), Fill(12, 0.U(1.W)))
    io.ex_op2 := 0.U
  }.elsewhen(opcode === Instructions.nop.id.U) {
    disable_regs()
  }.otherwise {
    disable_regs()
  }

}
