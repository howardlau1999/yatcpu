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

  val mem = SyncReadMem(capacity, UInt(32.W))

  mem.write((io.write_address / 4.U) & Fill(32, io.write_enable), io.write_data)

  io.read_data := mem.read(io.read_address / 4.U, true.B)
  io.debug_read_data := mem.read(io.debug_read_address / 4.U, true.B)
  io.char_read_data := mem.read(io.char_read_address / 4.U, true.B)
}
