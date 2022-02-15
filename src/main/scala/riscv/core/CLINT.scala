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

package riscv.core

import chisel3._
import chisel3.util._
import riscv.Parameters

object InterruptStatus {
  val None = 0x0.U(8.W)
  val Timer0 = 0x1.U(8.W)
  val Ret = 0xFF.U(8.W)
}

object InterruptEntry {
  val Timer0 = 0x4.U(8.W)
}

object InterruptState {
  val Idle = 0x0.U
  val SyncAssert = 0x1.U
  val AsyncAssert = 0x2.U
  val MRET = 0x3.U
}

object CSRState {
  val Idle = 0x0.U
  val MSTATUS = 0x1.U
  val MEPC = 0x2.U
  val MRET = 0x3.U
  val MCAUSE = 0x4.U
}

// Core Local Interrupt Controller
class CLINT extends Module {
  val io = IO(new Bundle {
    // Interrupt signals from peripherals
    val interrupt_flag = Input(UInt(Parameters.InterruptFlagWidth))

    // Current instruction from instruction decode
    val instruction = Input(UInt(Parameters.InstructionWidth))
    val instruction_address_id = Input(UInt(Parameters.AddrWidth))

    val jump_flag = Input(Bool())
    val jump_address = Input(UInt(Parameters.AddrWidth))

    val csr_mtvec = Input(UInt(Parameters.DataWidth))
    val csr_mepc = Input(UInt(Parameters.DataWidth))
    val csr_mstatus = Input(UInt(Parameters.DataWidth))

    // Is global interrupt enabled (from MSTATUS)?
    val interrupt_enable = Input(Bool())

    val ctrl_stall_flag = Output(Bool())

    val csr_reg_write_enable = Output(Bool())
    val csr_reg_write_address = Output(UInt(Parameters.CSRRegisterAddrWidth))
    val csr_reg_write_data = Output(UInt(Parameters.DataWidth))

    val ex_interrupt_handler_address = Output(UInt(Parameters.AddrWidth))
    val ex_interrupt_assert = Output(Bool())
  })

  val interrupt_state = WireInit(0.U)
  val csr_state = RegInit(CSRState.Idle)
  val instruction_address = RegInit(UInt(Parameters.AddrWidth), 0.U)
  val cause = RegInit(UInt(Parameters.DataWidth), 0.U)
  val interrupt_assert = RegInit(Bool(), false.B)
  val interrupt_handler_address = RegInit(UInt(Parameters.AddrWidth), 0.U)
  val csr_reg_write_enable = RegInit(Bool(), false.B)
  val csr_reg_write_address = RegInit(UInt(Parameters.CSRRegisterAddrWidth), 0.U)
  val csr_reg_write_data = RegInit(UInt(Parameters.DataWidth), 0.U)

  io.ctrl_stall_flag := interrupt_state =/= InterruptState.Idle || csr_state =/= CSRState.Idle

  // Interrupt FSM
  when(io.instruction === InstructionsEnv.ecall || io.instruction === InstructionsEnv.ebreak) {
    interrupt_state := InterruptState.SyncAssert
  }.elsewhen(io.interrupt_flag =/= InterruptStatus.None && io.interrupt_enable) {
    interrupt_state := InterruptState.AsyncAssert
  }.elsewhen(io.instruction === InstructionsRet.mret) {
    interrupt_state := InterruptState.MRET
  }.otherwise {
    interrupt_state := InterruptState.Idle
  }

  // CSR FSM
  when(csr_state === CSRState.Idle) {
    when(interrupt_state === InterruptState.SyncAssert) {
      // Synchronous Interrupt
      csr_state := CSRState.MEPC
      instruction_address := Mux(
        io.jump_flag,
        io.jump_address - 4.U,
        io.instruction_address_id
      )

      cause := MuxLookup(
        io.instruction,
        10.U,
        IndexedSeq(
          InstructionsEnv.ecall -> 11.U,
          InstructionsEnv.ebreak -> 3.U,
        )
      )
    }.elsewhen(interrupt_state === InterruptState.AsyncAssert) {
      // Asynchronous Interrupt
      cause := 0x8000000BL.U
      when(io.interrupt_flag(0)) {
        cause := 0x80000007L.U
      }
      csr_state := CSRState.MEPC
      instruction_address := Mux(
        io.jump_flag,
        io.jump_address,
        io.instruction_address_id,
      )
    }.elsewhen(interrupt_state === InterruptState.MRET) {
      // Interrupt Return
      csr_state := CSRState.MRET
    }
  }.elsewhen(csr_state === CSRState.MEPC) {
    csr_state := CSRState.MSTATUS
  }.elsewhen(csr_state === CSRState.MSTATUS) {
    csr_state := CSRState.MCAUSE
  }.elsewhen(csr_state === CSRState.MCAUSE) {
    csr_state := CSRState.Idle
  }.elsewhen(csr_state === CSRState.MRET) {
    csr_state := CSRState.Idle
  }.otherwise {
    csr_state := CSRState.Idle
  }

  csr_reg_write_enable := csr_state =/= CSRState.Idle
  csr_reg_write_address := Cat(Fill(20, 0.U(1.W)), MuxLookup(
    csr_state,
    0.U(Parameters.CSRRegisterAddrWidth),
    IndexedSeq(
      CSRState.MEPC -> CSRRegister.MEPC,
      CSRState.MCAUSE -> CSRRegister.MCAUSE,
      CSRState.MSTATUS -> CSRRegister.MSTATUS,
      CSRState.MRET -> CSRRegister.MSTATUS,
    )
  ))
  csr_reg_write_data := MuxLookup(
    csr_state,
    0.U(Parameters.DataWidth),
    IndexedSeq(
      CSRState.MEPC -> instruction_address,
      CSRState.MCAUSE -> cause,
      CSRState.MSTATUS -> Cat(io.csr_mstatus(31, 4), 0.U(1.W), io.csr_mstatus(2, 0)),
      CSRState.MRET -> Cat(io.csr_mstatus(31, 4), io.csr_mstatus(7), io.csr_mstatus(2, 0)),
    )
  )
  io.csr_reg_write_enable := csr_reg_write_enable
  io.csr_reg_write_address := csr_reg_write_address
  io.csr_reg_write_data := csr_reg_write_data

  interrupt_assert := csr_state === CSRState.MCAUSE || csr_state === CSRState.MRET
  interrupt_handler_address := MuxLookup(
    csr_state,
    0.U(Parameters.AddrWidth),
    IndexedSeq(
      CSRState.MCAUSE -> io.csr_mtvec,
      CSRState.MRET -> io.csr_mepc,
    )
  )

  io.ex_interrupt_assert := interrupt_assert
  io.ex_interrupt_handler_address := interrupt_handler_address
}
