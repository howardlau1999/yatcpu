package riscv

import chisel3._
import chisel3.util._

class BCD2Segments extends Module {
  val io = IO(new Bundle {
    val bcd = Input(UInt(4.W))
    val segs = Output(UInt(7.W))
  })

  val bcd = io.bcd
  val segs = Wire(UInt(7.W))

  segs := MuxLookup(
    bcd,
    0xFF.U,
    Array(
      0.U -> 0x01.U,
      1.U -> 0x4F.U,
      2.U -> 0x12.U,
      3.U -> 0x06.U,
      4.U -> 0x4C.U,
      5.U -> 0x24.U,
      6.U -> 0x20.U,
      7.U -> 0x0F.U,
      8.U -> 0x00.U,
      9.U -> 0x04.U,
      10.U -> 0x08.U,
      11.U -> 0x60.U,
      12.U -> 0x72.U,
      13.U -> 0x42.U,
      14.U -> 0x30.U,
      15.U -> 0x38.U,
    )
  )

  io.segs := segs
}

