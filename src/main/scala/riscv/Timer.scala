package riscv

import chisel3._
import chisel3.util._

// TODO(howard): connect to the bus
// TODO(howard): allow setting different frequency
class Timer extends Module{
  val io = IO(new Bundle{
    val signal_interrupt = Output(Bool())
  })
  val count = RegInit(0.U(32.W))
  val limit = 100000000.U(32.W)

  io.signal_interrupt := count >= (limit - 10.U)

  when(count >= limit) {
    count := 0.U
  }.otherwise {
    count := count + 1.U
  }
}
