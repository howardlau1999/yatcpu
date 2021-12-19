package riscv

import chisel3._
import chisel3.util._

object InterruptState {
  val Idle, SyncAssert, AsyncAssert, MRET = Enum(4)
}

object CSRState {
  val Idle, MSTATUS, MEPC, MRET, MCAUSE = Enum(5)
}

class CLINT extends Module {
  val io = IO(new Bundle {
    val interrupt_flag = Input(UInt(8.W))

    val instruction = Input(UInt(32.W))
    val instruction_address = Input(UInt(32.W))

    val jump_flag = Input(Bool())
    val jump_address = Input(UInt(32.W))

    val hold_flag = Input(UInt(3.W))

    val csr_reg_data = Input(UInt(32.W))
    val csr_mtvec = Input(UInt(32.W))
    val csr_mepc = Input(UInt(32.W))
    val csr_mstatus = Input(UInt(32.W))

    val interrupt_enable = Input(Bool())

    val ctrl_hold_flag = Output(Bool())

    val csr_reg_write_enable = Output(Bool())
    val csr_reg_read_address = Output(UInt(32.W))
    val csr_reg_write_address = Output(UInt(32.W))
    val csr_reg_write_data = Output(UInt(32.W))

    val ex_interrupt_handler_address = Output(UInt(32.W))
    val ex_interrupt_assert = Output(Bool())
  })

  val interrupt_state = RegInit(0.U)
  val csr_state = RegInit(0.U)

}
