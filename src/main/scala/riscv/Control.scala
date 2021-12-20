package riscv

import chisel3._
import chisel3.util._

object HoldStates {
  val None = 0.U
  val PC = 1.U
  val IF = 2.U
  val ID = 3.U
}

class Control extends Module {
  val io = IO(new Bundle {
    val jump_flag = Input(Bool())
    val hold_flag_if = Input(Bool())
    val hold_flag_id = Input(Bool())
    val hold_flag_ex = Input(Bool())
    val hold_flag_clint = Input(Bool())
    val jump_address = Input(UInt(32.W))

    val output_hold_flag = Output(UInt(3.W))

    val pc_jump_flag = Output(Bool())
    val pc_jump_address = Output(UInt(32.W))
  })

  io.pc_jump_flag := io.jump_flag
  io.pc_jump_address := io.jump_address

  io.output_hold_flag := MuxCase(
    HoldStates.None,
    Array(
      io.hold_flag_if -> HoldStates.IF,
      io.hold_flag_id -> HoldStates.IF,
      (io.jump_flag || io.hold_flag_ex || io.hold_flag_clint) -> HoldStates.ID,
    )
  )
}
