package riscv

import chisel3._
import chisel3.stage.ChiselStage

class ProgramCounter extends Module {
  val io = IO(new Bundle {
    val jump_enable = Input(Bool())
    val jump_address = Input(UInt(32.W))
    val hold_flag = Input(UInt(3.W))

    val pc = Output(UInt(32.W))
  })

  val pc = RegInit(0.U(32.W))

  when(io.jump_enable) {
    pc := io.jump_address
  }.elsewhen(io.hold_flag >= HoldStates.PC.id.U) {
    pc := pc
  }.otherwise {
    pc := pc + 4.U
  }

  io.pc := pc
}