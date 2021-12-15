package riscv

import chisel3._
import chisel3.util._

class DataMemory extends Module {
  val io = IO(new Bundle{
    val read_address = Input(UInt(32.W))
    val write_address = Input(UInt(32.W))
    val write_enable = Input(Bool())
    val write_data = Input(UInt(32.W))

    val read_data = Output(UInt(32.W))
  })
}
