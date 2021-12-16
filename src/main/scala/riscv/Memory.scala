package riscv

import chisel3._
import chisel3.util._

class Memory(capacity: Int) extends Module {
  val io = IO(new Bundle {
    val read_address = Input(UInt(32.W))
    val debug_read_address = Input(UInt(32.W))
    val char_read_address = Input(UInt(32.W))

    val write_address = Input(UInt(32.W))
    val write_enable = Input(Bool())
    val write_data = Input(UInt(32.W))

    val read_data = Output(UInt(32.W))
    val debug_read_data = Output(UInt(32.W))
    val char_read_data = Output(UInt(32.W))
  })

  val data = Reg(Vec(capacity, UInt(32.W)))
  when(io.write_enable) {
    data(io.write_address) := io.write_data
  }
  io.read_data := data(io.read_address)
  io.debug_read_data := data(io.debug_read_address)
  io.char_read_data := data(io.char_read_address)
}
