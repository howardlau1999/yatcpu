package riscv

import chisel3._
import chisel3.util._

class Top extends Module {
  val io = IO(new Bundle {
    val segs = Output(UInt(7.W))
    val digit_mask = Output(UInt(4.W))
  })

  val numbers = RegInit(UInt(16.W), 0.U)

  val onboard_display = Module(new OnboardDigitDisplay)

  onboard_display.io.numbers := numbers
  io.segs := onboard_display.io.segs
  io.digit_mask := onboard_display.io.digit_mask
}
