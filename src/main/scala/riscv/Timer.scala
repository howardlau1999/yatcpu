package riscv

import chisel3._
import chisel3.util._

// TODO(howard): connect to the bus
// TODO(howard): allow setting different frequency
class Timer extends Module{
  val io = IO(new Bundle{
    val clear_interrupt = Input(Bool())
    val signal_interrupt = Output(Bool())
  })
  val status = RegInit(0.U(3.W))
  val count = RegInit(0.U(32.W))
  val limit = 100000000.U(32.W)

  io.signal_interrupt := status === 1.U

  when(io.clear_interrupt) {
    status := 0.U
  }

  when(count >= limit) {
    count := 0.U
    status := 1.U
  }.otherwise {
    count := count + 1.U
  }
}
