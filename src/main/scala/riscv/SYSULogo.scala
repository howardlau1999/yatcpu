package riscv

import chisel3._
import chisel3.util._

class SYSULogo extends Module {
  val io = IO(new Bundle {
    val digit_mask = Input(UInt(4.W))
    val segs = Output(UInt(8.W))
  })

  io.segs := MuxLookup(
    io.digit_mask,
    "b00100100".U, // "b0111".U, "b1101".U -> S
    Array(
      "b1011".U -> "b01000100".U, // Y
      "b1110".U -> "b01000001".U, // U
    )
  )
}
