package riscv

import chisel3._
import chisel3.util._

object Registers extends Enumeration {
  type Register = Value
  val zero,
  ra, sp, gp, tp,
  t0, t1, t2, fp,
  s1,
  a0, a1, a2, a3, a4, a5, a6, a7,
  s2, s3, s4, s5, s6, s7, s8, s9, s10, s11,
  t3, t4, t5, t6 = Value
}

class RegisterFile extends Module {
  val io = IO(new Bundle {
    val write_enable = Input(Bool())
    val write_address = Input(UInt(32.W))
    val write_data = Input(UInt(32.W))

    val read_address1 = Input(UInt(32.W))
    val read_address2 = Input(UInt(32.W))
    val read_data1 = Output(UInt(32.W))
    val read_data2 = Output(UInt(32.W))

    val debug_read_address = Input(UInt(32.W))
    val debug_read_data = Output(UInt(32.W))
  })
  val registers = RegInit(VecInit(Seq.fill(32)(0.U(32.W))))

  when(!reset.asBool()) {
    when(io.write_enable && io.write_address =/= 0.U) {
      registers(io.write_address) := io.write_data
    }
  }

  io.read_data1 := MuxCase(
    registers(io.read_address1),
    Array(
      (io.read_address1 === 0.U) -> 0.U,
      (io.read_address1 === io.write_address && io.write_enable) -> io.write_data
    )
  )

  io.read_data2 := MuxCase(
    registers(io.read_address2),
    Array(
      (io.read_address2 === 0.U) -> 0.U,
      (io.read_address2 === io.write_address && io.write_enable) -> io.write_data
    )
  )

  io.debug_read_data := MuxCase(
    registers(io.debug_read_address),
    Array(
      (io.debug_read_address === 0.U) -> 0.U,
      (io.debug_read_address === io.write_address && io.write_enable) -> io.write_data
    )
  )

}
