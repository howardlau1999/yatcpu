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
  val mem_write_address_index = ((io.reg1 + Cat(Fill(20, io.instruction(31)), io.instruction(31, 25), io.instruction
  (11, 7))) & 0x3.U).asUInt()

  val mem_read_address_aligned = ((io.reg1 + Cat(Fill(20, io.instruction(31)), io.instruction(31, 20))) / 4.U)
  val mem_write_address_aligned = ((io.reg1 + Cat(Fill(20, io.instruction(31)), io.instruction(31, 25), io.instruction
  (11, 7)))) / 4.U

  def disable_memory() = {
    disable_memory_write()
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

  val writing_mem = RegInit(Bool(), false.B)
  val mem_write_address = Reg(UInt(32.W))
  val mem_write_data = Reg(UInt(32.W))

  when(opcode === InstructionTypes.I) {
    disable_control()
    disable_memory()
    val mask = (0xFFFFFFFFL.U >> io.instruction(24, 20)).asUInt()
    io.regs_write_data := MuxLookup(
      funct3,
      0.U,
      Array(
        InstructionsTypeI.addi -> (io.op1 + io.op2),
        InstructionsTypeI.slli -> ((io.reg1 << io.instruction(24, 20)).asUInt()),
        InstructionsTypeI.slti -> (signed_op1 < signed_op2),
        InstructionsTypeI.sltiu -> (io.op1 < io.op2),
        InstructionsTypeI.xori -> (io.op1 ^ io.op2),
        InstructionsTypeI.ori -> (io.op1 | io.op2),
        InstructionsTypeI.andi -> (io.op1 & io.op2),
        InstructionsTypeI.sri -> Mux(
          io.instruction(30) === 1.U,
          ((io.reg1 >> io.instruction(24, 20)).asUInt() & mask |
            (Fill(32, io.reg1(31)) & (~mask).asUInt()).asUInt()),
          io.reg1 >> io.instruction(24, 20)
        )
      )
    )
  }.elsewhen(opcode === InstructionTypes.RM) {
    disable_memory()
    disable_control()
    // TODO(howard): support mul and div
    when(funct7 === 0.U || funct7 === 0x20.U) {
      val mask = (0xFFFFFFFFL.U >> io.reg2(4, 0)).asUInt()
      io.regs_write_data := MuxLookup(
        funct3,
        0.U,
        Array(
          InstructionsTypeR.add_sub -> Mux(
            io.instruction(30) === 0.U,
            io.op1 + io.op2,
            io.op1 - io.op2
          ),
          InstructionsTypeR.sll -> ((io.op1 << io.op2(4, 0)).asUInt()),
          InstructionsTypeR.slt -> (signed_op1 < signed_op2),
          InstructionsTypeR.sltu -> (io.op1 < io.op2),
          InstructionsTypeR.xor -> (io.op1 ^ io.op2),
          InstructionsTypeR.or -> (io.op1 | io.op2),
          InstructionsTypeR.and -> (io.op1 & io.op2),
          InstructionsTypeR.sr -> Mux(
            io.instruction(30) === 1.U,
            ((io.reg1 >> io.reg2(4, 0)).asUInt() & mask |
              (Fill(32, io.reg1(31)) & (~mask).asUInt()).asUInt()),
            io.reg1 >> io.reg2(4, 0)
          )
        )
      )
    }
  }.elsewhen(opcode === InstructionTypes.L) {
    disable_memory_write()
    disable_control()
    io.regs_write_data := MuxLookup(
      funct3,
      0.U,
      Array(
        InstructionsTypeL.lb -> MuxLookup(
          mem_read_address_index,
          Cat(Fill(24, io.data(31)), io.data(31, 24)),
          Array(
            0.U -> Cat(Fill(24, io.data(7)), io.data(7, 0)),
            1.U -> Cat(Fill(24, io.data(15)), io.data(15, 8)),
            2.U -> Cat(Fill(24, io.data(23)), io.data(23, 16))
          )
        ),
        InstructionsTypeL.lbu -> MuxLookup(
          mem_read_address_index,
          Cat(Fill(24, 0.U), io.data(31, 24)),
          Array(
            0.U -> Cat(Fill(24, 0.U), io.data(7, 0)),
            1.U -> Cat(Fill(24, 0.U), io.data(15, 8)),
            2.U -> Cat(Fill(24, 0.U), io.data(23, 16))
          )
        ),
        InstructionsTypeL.lh -> Mux(
          mem_read_address_index === 0.U,
          Cat(Fill(16, io.data(15)), io.data(15, 0)),
          Cat(Fill(16, io.data(31)), io.data(31, 16))
        ),
        InstructionsTypeL.lhu -> Mux(
          mem_read_address_index === 0.U,
          Cat(Fill(16, 0.U), io.data(15, 0)),
          Cat(Fill(16, 0.U), io.data(31, 16))
        ),
        InstructionsTypeL.lw -> io.data
      )
    )
  }.elsewhen(opcode === InstructionTypes.S) {
    disable_control()
    io.mem_write_address := io.op1 + io.op2
    io.mem_write_enable := true.B
    writing_mem := true.B
    when(funct3 === InstructionsTypeS.sb) {
      io.mem_write_data := MuxLookup(
        mem_write_address_index,
        Cat(io.reg2(7, 0), io.data(23, 0)),
        Array(
          0.U -> Cat(io.data(31, 8), io.reg2(7, 0)),
          1.U -> Cat(io.data(31, 16), io.reg2(7, 0), io.data(7, 0)),
          2.U -> Cat(io.data(31, 24), io.reg2(7, 0), io.data(15, 0)),
        )
      )
    }.elsewhen(funct3 === InstructionsTypeS.sh) {
      io.mem_write_data := MuxLookup(
        mem_write_address_index,
        Cat(io.reg2(15, 0), io.data(15, 0)),
        Array(
          0.U -> Cat(io.data(31, 16), io.reg2(15, 0))
        )
      )
    }.elsewhen(funct3 === InstructionsTypeS.sw) {
      io.mem_write_data := io.reg2
    }.otherwise {
      disable_memory()
      writing_mem := false.B
    }
    mem_write_address := io.mem_write_address
    mem_write_data := io.mem_write_data
  }.elsewhen(opcode === InstructionTypes.B) {
    disable_control()
    disable_memory()
    io.ctrl_jump_flag := MuxLookup(
      funct3,
      0.U,
      Array(
        InstructionsTypeB.beq -> (io.op1 === io.op2),
        InstructionsTypeB.bne -> (io.op1 =/= io.op2),
        InstructionsTypeB.bltu -> (io.op1 < io.op2),
        InstructionsTypeB.bgeu -> (io.op1 >= io.op2),
        InstructionsTypeB.blt -> (signed_op1 < signed_op2),
        InstructionsTypeB.bge -> (signed_op1 >= signed_op2)
      )
    )
    io.ctrl_jump_address := Fill(32, io.ctrl_jump_flag) & (io.op1_jump + io.op2_jump)
  }.elsewhen(opcode === Instructions.jal || opcode === Instructions.jalr) {
    disable_memory()
    disable_hold()
    io.ctrl_jump_flag := true.B
    io.ctrl_jump_address := io.op1_jump + io.op2_jump
    io.regs_write_data := io.op1 + io.op2
  }.elsewhen(opcode === Instructions.lui || opcode === Instructions.auipc) {
    disable_control()
    disable_memory()
    io.regs_write_data := io.op1 + io.op2
  }.otherwise {
    disable_control()
    when(writing_mem) {
      io.mem_write_enable := true.B
      io.mem_write_data := mem_write_data
      io.mem_write_address := mem_write_address
      writing_mem := false.B
    }.otherwise {
      disable_memory()
    }
    io.regs_write_data := 0.U
  }
}
