package riscv

import chisel3._
import chisel3.util._

class OnboardDigitDisplay extends Module {
  val io = IO(new Bundle {
    val numbers = Input(UInt(16.W))

    val segs = Output(UInt(7.W))
    val digit_mask = Output(UInt(4.W))
  })

  val counter = RegInit(UInt(32.W), 0.U)
  val index = RegInit(UInt(2.W), 0.U)

  when(counter === 50000.U) {
    counter := 0.U
    index := index + 1.U
  }.otherwise {
    counter := counter + 1.U
  }

  val seg_mux = Module(new SegmentMux)
  seg_mux.io.index := index
  seg_mux.io.numbers := io.numbers

  io.segs := seg_mux.io.segs
  io.digit_mask := seg_mux.io.digit_mask
}
