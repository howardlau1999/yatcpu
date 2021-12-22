package riscv

import chisel3._
import chisel3.util._

object InterruptStatus extends Bundle {
  val None = 0x0.U(8.W)
  val Timer0 = 0x1.U(8.W)
  val Ret = 0xFF.U(8.W)
}

object InterruptEntry extends Bundle {
  val Timer0 = 0x4.U(8.W)
}

object InterruptState extends Bundle {
  val Idle = 0x0.U
  val SyncAssert = 0x1.U
  val AsyncAssert = 0x2.U
  val MRET = 0x3.U
}

object CSRState extends Bundle {
  val Idle = 0x0.U
  val MSTATUS = 0x1.U
  val MEPC = 0x2.U
  val MRET = 0x3.U
  val MCAUSE = 0x4.U
}

class CLINT extends Module {
  val io = IO(new Bundle {
    // Interrupt signals from peripherals
    val interrupt_flag = Input(UInt(8.W))

    // Current instruction from instruction decode
    val instruction = Input(UInt(32.W))
    val instruction_address_id = Input(UInt(32.W))

    val jump_flag = Input(Bool())
    val jump_address = Input(UInt(32.W))

    val csr_mtvec = Input(UInt(32.W))
    val csr_mepc = Input(UInt(32.W))
    val csr_mstatus = Input(UInt(32.W))

    // Is global interrupt enabled (from MSTATUS)?
    val interrupt_enable = Input(Bool())

    val ctrl_hold_flag = Output(Bool())

    val csr_reg_write_enable = Output(Bool())
    val csr_reg_write_address = Output(UInt(32.W))
    val csr_reg_write_data = Output(UInt(32.W))

    val ex_interrupt_handler_address = Output(UInt(32.W))
    val ex_interrupt_assert = Output(Bool())
  })

  val interrupt_state = Wire(UInt(32.W))
  val csr_state = RegInit(CSRState.Idle)
  val instruction_address = RegInit(UInt(32.W), 0.U)
  val cause = RegInit(UInt(32.W), 0.U)
  val interrupt_assert = RegInit(Bool(), false.B)
  val interrupt_handler_address = RegInit(UInt(32.W), 0.U)
  val csr_reg_write_enable = RegInit(Bool(), false.B)
  val csr_reg_write_address = RegInit(UInt(32.W), 0.U)
  val csr_reg_write_data = RegInit(UInt(32.W), 0.U)

  io.ctrl_hold_flag := interrupt_state =/= InterruptState.Idle || csr_state =/= CSRState.Idle

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
        Array(
          InstructionsEnv.ecall -> 11.U,
          InstructionsEnv.ebreak -> 3.U,
        )
      )
    }.elsewhen(interrupt_state === InterruptState.AsyncAssert) {
      // Asynchronous Interrupt
      cause := 0x80000004L.U
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
    0.U(12.W),
    Array(
      CSRState.MEPC -> CSRRegister.MEPC,
      CSRState.MCAUSE -> CSRRegister.MCAUSE,
      CSRState.MSTATUS -> CSRRegister.MSTATUS,
      CSRState.MRET -> CSRRegister.MSTATUS,
    )
  ))
  csr_reg_write_data := MuxLookup(
    csr_state,
    0.U(32.W),
    Array(
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
    0.U(32.W),
    Array(
      CSRState.MCAUSE -> io.csr_mtvec,
      CSRState.MRET -> io.csr_mepc,
    )
  )

  io.ex_interrupt_assert := interrupt_assert
  io.ex_interrupt_handler_address := interrupt_handler_address
}
