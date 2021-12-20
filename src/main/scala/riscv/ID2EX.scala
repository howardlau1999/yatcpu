package riscv

import chisel3._
import chisel3.util._

class ID2EX extends Module {
  val io = IO(new Bundle {
    val instruction = Input(UInt(32.W))
    val instruction_address = Input(UInt(32.W))
    val write_enable = Input(Bool())
    val write_address = Input(UInt(5.W))
    val reg1 = Input(UInt(32.W))
    val reg2 = Input(UInt(32.W))
    val op1 = Input(UInt(32.W))
    val op2 = Input(UInt(32.W))
    val op1_jump = Input(UInt(32.W))
    val op2_jump = Input(UInt(32.W))
    val csr_write_enable = Input(Bool())
    val csr_write_address = Input(UInt(32.W))
    val csr_read_data = Input(UInt(32.W))
    val hold_flag = Input(UInt(3.W))

    val output_instruction = Output(UInt(32.W))
    val output_instruction_address = Output(UInt(32.W))
    val output_write_enable = Output(Bool())
    val output_write_address = Output(UInt(5.W))
    val output_reg1 = Output(UInt(32.W))
    val output_reg2 = Output(UInt(32.W))
    val output_op1 = Output(UInt(32.W))
    val output_op2 = Output(UInt(32.W))
    val output_op1_jump = Output(UInt(32.W))
    val output_op2_jump = Output(UInt(32.W))
    val output_csr_write_enable = Output(Bool())
    val output_csr_write_address = Output(UInt(32.W))
    val output_csr_read_data = Output(UInt(32.W))
  })
  val hold_enable = io.hold_flag >= HoldStates.ID

  val instruction = Module(new PipelineRegister(defaultValue = 0x00000013.U))
  instruction.io.in := io.instruction
  instruction.io.hold_enable := hold_enable
  io.output_instruction := instruction.io.out

  val instruction_address = Module(new PipelineRegister(defaultValue = ProgramCounter.EntryAddress))
  instruction_address.io.in := io.instruction_address
  instruction_address.io.hold_enable := hold_enable
  io.output_instruction_address := instruction_address.io.out

  val write_enable = Module(new PipelineRegister(1))
  write_enable.io.in := io.write_enable
  write_enable.io.hold_enable := hold_enable
  io.output_write_enable := write_enable.io.out

  val write_address = Module(new PipelineRegister(5))
  write_address.io.in := io.write_address
  write_address.io.hold_enable := hold_enable
  io.output_write_address := write_address.io.out

  val reg1 = Module(new PipelineRegister())
  reg1.io.in := io.reg1
  reg1.io.hold_enable := hold_enable
  io.output_reg1 := reg1.io.out

  val reg2 = Module(new PipelineRegister())
  reg2.io.in := io.reg2
  reg2.io.hold_enable := hold_enable
  io.output_reg2 := reg2.io.out

  val op1 = Module(new PipelineRegister())
  op1.io.in := io.op1
  op1.io.hold_enable := hold_enable
  io.output_op1 := op1.io.out

  val op2 = Module(new PipelineRegister())
  op2.io.in := io.op2
  op2.io.hold_enable := hold_enable
  io.output_op2 := op2.io.out

  val op1_jump = Module(new PipelineRegister())
  op1_jump.io.in := io.op1_jump
  op1_jump.io.hold_enable := hold_enable
  io.output_op1_jump := op1_jump.io.out

  val op2_jump = Module(new PipelineRegister())
  op2_jump.io.in := io.op2_jump
  op2_jump.io.hold_enable := hold_enable
  io.output_op2_jump := op2_jump.io.out

  val csr_write_enable = Module(new PipelineRegister())
  csr_write_enable.io.in := io.csr_write_enable
  csr_write_enable.io.hold_enable := hold_enable
  io.output_csr_write_enable := csr_write_enable.io.out

  val csr_write_address = Module(new PipelineRegister())
  csr_write_address.io.in := io.csr_write_address
  csr_write_address.io.hold_enable := hold_enable
  io.output_csr_write_address := csr_write_address.io.out

  val csr_read_data = Module(new PipelineRegister())
  csr_read_data.io.in := io.csr_read_data
  csr_read_data.io.hold_enable := hold_enable
  io.output_csr_read_data := csr_read_data.io.out
}
