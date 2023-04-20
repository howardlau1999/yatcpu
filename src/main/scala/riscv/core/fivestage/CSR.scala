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


object CSRRegister {
  // Refer to Spec. Vol.II Page 8-10
  val CycleL = 0xc00.U(Parameters.CSRRegisterAddrWidth)
  val CycleH = 0xc80.U(Parameters.CSRRegisterAddrWidth)
  val MTVEC = 0x305.U(Parameters.CSRRegisterAddrWidth)
  val MCAUSE = 0x342.U(Parameters.CSRRegisterAddrWidth)
  val MEPC = 0x341.U(Parameters.CSRRegisterAddrWidth)
  val MIE = 0x304.U(Parameters.CSRRegisterAddrWidth)
  val MSTATUS = 0x300.U(Parameters.CSRRegisterAddrWidth)
  val MSCRATCH = 0x340.U(Parameters.CSRRegisterAddrWidth)
  val MTVAL = 0x343.U(Parameters.CSRRegisterAddrWidth)
  val SATP = 0x180.U(Parameters.CSRRegisterAddrWidth)
}

class CSR extends Module {
  val io = IO(new Bundle {
    val reg_write_enable_ex = Input(Bool())
    val reg_read_address_id = Input(UInt(Parameters.CSRRegisterAddrWidth))
    val reg_write_address_ex = Input(UInt(Parameters.CSRRegisterAddrWidth))
    val reg_write_data_ex = Input(UInt(Parameters.DataWidth))

    val reg_write_enable_clint = Input(Bool())
    val reg_read_address_clint = Input(UInt(Parameters.CSRRegisterAddrWidth))
    val reg_write_address_clint = Input(UInt(Parameters.CSRRegisterAddrWidth))
    val reg_write_data_clint = Input(UInt(Parameters.DataWidth))

    val interrupt_enable = Output(Bool())
    val mmu_enable = Output(Bool())
    val id_reg_data = Output(UInt(Parameters.DataWidth))

    val start_paging = Output(Bool())

    val clint_reg_data = Output(UInt(Parameters.DataWidth))
    val clint_csr_mtvec = Output(UInt(Parameters.DataWidth))
    val clint_csr_mepc = Output(UInt(Parameters.DataWidth))
    val clint_csr_mstatus = Output(UInt(Parameters.DataWidth))
    val mmu_csr_satp = Output(UInt(Parameters.DataWidth))
  })


  val cycles = RegInit(UInt(64.W), 0.U)
  val mtvec = RegInit(UInt(Parameters.DataWidth), 0.U)
  val mcause = RegInit(UInt(Parameters.DataWidth), 0.U)
  val mepc = RegInit(UInt(Parameters.DataWidth), 0.U)
  val mie = RegInit(UInt(Parameters.DataWidth), 0.U)
  val mstatus = RegInit(UInt(Parameters.DataWidth), 0.U)
  val mscratch = RegInit(UInt(Parameters.DataWidth), 0.U)
  val mtval = RegInit(UInt(Parameters.DataWidth), 0.U)
  val satp = RegInit(UInt(Parameters.DataWidth), 0.U)

  cycles := cycles + 1.U
  io.clint_csr_mtvec := mtvec
  io.clint_csr_mepc := mepc
  io.clint_csr_mstatus := mstatus
  io.interrupt_enable := mstatus(3) === 1.U
  io.mmu_csr_satp := satp
  io.mmu_enable := satp(31) === 1.U
  io.start_paging := false.B

  val reg_write_address = Wire(UInt(Parameters.CSRRegisterAddrWidth))
  val reg_write_data = Wire(UInt(Parameters.DataWidth))
  reg_write_address := 0.U
  reg_write_data := 0.U

  val reg_read_address = Wire(UInt(Parameters.CSRRegisterAddrWidth))
  val reg_read_data = Wire(UInt(Parameters.DataWidth))
  reg_read_address := 0.U
  reg_read_data := 0.U

  when(io.reg_write_enable_ex) {
    reg_write_address := io.reg_write_address_ex(11, 0)
    reg_write_data := io.reg_write_data_ex
  }.elsewhen(io.reg_write_enable_clint) {
    reg_write_address := io.reg_write_address_clint(11, 0)
    reg_write_data := io.reg_write_data_clint
  }

  when(reg_write_address === CSRRegister.MTVEC) {
    mtvec := reg_write_data
  }.elsewhen(reg_write_address === CSRRegister.MCAUSE) {
    mcause := reg_write_data
  }.elsewhen(reg_write_address === CSRRegister.MEPC) {
    mepc := reg_write_data
  }.elsewhen(reg_write_address === CSRRegister.MIE) {
    mie := reg_write_data
  }.elsewhen(reg_write_address === CSRRegister.MSTATUS) {
    mstatus := reg_write_data
  }.elsewhen(reg_write_address === CSRRegister.MSCRATCH) {
    mscratch := reg_write_data
  }.elsewhen(reg_write_address === CSRRegister.MTVAL) {
    mtval := reg_write_data
  }.elsewhen(reg_write_address === CSRRegister.SATP) {
    satp := reg_write_data
    when(reg_write_data(31) === 1.U && satp(31) === 0.U) {
      io.start_paging := true.B
    }
  }

  val regLUT =
    IndexedSeq(
      CSRRegister.CycleL -> cycles(31, 0),
      CSRRegister.CycleH -> cycles(63, 32),
      CSRRegister.MTVEC -> mtvec,
      CSRRegister.MCAUSE -> mcause,
      CSRRegister.MEPC -> mepc,
      CSRRegister.MIE -> mie,
      CSRRegister.MSTATUS -> mstatus,
      CSRRegister.MSCRATCH -> mscratch,
      CSRRegister.MTVAL -> mtval,
      CSRRegister.SATP -> satp,
    )

  io.id_reg_data := MuxLookup(io.reg_read_address_id, 0.U)(
    regLUT
  )

  io.clint_reg_data := MuxLookup(io.reg_read_address_clint, 0.U)(
    regLUT
  )
}
