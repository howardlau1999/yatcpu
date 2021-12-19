package riscv

import chisel3._
import chisel3.util._

class IF2ID extends Module {
  val io = IO(new Bundle {
    val instruction = Input(UInt(32.W))
    val instruction_address = Input(UInt(32.W))
    val hold_flag = Input(UInt(3.W))

    val output_instruction = Output(UInt(32.W))
    val output_instruction_address = Output(UInt(32.W))
  })

  val hold_enable = io.hold_flag >= HoldStates.IF.id.U

  val instruction = Module(new PipelineRegister(defaultValue = 0x00000013.U))
  instruction.io.in := io.instruction
  instruction.io.hold_enable := hold_enable
  io.output_instruction := instruction.io.out

  val instruction_address = Module(new PipelineRegister())
  instruction_address.io.in := io.instruction_address
  instruction_address.io.hold_enable := hold_enable
  io.output_instruction_address := instruction_address.io.out
}
