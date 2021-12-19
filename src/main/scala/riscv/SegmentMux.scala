package riscv

import chisel3._
import chisel3.util._

class SegmentMux extends Module {
  val io = IO(new Bundle {
    val index = Input(UInt(2.W))
    val numbers = Input(UInt(16.W))

    val segs = Output(UInt(7.W))
    val digit_mask = Output(UInt(4.W))
  })


  val digit = RegInit(UInt(4.W), 0.U)
  val bcd2segs = Module(new BCD2Segments)

  bcd2segs.io.bcd := digit
  io.segs := bcd2segs.io.segs
  digit := MuxLookup(
    io.index,
    io.numbers(3, 0), // 0.U
    Array(
      1.U -> io.numbers(7, 4),
      2.U -> io.numbers(11, 8),
      3.U -> io.numbers(15, 12)
    )
  )

  val s0 = io.index(0).asBool()
  val s1 = io.index(1).asBool()

  io.digit_mask := Cat(!(s0 && s1), s0 || !s1, !s0 || s1, s0 || s1)
}
