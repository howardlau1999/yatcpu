package riscv

import chisel3._
import chisel3.util._

class OnboardDigitDisplay extends Module {
  val io = IO(new Bundle {
    val digit_mask = Output(UInt(4.W))
  })

  val counter = RegInit(UInt(16.W), 0.U)
  val digit_mask = RegInit(UInt(4.W), "b0111".U)

  counter := counter + 1.U
  when(counter === 0.U) {
    digit_mask := (digit_mask << 1) + digit_mask(3)
  }
  io.digit_mask := digit_mask
}
