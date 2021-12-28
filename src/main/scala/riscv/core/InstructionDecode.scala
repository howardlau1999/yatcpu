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
import chisel3.util._
import riscv.Parameters


object InstructionTypes extends Bundle {
  val L = "b0000011".U
  val I = "b0010011".U
  val S = "b0100011".U
  val RM = "b0110011".U
  val B = "b1100011".U
}

object Instructions extends Bundle {
  val lui = "b0110111".U
  val nop = "b0000001".U
  val jal = "b1101111".U
  val jalr = "b1100111".U
  val auipc = "b0010111".U
  val csr = "b1110011".U
  val fence = "b0001111".U
}

object InstructionsTypeL extends Bundle {
  val lb = "b000".U
  val lh = "b001".U
  val lw = "b010".U
  val lbu = "b100".U
  val lhu = "b101".U
}

object InstructionsTypeI extends Bundle {
  val addi = 0.U
  val slli = 1.U
  val slti = 2.U
  val sltiu = 3.U
  val xori = 4.U
  val sri = 5.U
  val ori = 6.U
  val andi = 7.U
}

object InstructionsTypeS extends Bundle {
  val sb = "b000".U
  val sh = "b001".U
  val sw = "b010".U
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
  val beq = "b000".U
  val bne = "b001".U
  val blt = "b100".U
  val bge = "b101".U
  val bltu = "b110".U
  val bgeu = "b111".U
}

object InstructionsTypeCSR extends Bundle {
  val csrrw = "b001".U
  val csrrs = "b010".U
  val csrrc = "b011".U
  val csrrwi = "b101".U
  val csrrsi = "b110".U
  val csrrci = "b111".U
}

object InstructionsNop extends Bundle {
  val nop = 0x00000013L.U(Parameters.DataWidth)
}

object InstructionsRet extends Bundle {
  val mret = 0x30200073L.U(Parameters.DataWidth)
  val ret = 0x00008067L.U(Parameters.DataWidth)
}

object InstructionsEnv extends Bundle {
  val ecall = 0x00000073L.U(Parameters.DataWidth)
  val ebreak = 0x00100073L.U(Parameters.DataWidth)
}

class InstructionDecode extends Module {
  val io = IO(new Bundle {
    val instruction = Input(UInt(Parameters.InstructionWidth))
    val instruction_address = Input(UInt(Parameters.AddrWidth))
    val reg1_data = Input(UInt(Parameters.DataWidth))
    val reg2_data = Input(UInt(Parameters.DataWidth))

    val csr_read_data = Input(UInt(Parameters.DataWidth))

    val regs_reg1_read_address = Output(UInt(Parameters.PhysicalRegisterAddrWidth))
    val regs_reg2_read_address = Output(UInt(Parameters.PhysicalRegisterAddrWidth))

    val ctrl_stall_flag = Output(UInt(Parameters.StallStateWidth))

    val ex_op1 = Output(UInt(Parameters.DataWidth))
    val ex_op2 = Output(UInt(Parameters.DataWidth))
    val ex_op1_jump = Output(UInt(Parameters.DataWidth))
    val ex_op2_jump = Output(UInt(Parameters.DataWidth))
    val ex_instruction = Output(UInt(Parameters.DataWidth))
    val ex_instruction_address = Output(UInt(Parameters.AddrWidth))
    val ex_reg1_data = Output(UInt(Parameters.DataWidth))
    val ex_reg2_data = Output(UInt(Parameters.DataWidth))
    val ex_reg_write_enable = Output(Bool())
    val ex_reg_write_address = Output(UInt(Parameters.PhysicalRegisterAddrWidth))
    val ex_mem_read_address = Output(UInt(Parameters.AddrWidth))


    val csr_read_address = Output(UInt(Parameters.CSRRegisterAddrWidth))
    val ex_csr_write_enable = Output(Bool())
    val ex_csr_write_address = Output(UInt(Parameters.CSRRegisterAddrWidth))
    val ex_csr_write_data = Output(UInt(Parameters.DataWidth))
    val ex_csr_read_data = Output(UInt(Parameters.DataWidth))
  })
  val opcode = io.instruction(6, 0)
  val funct3 = io.instruction(14, 12)
  val funct7 = io.instruction(31, 25)
  val rd = io.instruction(11, 7)
  val rs1 = io.instruction(19, 15)
  val rs2 = io.instruction(24, 20)
  val last_write_address = RegInit(UInt(Parameters.AddrWidth), 0.U)

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
  io.ex_reg1_data := io.reg1_data
  io.ex_reg2_data := io.reg2_data
  io.ex_op1 := 0.U
  io.ex_op2 := 0.U
  io.ex_op1_jump := 0.U
  io.ex_op2_jump := 0.U
  io.ex_mem_read_address := 0.U
  io.ctrl_stall_flag := false.B
  io.csr_read_address := 0.U
  io.ex_csr_read_data := io.csr_read_data
  io.ex_csr_write_enable := false.B
  io.ex_csr_write_data := 0.U
  io.ex_csr_write_address := 0.U
  last_write_address := 0.U

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
      io.ex_op1 := io.reg1_data
      io.ex_op2 := Cat(Fill(20, io.instruction(31)), io.instruction(31, 20))
      io.ex_mem_read_address := io.ex_op1 + io.ex_op2
      io.ctrl_stall_flag := io.ex_mem_read_address === last_write_address
    }.otherwise {
      disable_regs()
    }
  }.elsewhen(opcode === InstructionTypes.I) {
    enable_write(rd)
    io.regs_reg1_read_address := rs1
    io.regs_reg2_read_address := 0.U
    io.ex_op1 := io.reg1_data
    io.ex_op2 := Cat(Fill(20, io.instruction(31)), io.instruction(31, 20))
  }.elsewhen(opcode === InstructionTypes.S) {
    when(funct3 === InstructionsTypeS.sb ||
      funct3 === InstructionsTypeS.sh ||
      funct3 === InstructionsTypeS.sw) {
      disable_write()
      io.regs_reg1_read_address := rs1
      io.regs_reg2_read_address := rs2
      io.ex_op1 := io.reg1_data
      io.ex_op2 := Cat(Fill(20, io.instruction(31)), io.instruction(31, 25), io.instruction(11, 7))
      io.ex_mem_read_address := io.ex_op1 + io.ex_op2
      last_write_address := io.ex_op1 + io.ex_op2
    }.otherwise {
      disable_regs()
    }
  }.elsewhen(opcode === InstructionTypes.RM) {
    when(funct7 === 0.U || funct7 === 0x20.U) {
      enable_write(rd)
      io.regs_reg1_read_address := rs1
      io.regs_reg2_read_address := rs2
      io.ex_op1 := io.reg1_data
      io.ex_op2 := io.reg2_data
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
      io.ex_op1 := io.reg1_data
      io.ex_op2 := io.reg2_data
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
    io.ex_op1_jump := io.reg1_data
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
  }.elsewhen(opcode === Instructions.csr) {
    disable_regs()
    io.csr_read_address := Cat(0.U(20.W), io.instruction(31, 20))
    io.ex_csr_write_address := Cat(0.U(20.W), io.instruction(31, 20))
    when(
      funct3 === InstructionsTypeCSR.csrrc ||
        funct3 === InstructionsTypeCSR.csrrs ||
        funct3 === InstructionsTypeCSR.csrrw
    ) {
      io.regs_reg1_read_address := rs1
      io.regs_reg2_read_address := 0.U
      io.ex_reg_write_enable := true.B
      io.ex_reg_write_address := rd
      io.ex_csr_write_enable := true.B
    }.elsewhen(
      funct3 === InstructionsTypeCSR.csrrci ||
        funct3 === InstructionsTypeCSR.csrrsi ||
        funct3 === InstructionsTypeCSR.csrrwi
    ) {
      io.regs_reg1_read_address := 0.U
      io.regs_reg2_read_address := 0.U
      io.ex_reg_write_enable := true.B
      io.ex_reg_write_address := rd
      io.ex_csr_write_enable := true.B
    }.otherwise {
      io.ex_csr_write_enable := false.B
      io.ex_csr_write_data := 0.U
      io.ex_csr_write_address := 0.U
    }
  }.elsewhen(opcode === Instructions.nop) {
    disable_regs()
  }.otherwise {
    disable_regs()
  }

}
