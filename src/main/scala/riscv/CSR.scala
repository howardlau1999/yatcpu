package riscv

import chisel3._
import chisel3.util._

object CSRInstruction {
  val csrrw = 0x1.U(3.W)
  val csrrs = 0x2.U(3.W)
  val csrrc = 0x3.U(3.W)
  val csrrwi = 0x5.U(3.W)
  val csrrsi = 0x6.U(3.W)
  val csrrci = 0x7.U(3.W)
}

object CSRRegister {
  val CycleL = 0xc00.U(12.W)
  val CycleH = 0xc80.U(12.W)
  val MTVEC = 0x305.U(12.W)
  val MCAUSE = 0x342.U(12.W)
  val MEPC = 0x341.U(12.W)
  val MIE = 0x304.U(12.W)
  val MSTATUS = 0x300.U(12.W)
  val MSCRATCH = 0x340.U(12.W)
}

class CSR extends Module {
  val io = IO(new Bundle {
    val reg_write_enable_ex = Input(Bool())
    val reg_read_address_ex = Input(UInt(32.W))
    val reg_write_address_ex = Input(UInt(32.W))
    val reg_write_data_ex = Input(UInt(32.W))

    val reg_write_enable_clint = Input(Bool())
    val reg_read_address_clint = Input(UInt(32.W))
    val reg_write_address_clint = Input(UInt(32.W))
    val reg_write_data_clint = Input(UInt(32.W))

    val interrupt_enable = Input(Bool())

    val ex_reg_data = Output(UInt(32.W))

    val clint_reg_data = Output(UInt(32.W))
    val clint_csr_mtvec = Output(UInt(32.W))
    val clint_csr_mepc = Output(UInt(32.W))
    val clint_csr_mstatus = Output(UInt(32.W))
  })


  val cycles = RegInit(UInt(64.W), 0.U)
  val mtvec = RegInit(UInt(32.W), 0.U)
  val mcause = RegInit(UInt(32.W), 0.U)
  val mepc = RegInit(UInt(32.W), 0.U)
  val mie = RegInit(UInt(32.W), 0.U)
  val mstatus = RegInit(UInt(32.W), 0.U)
  val mscratch = RegInit(UInt(32.W), 0.U)

  cycles := cycles + 1.U
  io.clint_csr_mtvec := mtvec
  io.clint_csr_mepc := mepc
  io.clint_csr_mstatus := mstatus
  io.interrupt_enable := mscratch(3) === 1.U

  val reg_write_address = Wire(UInt(32.W))
  val reg_write_data = Wire(UInt(32.W))

  val reg_read_address = Wire(UInt(32.W))
  val reg_read_data = Wire(UInt(32.W))
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
  }

  val regLUT =
    Array(
      CSRRegister.CycleL -> cycles(31, 0),
      CSRRegister.CycleH -> cycles(63, 32),
      CSRRegister.MTVEC -> mtvec,
      CSRRegister.MCAUSE -> mcause,
      CSRRegister.MEPC -> mepc,
      CSRRegister.MIE -> mie,
      CSRRegister.MSTATUS -> mstatus,
      CSRRegister.MSCRATCH -> mscratch,
    )

  io.ex_reg_data := MuxLookup(
    io.reg_read_address_ex,
    0.U,
    regLUT,
  )

  io.clint_reg_data := MuxLookup(
    io.reg_read_address_clint,
    0.U,
    regLUT,
  )
}
