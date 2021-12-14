package riscv

import chisel3._
import chisel3.util._

object HoldStates extends Enumeration {
  type HoldState = Value

  val None, PC, IF, ID = Value
}

class Control extends Module {
  val io = IO(new Bundle {
    val jump_flag = Input(Bool())
    val hold_flag_ex = Input(Bool())
    val jump_address = Input(UInt(32.W))

    val output_hold_flag = Output(UInt(3.W))

    val pc_jump_flag = Output(Bool())
    val pc_jump_address = Output(UInt(32.W))
  })

  io.pc_jump_flag := io.jump_flag
  io.pc_jump_address := io.jump_address

  io.output_hold_flag := HoldStates.None.id.U

  when(io.jump_flag || io.hold_flag_ex) {
    io.output_hold_flag := HoldStates.ID.id.U
  }
}
