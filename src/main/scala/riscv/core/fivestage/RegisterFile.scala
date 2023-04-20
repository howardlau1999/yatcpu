// Copyright 2021 Howard Lau
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package riscv.core.fivestage

import chisel3._
import chisel3.util._
import riscv.Parameters

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
    val write_address = Input(UInt(Parameters.PhysicalRegisterAddrWidth))
    val write_data = Input(UInt(Parameters.DataWidth))

    val read_address1 = Input(UInt(Parameters.PhysicalRegisterAddrWidth))
    val read_address2 = Input(UInt(Parameters.PhysicalRegisterAddrWidth))
    val read_data1 = Output(UInt(Parameters.DataWidth))
    val read_data2 = Output(UInt(Parameters.DataWidth))

    val debug_read_address = Input(UInt(Parameters.PhysicalRegisterAddrWidth))
    val debug_read_data = Output(UInt(Parameters.DataWidth))
  })
  val registers = Reg(Vec(Parameters.PhysicalRegisters, UInt(Parameters.DataWidth)))

  when(!reset.asBool) {
    when(io.write_enable && io.write_address =/= 0.U) {
      registers(io.write_address) := io.write_data
    }
  }

  io.read_data1 := MuxCase(
    registers(io.read_address1),
    IndexedSeq(
      (io.read_address1 === 0.U) -> 0.U,
      (io.read_address1 === io.write_address && io.write_enable) -> io.write_data
    )
  )

  io.read_data2 := MuxCase(
    registers(io.read_address2),
    IndexedSeq(
      (io.read_address2 === 0.U) -> 0.U,
      (io.read_address2 === io.write_address && io.write_enable) -> io.write_data
    )
  )

  io.debug_read_data := MuxCase(
    registers(io.debug_read_address),
    IndexedSeq(
      (io.debug_read_address === 0.U) -> 0.U,
      (io.debug_read_address === io.write_address && io.write_enable) -> io.write_data
    )
  )

}
