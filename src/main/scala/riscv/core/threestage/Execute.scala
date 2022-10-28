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

package riscv.core.threestage

import chisel3._
import chisel3.experimental.ChiselEnum
import chisel3.util._
import riscv.Parameters
import riscv.core.BusBundle

object MemoryAccessStates extends ChiselEnum {
  val Idle, Read, Write, ReadWrite = Value
}

class Execute extends Module {
  val io = IO(new Bundle {
    val instruction = Input(UInt(Parameters.InstructionWidth))
    val instruction_address = Input(UInt(Parameters.AddrWidth))
    val interrupt_assert = Input(Bool())
    val interrupt_handler_address = Input(UInt(Parameters.AddrWidth))
    val regs_write_enable_id = Input(Bool())
    val regs_write_address_id = Input(UInt(Parameters.PhysicalRegisterAddrWidth))
    val csr_reg_write_enable_id = Input(Bool())
    val csr_reg_write_address_id = Input(UInt(Parameters.CSRRegisterAddrWidth))
    val csr_reg_data_id = Input(UInt(Parameters.DataWidth))
    val reg1_data = Input(UInt(Parameters.DataWidth))
    val reg2_data = Input(UInt(Parameters.DataWidth))
    val op1 = Input(UInt(Parameters.DataWidth))
    val op2 = Input(UInt(Parameters.DataWidth))
    val op1_jump = Input(UInt(Parameters.DataWidth))
    val op2_jump = Input(UInt(Parameters.DataWidth))

    val bus = new BusBundle

    val regs_write_enable = Output(Bool())
    val regs_write_address = Output(UInt(Parameters.PhysicalRegisterAddrWidth))
    val regs_write_data = Output(UInt(Parameters.DataWidth))

    val ctrl_stall_flag = Output(Bool())
    val ctrl_jump_flag = Output(Bool())
    val ctrl_jump_address = Output(UInt(Parameters.AddrWidth))

    val csr_reg_write_enable = Output(Bool())
    val csr_reg_write_address = Output(UInt(Parameters.CSRRegisterAddrWidth))
    val csr_reg_write_data = Output(UInt(Parameters.DataWidth))
  })

  val opcode = io.instruction(6, 0)
  val funct3 = io.instruction(14, 12)
  val funct7 = io.instruction(31, 25)
  val rd = io.instruction(11, 7)
  val uimm = io.instruction(19, 15)

  val alu = Module(new ALU)
  val alu_ctrl = Module(new ALUControl)

  alu_ctrl.io.opcode := opcode
  alu_ctrl.io.funct3 := funct3
  alu_ctrl.io.funct7 := funct7
  alu.io.func := alu_ctrl.io.alu_funct
  alu.io.op1 := io.op1
  alu.io.op2 := io.op2


  val mem_read_address_index = (io.op1 + io.op2) (log2Up(Parameters.WordSize) - 1, 0).asUInt
  val mem_write_address_index = (io.op1 + io.op2) (log2Up(Parameters.WordSize) - 1, 0).asUInt
  val mem_access_state = RegInit(MemoryAccessStates.Idle)
  val pending_interrupt = RegInit(false.B)
  val pending_interrupt_handler_address = RegInit(Parameters.EntryAddress)

  val jump_flag = Wire(Bool())
  val jump_address = Wire(UInt(Parameters.AddrWidth))

  io.ctrl_jump_flag := jump_flag || io.interrupt_assert
  io.ctrl_jump_address := Mux(io.interrupt_assert, io.interrupt_handler_address, jump_address)

  io.bus.read := false.B
  io.bus.address := 0.U
  io.bus.write_data := 0.U
  io.bus.write_strobe := VecInit(Seq.fill(Parameters.WordSize)(false.B))
  io.bus.write := false.B
  io.regs_write_enable := io.regs_write_enable_id && !io.interrupt_assert
  io.regs_write_address := io.regs_write_address_id
  io.regs_write_data := 0.U
  io.csr_reg_write_enable := io.csr_reg_write_enable_id && !io.interrupt_assert
  io.csr_reg_write_address := io.csr_reg_write_address_id
  io.csr_reg_write_data := 0.U
  io.bus.request := false.B

  def disable_control() = {
    disable_stall()
    disable_jump()
  }

  def disable_stall() = {
    io.ctrl_stall_flag := false.B
  }

  def disable_jump() = {
    jump_address := 0.U
    jump_flag := false.B
  }

  def check_interrupt_during_bus_transaction() = {
    // Store the interrupt and process later
    when(io.interrupt_assert) {
      pending_interrupt := true.B
      pending_interrupt_handler_address := io.interrupt_handler_address
      io.ctrl_jump_flag := false.B
    }
  }

  def on_bus_transaction_finished() = {
    mem_access_state := MemoryAccessStates.Idle
    io.ctrl_stall_flag := false.B
    when(pending_interrupt) {
      pending_interrupt := false.B
      io.ctrl_jump_flag := true.B
      io.ctrl_jump_address := pending_interrupt_handler_address
    }
  }

  when(opcode === InstructionTypes.I) {
    disable_control()
    val mask = (0xFFFFFFFFL.U >> io.instruction(24, 20)).asUInt
    io.regs_write_data := alu.io.result
    when(funct3 === InstructionsTypeI.sri) {
      when(funct7(5).asBool()) {
        io.regs_write_data := alu.io.result & mask |
          (Fill(32, io.op1(31)) & (~mask).asUInt).asUInt
      }
    }
  }.elsewhen(opcode === InstructionTypes.RM) {
    disable_control()
    // TODO(howard): support mul and div
    when(funct7 === 0.U || funct7 === 0x20.U) {
      val mask = (0xFFFFFFFFL.U >> io.reg2_data(4, 0)).asUInt
      io.regs_write_data := alu.io.result
      when(funct3 === InstructionsTypeR.sr) {
        when(funct7(5).asBool()) {
          io.regs_write_data := alu.io.result & mask |
            (Fill(32, io.op1(31)) & (~mask).asUInt).asUInt
        }
      }
    }
  }.elsewhen(opcode === InstructionTypes.L) {
    disable_control()

    when(mem_access_state === MemoryAccessStates.Idle) {
      // Start the read transaction when there is no interrupt asserted
      // and the bus is available
      when(!io.interrupt_assert) {
        io.ctrl_stall_flag := true.B
        io.regs_write_enable := false.B
        io.bus.read := true.B
        io.bus.address := io.op1 + io.op2
        io.bus.request := true.B
        when(io.bus.granted) {
          mem_access_state := MemoryAccessStates.Read
        }
      }
    }.elsewhen(mem_access_state === MemoryAccessStates.Read) {
      check_interrupt_during_bus_transaction()
      io.bus.request := true.B
      io.bus.read := false.B
      io.ctrl_stall_flag := true.B
      io.bus.address := io.op1 + io.op2
      when(io.bus.read_valid) {
        io.regs_write_enable := true.B
        val data = io.bus.read_data
        io.regs_write_data := MuxLookup(
          funct3,
          0.U,
          IndexedSeq(
            InstructionsTypeL.lb -> MuxLookup(
              mem_read_address_index,
              Cat(Fill(24, data(31)), data(31, 24)),
              IndexedSeq(
                0.U -> Cat(Fill(24, data(7)), data(7, 0)),
                1.U -> Cat(Fill(24, data(15)), data(15, 8)),
                2.U -> Cat(Fill(24, data(23)), data(23, 16))
              )
            ),
            InstructionsTypeL.lbu -> MuxLookup(
              mem_read_address_index,
              Cat(Fill(24, 0.U), data(31, 24)),
              IndexedSeq(
                0.U -> Cat(Fill(24, 0.U), data(7, 0)),
                1.U -> Cat(Fill(24, 0.U), data(15, 8)),
                2.U -> Cat(Fill(24, 0.U), data(23, 16))
              )
            ),
            InstructionsTypeL.lh -> Mux(
              mem_read_address_index === 0.U,
              Cat(Fill(16, data(15)), data(15, 0)),
              Cat(Fill(16, data(31)), data(31, 16))
            ),
            InstructionsTypeL.lhu -> Mux(
              mem_read_address_index === 0.U,
              Cat(Fill(16, 0.U), data(15, 0)),
              Cat(Fill(16, 0.U), data(31, 16))
            ),
            InstructionsTypeL.lw -> data
          )
        )
        on_bus_transaction_finished()
      }
    }
  }.elsewhen(opcode === InstructionTypes.S) {
    disable_control()

    when(mem_access_state === MemoryAccessStates.Idle) {
      // Start the write transaction when there is no interrupt asserted
      // and the bus is available
      when(!io.interrupt_assert) {
        io.ctrl_stall_flag := true.B
        io.bus.address := io.op1 + io.op2
        io.bus.write_data := io.reg2_data
        io.bus.write := true.B
        io.bus.write_strobe := VecInit(Seq.fill(Parameters.WordSize)(false.B))
        when(funct3 === InstructionsTypeS.sb) {
          io.bus.write_strobe(mem_write_address_index) := true.B
          io.bus.write_data := io.reg2_data(Parameters.ByteBits, 0) << (mem_write_address_index << log2Up(Parameters
            .ByteBits).U)
        }.elsewhen(funct3 === InstructionsTypeS.sh) {
          when(mem_write_address_index === 0.U) {
            for (i <- 0 until Parameters.WordSize / 2) {
              io.bus.write_strobe(i) := true.B
            }
            io.bus.write_data := io.reg2_data(Parameters.WordSize / 2 * Parameters.ByteBits, 0)
          }.otherwise {
            for (i <- Parameters.WordSize / 2 until Parameters.WordSize) {
              io.bus.write_strobe(i) := true.B
            }
            io.bus.write_data := io.reg2_data(Parameters.WordSize / 2 * Parameters.ByteBits, 0) << (Parameters
              .WordSize / 2 * Parameters.ByteBits)
          }
        }.elsewhen(funct3 === InstructionsTypeS.sw) {
          for (i <- 0 until Parameters.WordSize) {
            io.bus.write_strobe(i) := true.B
          }
        }
        io.bus.request := true.B
        when(io.bus.granted) {
          mem_access_state := MemoryAccessStates.Write
        }
      }
    }.elsewhen(mem_access_state === MemoryAccessStates.Write) {
      check_interrupt_during_bus_transaction()
      io.bus.request := true.B
      io.ctrl_stall_flag := true.B
      io.bus.write := false.B
      io.bus.address := io.op1 + io.op2
      when(io.bus.write_valid) {
        on_bus_transaction_finished()
      }
    }
  }.elsewhen(opcode === InstructionTypes.B) {
    disable_control()
    jump_flag := MuxLookup(
      funct3,
      0.U,
      IndexedSeq(
        InstructionsTypeB.beq -> (io.op1 === io.op2),
        InstructionsTypeB.bne -> (io.op1 =/= io.op2),
        InstructionsTypeB.bltu -> (io.op1 < io.op2),
        InstructionsTypeB.bgeu -> (io.op1 >= io.op2),
        InstructionsTypeB.blt -> (io.op1.asSInt() < io.op2.asSInt()),
        InstructionsTypeB.bge -> (io.op1.asSInt() >= io.op2.asSInt())
      )
    )
    jump_address := Fill(32, io.ctrl_jump_flag) & (io.op1_jump + io.op2_jump)
  }.elsewhen(opcode === Instructions.jal || opcode === Instructions.jalr) {
    disable_stall()
    jump_flag := true.B
    jump_address := io.op1_jump + io.op2_jump
    io.regs_write_data := io.op1 + io.op2
  }.elsewhen(opcode === Instructions.lui || opcode === Instructions.auipc) {
    disable_control()
    io.regs_write_data := io.op1 + io.op2
  }.elsewhen(opcode === Instructions.csr) {
    disable_control()
    io.csr_reg_write_data := MuxLookup(funct3, 0.U, IndexedSeq(
      InstructionsTypeCSR.csrrw -> io.reg1_data,
      InstructionsTypeCSR.csrrc -> io.csr_reg_data_id.&((~io.reg1_data).asUInt),
      InstructionsTypeCSR.csrrs -> io.csr_reg_data_id.|(io.reg1_data),
      InstructionsTypeCSR.csrrwi -> Cat(0.U(27.W), uimm),
      InstructionsTypeCSR.csrrci -> io.csr_reg_data_id.&((~Cat(0.U(27.W), uimm)).asUInt),
      InstructionsTypeCSR.csrrsi -> io.csr_reg_data_id.|(Cat(0.U(27.W), uimm)),
    ))
    io.regs_write_data := MuxLookup(funct3, 0.U, IndexedSeq(
      InstructionsTypeCSR.csrrw -> io.csr_reg_data_id,
      InstructionsTypeCSR.csrrc -> io.csr_reg_data_id,
      InstructionsTypeCSR.csrrs -> io.csr_reg_data_id,
      InstructionsTypeCSR.csrrwi -> io.csr_reg_data_id,
      InstructionsTypeCSR.csrrci -> io.csr_reg_data_id,
      InstructionsTypeCSR.csrrsi -> io.csr_reg_data_id,
    ))
  }.otherwise {
    disable_control()
    io.regs_write_data := 0.U
  }
}
