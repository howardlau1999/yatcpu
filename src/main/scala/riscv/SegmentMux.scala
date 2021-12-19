package riscv

import chisel3._
import chisel3.util._

class SegmentMux extends Module {
  val io = IO(new Bundle {
    val digit_mask = Input(UInt(4.W))
    val numbers = Input(UInt(16.W))
    val segs = Output(UInt(8.W))
  })


  val digit = RegInit(UInt(4.W), 0.U)
  val bcd2segs = Module(new BCD2Segments)

  bcd2segs.io.bcd := digit
  io.segs := bcd2segs.io.segs
  digit := MuxLookup(
    io.digit_mask,
    io.numbers(3, 0), // "b1110".U
    Array(
      "b1101".U -> io.numbers(7, 4),
      "b1011".U -> io.numbers(11, 8),
      "b0111".U -> io.numbers(15, 12)
    )
  )
}
