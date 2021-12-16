package riscv

import chisel3._
import chisel3.util._

object InstructionTypes extends Bundle {

  //          0b0000011
  val L = 0x03.U
  //          0b0010011
  val I = 0x13.U
  //          0b0100011
  val S = 0x23.U
  //          0b0110011
  val RM = 0x33.U
  //          0b1100011
  val B = 0x63.U
}

object Instructions extends Bundle {

  //          0b0110111
  val lui = 0x37.U
  //          0b0000001
  val nop = 0x01.U
  //          0b1101111
  val jal = 0x6f.U
  //          0b1100111
  val jalr = 0x67.U
  //          0b0010111
  val auipc = 0x17.U
}

object InstructionsTypeL extends Bundle {

  // 0b000
  val lb = 0.U
  // 0b001
  val lh = 1.U
  // 0b010
  val lw = 2.U
  // 0b100
  val lbu = 4.U
  // 0b101
  val lhu = 5.U
}

object InstructionsTypeI extends Bundle {

  // 0b000
  val addi = 0.U
  // 0b001
  val slli = 1.U
  // 0b010
  val slti = 2.U
  // 0b011
  val sltiu = 3.U
  // 0b100
  val xori = 4.U
  // 0b101
  val sri = 5.U
  // 0b110
  val ori = 6.U
  // 0b111
  val andi = 7.U
}

object InstructionsTypeS extends Bundle {

  // 0b000
  val sb = 0.U
  // 0b001
  val sh = 1.U
  // 0b010
  val sw = 2.U
}

object InstructionsTypeR extends Bundle {
  val add_sub = 0.U
  val sll = 1.U
  val slt = 2.U
  val sltu = 3.U
  val xor = 4.U
  val sr = 5.U
  val or = 6.U
  val and = 7.U
}

object InstructionsTypeM extends Bundle {
  val mul = 0.U
  val mulh = 1.U
  val mulhsu = 2.U
  val mulhum = 3.U
  val div = 4.U
  val divu = 5.U
  val rem = 6.U
  val remu = 7.U
}

object InstructionsTypeB extends Bundle {

  // 0b000
  val beq = 0.U
  // 0b001
  val bne = 1.U
  // 0b100
  val blt = 4.U
  // 0b101
  val bge = 5.U
  // 0b110
  val bltu = 6.U
  // 0b111
  val bgeu = 7.U
}

class InstructionDecode extends Module {
  val io = IO(new Bundle {
    val instruction = Input(UInt(32.W))
    val instruction_address = Input(UInt(32.W))
    val jump_flag = Input(Bool())
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
    val ex_mem_read_address = Output(UInt(32.W))
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
  io.ex_mem_read_address := 0.U

  when(opcode === InstructionTypes.L) {
    when(
      funct3 === InstructionsTypeL.lb ||
        funct3 === InstructionsTypeL.lh ||
        funct3 === InstructionsTypeL.lw ||
        funct3 === InstructionsTypeL.lbu ||
        funct3 === InstructionsTypeL.lhu
    ) {
      enable_write(rd)
      io.regs_reg1_read_address := rs1
      io.regs_reg2_read_address := 0.U
      io.ex_op1 := io.reg1
      io.ex_op2 := Cat(Fill(20, io.instruction(31)), io.instruction(31, 20))
      io.ex_mem_read_address := io.ex_op1 + io.ex_op2
    }.otherwise {
      disable_regs()
    }
  }.elsewhen(opcode === InstructionTypes.I) {
    enable_write(rd)
    io.regs_reg1_read_address := rs1
    io.regs_reg2_read_address := 0.U
    io.ex_op1 := io.reg1
    io.ex_op2 := Cat(Fill(20, io.instruction(31)), io.instruction(31, 20))
  }.elsewhen(opcode === InstructionTypes.S) {
    when(funct3 === InstructionsTypeS.sb ||
      funct3 === InstructionsTypeS.sh ||
      funct3 === InstructionsTypeS.sw) {
      disable_write()
      io.regs_reg1_read_address := rs1
      io.regs_reg2_read_address := rs2
      io.ex_op1 := io.reg1
      io.ex_op2 := Cat(Fill(20, io.instruction(31)), io.instruction(31, 25), io.instruction(11, 7))
    }.otherwise {
      disable_regs()
    }
  }.elsewhen(opcode === InstructionTypes.RM) {
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
  }.elsewhen(opcode === InstructionTypes.B) {
    when(
      funct3 === InstructionsTypeB.beq ||
        funct3 === InstructionsTypeB.bne ||
        funct3 === InstructionsTypeB.blt ||
        funct3 === InstructionsTypeB.bge ||
        funct3 === InstructionsTypeB.bltu ||
        funct3 === InstructionsTypeB.bgeu
    ) {
      disable_write()
      io.regs_reg1_read_address := rs1
      io.regs_reg2_read_address := rs2
      io.ex_op1 := io.reg1
      io.ex_op2 := io.reg2
      io.ex_op1_jump := io.instruction_address
      io.ex_op2_jump := Cat(Fill(20, io.instruction(31)), io.instruction(7), io.instruction(30, 25), io.instruction
      (11, 8), 0.U(1.W))
    }.otherwise {
      disable_regs()
    }
  }.elsewhen(opcode === Instructions.jal) {
    enable_write(rd)
    io.regs_reg1_read_address := 0.U
    io.regs_reg2_read_address := 0.U
    io.ex_op1 := io.instruction_address
    io.ex_op2 := 4.U
    io.ex_op1_jump := io.instruction_address
    io.ex_op2_jump := Cat(Fill(12, io.instruction(31)), io.instruction(19, 12), io.instruction(20), io.instruction
    (30, 21), 0.U(1.W))
  }.elsewhen(opcode === Instructions.jalr) {
    enable_write(rd)
    io.regs_reg1_read_address := rs1
    io.regs_reg2_read_address := 0.U
    io.ex_op1 := io.instruction_address
    io.ex_op2 := 4.U
    io.ex_op1_jump := io.reg1
    io.ex_op2_jump := Cat(Fill(20, io.instruction(31)), io.instruction(31, 20))
  }.elsewhen(opcode === Instructions.lui) {
    enable_write(rd)
    io.regs_reg1_read_address := 0.U
    io.regs_reg2_read_address := 0.U
    io.ex_op1 := Cat(io.instruction(31, 12), Fill(12, 0.U(1.W)))
    io.ex_op2 := 0.U
  }.elsewhen(opcode === Instructions.auipc) {
    enable_write(rd)
    io.regs_reg1_read_address := 0.U
    io.regs_reg2_read_address := 0.U
    io.ex_op1 := io.instruction_address
    io.ex_op2 := Cat(io.instruction(31, 12), Fill(12, 0.U(1.W)))
  }.elsewhen(opcode === Instructions.nop) {
    disable_regs()
  }.otherwise {
    disable_regs()
  }

}
