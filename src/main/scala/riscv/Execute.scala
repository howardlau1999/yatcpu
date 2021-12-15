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
    val mem_write_data = Output(UInt(32.W))

    val regs_write_enable = Output(Bool())
    val regs_write_address = Output(UInt(32.W))
    val regs_write_data = Output(UInt(32.W))

    val ctrl_hold_flag = Output(Bool())
    val ctrl_jump_flag = Output(Bool())
    val ctrl_jump_address = Output(UInt(32.W))
  })

  val opcode = io.instruction(6, 0)
  val funct3 = io.instruction(14, 12)
  val funct7 = io.instruction(31, 25)
  val rd = io.instruction(11, 7)
  val uimm = io.instruction(19, 15)

  val signed_op1 = io.op1.asSInt()
  val signed_op2 = io.op2.asSInt()

  val mem_read_address_index = ((io.reg1 + Cat(Fill(20, io.instruction(31)), io.instruction(31, 20))) & 0x3.U).asUInt()
  val mem_write_address_index = ((io.reg1 + Cat(Fill(20, io.instruction(31)), io.instruction(31, 25), io.instruction(11, 7))) & 0x3.U).asUInt()

  def disable_memory() = {
    disable_memory_write()
    io.mem_read_address := 0.U
  }

  def disable_memory_write() = {
    io.mem_write_enable := false.B
    io.mem_write_data := 0.U
    io.mem_write_address := 0.U
  }

  def disable_control() = {
    disable_hold()
    disable_jump()
  }

  def disable_hold() = {
    io.ctrl_hold_flag := false.B
  }

  def disable_jump() = {
    io.ctrl_jump_address := 0.U
    io.ctrl_jump_flag := false.B
  }

  io.regs_write_enable := io.write_enable
  io.regs_write_address := io.write_address
  io.regs_write_data := 0.U

  when(opcode === InstructionTypes.I.id.U) {
    disable_control()
    disable_memory()
    when(funct3 === InstructionsTypeI.addi.id.U) {
      io.regs_write_data := io.op1 + io.op2
    }.elsewhen(funct3 === InstructionsTypeI.slli.id.U) {
      io.regs_write_data := io.reg1 << io.instruction(24, 20)
    }.elsewhen(funct3 === InstructionsTypeI.slti.id.U) {
      io.regs_write_data := signed_op1 < signed_op2
    }.elsewhen(funct3 === InstructionsTypeI.sltiu.id.U) {
      io.regs_write_data := io.op1 < io.op2
    }.elsewhen(funct3 === InstructionsTypeI.xori.id.U) {
      io.regs_write_data := io.op1 ^ io.op2
    }.elsewhen(funct3 === InstructionsTypeI.sri.id.U) {
      when(io.instruction(30) === 1.U) {
        val mask = (0xFFFFFFFFL.U >> io.instruction(24, 20)).asUInt()
        io.regs_write_data := ((io.reg1 >> io.instruction(24, 20)).asUInt() & mask |
          (Fill(32, io.reg1(31)) & (~mask).asUInt()).asUInt())
      }.otherwise {
        io.regs_write_data := io.reg1 >> io.instruction(24, 20)
      }
    }.elsewhen(funct3 === InstructionsTypeI.ori.id.U) {
      io.regs_write_data := io.op1 | io.op2
    }.elsewhen(funct3 === InstructionsTypeI.andi.id.U) {
      io.regs_write_data := io.op1 & io.op2
    }.otherwise {
      io.regs_write_data := 0.U
    }
  }.elsewhen(opcode === InstructionTypes.RM.id.U) {
    disable_memory()
    disable_control()
    // TODO(howard): support mul and div
    when(funct7 === 0.U || funct7 === 0x20.U) {
      when(funct3 === InstructionsTypeR.add_sub.id.U) {
        when(io.instruction(30) === 0.U) {
          io.regs_write_data := io.op1 + io.op2
        }.otherwise {
          io.regs_write_data := io.op1 - io.op2
        }
      }.elsewhen(funct3 === InstructionsTypeR.sll.id.U) {
        io.regs_write_data := io.op1 << io.op2(4, 0)
      }.elsewhen(funct3 === InstructionsTypeR.slt.id.U) {
        io.regs_write_data := signed_op1 < signed_op2
      }.elsewhen(funct3 === InstructionsTypeR.sltu.id.U) {
        io.regs_write_data := io.op1 < io.op2
      }.elsewhen(funct3 === InstructionsTypeR.xor.id.U) {
        io.regs_write_data := io.op1 ^ io.op2
      }.elsewhen(funct3 === InstructionsTypeR.sr.id.U) {
        when(io.instruction(30) === 1.U) {
          val mask = (0xFFFFFFFFL.U >> io.reg2(4, 0)).asUInt()
          io.regs_write_data := ((io.reg1 >> io.reg2(4, 0)).asUInt() & mask |
            (Fill(32, io.reg1(31)) & (~mask).asUInt()).asUInt())
        }.otherwise {
          io.regs_write_data := io.reg1 >> io.reg2(4, 0)
        }
      }.elsewhen(funct3 === InstructionsTypeR.or.id.U) {
        io.regs_write_data := io.op1 | io.op2
      }.elsewhen(funct3 === InstructionsTypeR.and.id.U) {
        io.regs_write_data := io.op1 & io.op2
      }
    }
  }.elsewhen(opcode === InstructionTypes.L.id.U) {
    disable_memory_write()
    disable_control()
    io.mem_read_address := io.op1 + io.op2
    when(funct3 === InstructionsTypeL.lb.id.U || funct3 === InstructionsTypeL.lbu.id.U) {
      val is_lbu = funct3 === InstructionsTypeL.lbu.id.U
      when(mem_read_address_index === 0.U) {
        when(is_lbu) {
          io.regs_write_data := Cat(Fill(24, 0.U), io.data(7, 0))
        }.otherwise {
          io.regs_write_data := Cat(Fill(24, io.data(7)), io.data(7, 0))
        }
      }.elsewhen(mem_read_address_index === 1.U) {
        when(is_lbu) {
          io.regs_write_data := Cat(Fill(24, 0.U), io.data(15, 8))
        }.otherwise {
          io.regs_write_data := Cat(Fill(24, io.data(15)), io.data(15, 8))
        }

      }.elsewhen(mem_read_address_index === 2.U) {
        when(is_lbu) {
          io.regs_write_data := Cat(Fill(24, 0.U), io.data(23, 16))
        }.otherwise {
          io.regs_write_data := Cat(Fill(24, io.data(23)), io.data(23, 16))
        }
      }.otherwise {
        when(is_lbu) {
          io.regs_write_data := Cat(Fill(24, 0.U), io.data(31, 24))
        }.otherwise {
          io.regs_write_data := Cat(Fill(24, io.data(31)), io.data(31, 24))
        }
      }
    }.elsewhen(funct3 === InstructionsTypeL.lh.id.U || funct3 === InstructionsTypeL.lhu.id.U) {
      val is_lhu = funct3 === InstructionsTypeL.lhu.id.U
      when(mem_read_address_index === 0.U) {
        when(is_lhu) {
          io.regs_write_data := Cat(Fill(16, 0.U), io.data(15, 0))
        }.otherwise {
          io.regs_write_data := Cat(Fill(16, io.data(15)), io.data(15, 0))
        }
      }.otherwise {
        when(is_lhu) {
          io.regs_write_data := Cat(Fill(16, 0.U), io.data(31, 16))
        }.otherwise {
          io.regs_write_data := Cat(Fill(16, io.data(31)), io.data(31, 16))
        }
      }
    }.elsewhen(funct3 === InstructionsTypeL.lw.id.U) {
      io.regs_write_data := io.data
    }.otherwise {
      io.regs_write_data := 0.U
    }
  }.elsewhen(opcode === InstructionTypes.S.id.U) {
    disable_control()
    io.mem_read_address := io.op1 + io.op2
    io.mem_write_address := io.op1 + io.op2
    io.mem_write_enable := true.B
    when(funct3 === InstructionsTypeS.sb.id.U) {
      when(mem_write_address_index === 0.U) {
        io.mem_write_data := Cat(io.data(31, 8), io.reg2(7, 0))
      }.elsewhen(mem_write_address_index === 1.U) {
        io.mem_write_data := Cat(io.data(31, 16), io.reg2(7, 0), io.data(7, 0))
      }.elsewhen(mem_write_address_index === 2.U) {
        io.mem_write_data := Cat(io.data(31, 24), io.reg2(7, 0), io.data(15, 0))
      }.otherwise {
        io.mem_write_data := Cat(io.reg2(7, 0), io.data(23, 0))
      }
    }.elsewhen(funct3 === InstructionsTypeS.sh.id.U) {
      when(mem_write_address_index === 0.U) {
        io.mem_write_data := Cat(io.data(31, 16), io.reg2(15, 0))
      }.otherwise {
        io.mem_write_data := Cat(io.reg2(15, 0), io.data(15, 0))
      }
    }.elsewhen(funct3 === InstructionsTypeS.sw.id.U) {
      io.mem_write_data := io.reg2
    }.otherwise {
      disable_memory()
    }
  }.elsewhen(opcode === InstructionTypes.B.id.U) {
    disable_control()
    disable_memory()
    when(funct3 === InstructionsTypeB.beq.id.U) {
      io.ctrl_jump_flag := io.op1 === io.op2
      io.ctrl_jump_address := Fill(32, io.op1 === io.op2) & (io.op1_jump + io.op2_jump)
    }.elsewhen(funct3 === InstructionsTypeB.bne.id.U) {
      io.ctrl_jump_flag := io.op1 =/= io.op2
      io.ctrl_jump_address := Fill(32, io.op1 =/= io.op2) & (io.op1_jump + io.op2_jump)
    }.elsewhen(funct3 === InstructionsTypeB.bltu.id.U) {
      io.ctrl_jump_flag := io.op1 < io.op2
      io.ctrl_jump_address := Fill(32, io.op1 < io.op2) & (io.op1_jump + io.op2_jump)
    }.elsewhen(funct3 === InstructionsTypeB.bgeu.id.U) {
      io.ctrl_jump_flag := io.op1 >= io.op2
      io.ctrl_jump_address := Fill(32, io.op1 >= io.op2) & (io.op1_jump + io.op2_jump)
    }.elsewhen(funct3 === InstructionsTypeB.blt.id.U) {
      io.ctrl_jump_flag := signed_op1 < signed_op2
      io.ctrl_jump_address := Fill(32, signed_op1 < signed_op2) & (io.op1_jump + io.op2_jump)
    }.elsewhen(funct3 === InstructionsTypeB.bge.id.U) {
      io.ctrl_jump_flag := signed_op1 >= signed_op2
      io.ctrl_jump_address := Fill(32, signed_op1 >= signed_op2) & (io.op1_jump + io.op2_jump)
    }
  }.elsewhen(opcode === Instructions.jal.id.U || opcode === Instructions.jalr.id.U) {
    disable_memory()
    disable_hold()
    io.ctrl_jump_flag := true.B
    io.ctrl_jump_address := io.op1_jump + io.op2_jump
    io.regs_write_data := io.op1 + io.op2
  }.elsewhen(opcode === Instructions.lui.id.U || opcode === Instructions.auipc.id.U) {
    disable_control()
    disable_memory()
    io.regs_write_data := io.op1 + io.op2
  }.elsewhen(opcode === Instructions.nop.id.U) {
    disable_control()
    disable_memory()
    io.regs_write_data := 0.U
  }.otherwise {
    disable_control()
    disable_memory()
    io.regs_write_data := 0.U
  }
}
