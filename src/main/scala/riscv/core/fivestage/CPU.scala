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

import bus.AXI4LiteMaster
import chisel3._
import riscv.Parameters
import riscv.core.CPUBundle

object BUSGranted extends ChiselEnum {
  val idle, if_granted, mem_granted, mmu_mem_granted, mmu_if_granted = Value
}

object MEMAccessState extends ChiselEnum {
  val idle, if_address_translate, mem_address_translate, mem_access, if_access = Value
}

class CPU extends Module {
  val io = IO(new CPUBundle)

  val ctrl = Module(new Control)
  val regs = Module(new RegisterFile)
  val inst_fetch = Module(new InstructionFetch)
  val if2id = Module(new IF2ID)
  val id = Module(new InstructionDecode)
  val id2ex = Module(new ID2EX)
  val ex = Module(new Execute)
  val ex2mem = Module(new EX2MEM)
  val mem = Module(new MemoryAccess)
  val mem2wb = Module(new MEM2WB)
  val wb = Module(new WriteBack)
  val forwarding = Module(new Forwarding)
  val clint = Module(new CLINT)
  val csr_regs = Module(new CSR)
  val axi4_master = Module(new AXI4LiteMaster(Parameters.AddrBits, Parameters.DataBits))
  val mmu = Module(new MMU)
  axi4_master.io.channels <> io.axi4_channels
  io.debug(0) := ex.io.reg1_data
  io.debug(1) := ex.io.reg2_data
  io.debug(2) := ex.io.instruction_address
  io.debug(3) := ex.io.instruction
  io.debug(4) := inst_fetch.io.jump_address_id
  io.debug(5) := inst_fetch.io.jump_flag_id
  io.bus_busy := axi4_master.io.bundle.busy

  val bus_granted = RegInit(BUSGranted.idle)
  val mem_access_state = RegInit(MEMAccessState.idle)
  val virtual_address = RegInit(UInt(Parameters.AddrWidth), 0.U)
  val physical_address = RegInit(UInt(Parameters.AddrWidth), 0.U)
  val mmu_restart = RegInit(false.B)
  val pending = RegInit(false.B) //play the same role as pending_jump in Inst_fetch

  //bus arbitration
  when(mem_access_state === MEMAccessState.idle) {
    bus_granted := BUSGranted.idle
    when(!axi4_master.io.bundle.busy && !axi4_master.io.bundle.read_valid) {
      when(csr_regs.io.mmu_enable) {
        when(mem.io.bus.request) {
          mem_access_state := MEMAccessState.mem_address_translate
          bus_granted := BUSGranted.mmu_mem_granted
          virtual_address := ex2mem.io.output_alu_result
        }.elsewhen(inst_fetch.io.bus.request && io.instruction_valid && inst_fetch.io.pc_valid) {
          mem_access_state := MEMAccessState.if_address_translate
          bus_granted := BUSGranted.mmu_if_granted
          virtual_address := inst_fetch.io.id_instruction_address
        }
      }.otherwise {
        when(mem.io.bus.request) {
          mem_access_state := MEMAccessState.mem_access
          physical_address := ex2mem.io.output_alu_result
          bus_granted := BUSGranted.mem_granted
        }.elsewhen(inst_fetch.io.bus.request && io.instruction_valid && inst_fetch.io.pc_valid) {
          mem_access_state := MEMAccessState.if_access
          bus_granted := BUSGranted.if_granted
          physical_address := inst_fetch.io.id_instruction_address
        }
      }
    }
  }.elsewhen(mem_access_state === MEMAccessState.mem_address_translate) {
    when(clint.io.exception_token) {
      mem_access_state := MEMAccessState.if_address_translate
      bus_granted := BUSGranted.mmu_if_granted
      virtual_address := id.io.if_jump_address
    }.elsewhen(mmu.io.pa_valid) {
      mem_access_state := MEMAccessState.mem_access
      bus_granted := BUSGranted.mem_granted
      physical_address := mmu.io.pa
    }
  }.elsewhen(mem_access_state === MEMAccessState.if_address_translate) {
    //"Interrupt" the IF address translation, turn to mem address translation
    when(mem.io.bus.request) {
      mmu_restart := true.B
      when(mmu.io.restart_done) {
        mmu_restart := false.B
        mem_access_state := MEMAccessState.mem_address_translate
        bus_granted := BUSGranted.mmu_mem_granted
        virtual_address := ex2mem.io.output_alu_result
      }
    }.otherwise {
      when(pending) {
        when(mmu.io.restart_done) {
          mmu_restart := false.B
          pending := false.B
          mem_access_state := MEMAccessState.if_address_translate
          bus_granted := BUSGranted.mmu_if_granted
          virtual_address := inst_fetch.io.id_instruction_address
        }
      }.otherwise {
        when(!id.io.if_jump_flag && mmu.io.pa_valid) {
          mem_access_state := MEMAccessState.if_access
          bus_granted := BUSGranted.if_granted
          physical_address := mmu.io.pa
        }
      }

      when(id.io.if_jump_flag) {
        mmu_restart := true.B
        pending := true.B
      }
    }
  }.elsewhen(mem_access_state === MEMAccessState.mem_access) {
    when(mem.io.bus.read_valid || mem.io.bus.write_valid) {
      mem_access_state := MEMAccessState.idle
      bus_granted := BUSGranted.idle
    }
  }.elsewhen(mem_access_state === MEMAccessState.if_access) {
    when(inst_fetch.io.bus.read_valid) {
      bus_granted := BUSGranted.idle
      mem_access_state := MEMAccessState.idle
    }
  }

  when(bus_granted === BUSGranted.mmu_if_granted || bus_granted === BUSGranted.mmu_mem_granted) {
    io.bus_address := mmu.io.bus.address
    axi4_master.io.bundle.read := mmu.io.bus.read
    axi4_master.io.bundle.address := mmu.io.bus.address
    axi4_master.io.bundle.write := mmu.io.bus.write
    axi4_master.io.bundle.write_data := mmu.io.bus.write_data
    axi4_master.io.bundle.write_strobe := mmu.io.bus.write_strobe
  }.elsewhen(bus_granted === BUSGranted.mem_granted) {
    io.bus_address := mem.io.bus.address
    axi4_master.io.bundle.read := mem.io.bus.read
    axi4_master.io.bundle.address := mem.io.bus.address
    axi4_master.io.bundle.write := mem.io.bus.write
    axi4_master.io.bundle.write_data := mem.io.bus.write_data
    axi4_master.io.bundle.write_strobe := mem.io.bus.write_strobe
  }.otherwise {
    io.bus_address := inst_fetch.io.bus.address
    axi4_master.io.bundle.read := inst_fetch.io.bus.read
    axi4_master.io.bundle.address := inst_fetch.io.bus.address
    axi4_master.io.bundle.write := inst_fetch.io.bus.write
    axi4_master.io.bundle.write_data := inst_fetch.io.bus.write_data
    axi4_master.io.bundle.write_strobe := inst_fetch.io.bus.write_strobe
  }

  inst_fetch.io.bus.read_valid := Mux(
    bus_granted === BUSGranted.if_granted && io.instruction_valid,
    axi4_master.io.bundle.read_valid,
    false.B
  )
  inst_fetch.io.bus.read_data := Mux(
    bus_granted === BUSGranted.if_granted && io.instruction_valid,
    axi4_master.io.bundle.read_data,
    0.U
  )
  inst_fetch.io.bus.write_valid := false.B
  inst_fetch.io.bus.busy := Mux(
    bus_granted === BUSGranted.if_granted && io.instruction_valid,
    axi4_master.io.bundle.busy,
    false.B
  )
  mem.io.bus.read_valid := Mux(
    bus_granted === BUSGranted.mem_granted,
    axi4_master.io.bundle.read_valid,
    false.B
  )
  mem.io.bus.read_data := Mux(
    bus_granted === BUSGranted.mem_granted,
    axi4_master.io.bundle.read_data,
    0.U
  )
  mem.io.bus.write_valid := Mux(
    bus_granted === BUSGranted.mem_granted,
    axi4_master.io.bundle.write_valid,
    false.B
  )
  mem.io.bus.busy := Mux(
    bus_granted === BUSGranted.mem_granted,
    axi4_master.io.bundle.busy,
    false.B
  )
  mmu.io.bus.read_valid := Mux(
    bus_granted === BUSGranted.mmu_if_granted || bus_granted === BUSGranted.mmu_mem_granted,
    axi4_master.io.bundle.read_valid,
    false.B
  )
  mmu.io.bus.read_data := Mux(
    bus_granted === BUSGranted.mmu_if_granted || bus_granted === BUSGranted.mmu_mem_granted,
    axi4_master.io.bundle.read_data,
    0.U
  )
  mmu.io.bus.write_valid := Mux(
    bus_granted === BUSGranted.mmu_if_granted || bus_granted === BUSGranted.mmu_mem_granted,
    axi4_master.io.bundle.write_valid,
    false.B
  )
  mmu.io.bus.busy := Mux(
    bus_granted === BUSGranted.mmu_if_granted || bus_granted === BUSGranted.mmu_mem_granted,
    axi4_master.io.bundle.busy,
    false.B
  )

  mmu.io.instructions := ex2mem.io.output_instruction
  mmu.io.instructions_address := ex2mem.io.output_instruction_address
  mmu.io.virtual_address := virtual_address
  mmu.io.bus.granted := bus_granted === BUSGranted.mmu_mem_granted || bus_granted === BUSGranted.mmu_if_granted
  mmu.io.page_fault_responed := false.B
  mmu.io.ppn_from_satp := csr_regs.io.mmu_csr_satp(21, 0)
  mmu.io.page_fault_responed := clint.io.exception_token
  mmu.io.mmu_occupied_by_mem := bus_granted === BUSGranted.mmu_mem_granted
  mmu.io.restart := mmu_restart

  inst_fetch.io.bus.granted := bus_granted === BUSGranted.if_granted
  inst_fetch.io.physical_address := physical_address

  mem.io.bus.granted := bus_granted === BUSGranted.mem_granted
  mem.io.physical_address := physical_address

  ctrl.io.jump_flag := id.io.if_jump_flag
  ctrl.io.jump_instruction_id := id.io.ctrl_jump_instruction
  ctrl.io.stall_flag_if := inst_fetch.io.ctrl_stall_flag
  ctrl.io.stall_flag_mem := mem.io.ctrl_stall_flag
  ctrl.io.stall_flag_clint := clint.io.ctrl_stall_flag
  ctrl.io.stall_flag_bus := io.stall_flag_bus
  ctrl.io.rs1_id := id.io.regs_reg1_read_address
  ctrl.io.rs2_id := id.io.regs_reg2_read_address
  ctrl.io.memory_read_enable_ex := id2ex.io.output_memory_read_enable
  ctrl.io.rd_ex := id2ex.io.output_regs_write_address
  ctrl.io.memory_read_enable_mem := ex2mem.io.output_memory_read_enable
  ctrl.io.rd_mem := ex2mem.io.output_regs_write_address
  ctrl.io.csr_start_paging := csr_regs.io.start_paging

  regs.io.write_enable := mem2wb.io.output_regs_write_enable
  regs.io.write_address := mem2wb.io.output_regs_write_address
  regs.io.write_data := wb.io.regs_write_data
  regs.io.read_address1 := id.io.regs_reg1_read_address
  regs.io.read_address2 := id.io.regs_reg2_read_address

  regs.io.debug_read_address := io.debug_read_address
  io.debug_read_data := regs.io.debug_read_data

  inst_fetch.io.stall_flag_ctrl := ctrl.io.pc_stall
  inst_fetch.io.jump_flag_id := id.io.if_jump_flag
  inst_fetch.io.jump_address_id := id.io.if_jump_address

  if2id.io.stall_flag := ctrl.io.if_stall
  if2id.io.flush_enable := ctrl.io.if_flush
  if2id.io.instruction := inst_fetch.io.id_instruction
  if2id.io.instruction_address := inst_fetch.io.id_instruction_address
  if2id.io.interrupt_flag := io.interrupt_flag

  id.io.instruction := if2id.io.output_instruction
  id.io.instruction_address := if2id.io.output_instruction_address
  id.io.reg1_data := regs.io.read_data1
  id.io.reg2_data := regs.io.read_data2
  id.io.forward_from_mem := mem.io.forward_data
  id.io.forward_from_wb := wb.io.regs_write_data
  id.io.reg1_forward := forwarding.io.reg1_forward_id
  id.io.reg2_forward := forwarding.io.reg2_forward_id
  id.io.interrupt_assert := clint.io.id_interrupt_assert
  id.io.interrupt_handler_address := clint.io.id_interrupt_handler_address

  id2ex.io.stall_flag := ctrl.io.id_stall
  id2ex.io.flush_enable := ctrl.io.id_flush
  id2ex.io.instruction := if2id.io.output_instruction
  id2ex.io.instruction_address := if2id.io.output_instruction_address
  id2ex.io.regs_write_enable := id.io.ex_reg_write_enable
  id2ex.io.regs_write_address := id.io.ex_reg_write_address
  id2ex.io.regs_write_source := id.io.ex_reg_write_source
  id2ex.io.reg1_data := regs.io.read_data1
  id2ex.io.reg2_data := regs.io.read_data2
  id2ex.io.immediate := id.io.ex_immediate
  id2ex.io.aluop1_source := id.io.ex_aluop1_source
  id2ex.io.aluop2_source := id.io.ex_aluop2_source
  id2ex.io.csr_write_enable := id.io.ex_csr_write_enable
  id2ex.io.csr_address := id.io.ex_csr_address
  id2ex.io.memory_read_enable := id.io.ex_memory_read_enable
  id2ex.io.memory_write_enable := id.io.ex_memory_write_enable
  id2ex.io.csr_read_data := csr_regs.io.id_reg_data

  ex.io.instruction := id2ex.io.output_instruction
  ex.io.instruction_address := id2ex.io.output_instruction_address
  ex.io.reg1_data := id2ex.io.output_reg1_data
  ex.io.reg2_data := id2ex.io.output_reg2_data
  ex.io.immediate := id2ex.io.output_immediate
  ex.io.aluop1_source := id2ex.io.output_aluop1_source
  ex.io.aluop2_source := id2ex.io.output_aluop2_source
  ex.io.csr_read_data := id2ex.io.output_csr_read_data
  ex.io.forward_from_mem := mem.io.forward_data
  ex.io.forward_from_wb := wb.io.regs_write_data
  ex.io.reg1_forward := forwarding.io.reg1_forward_ex
  ex.io.reg2_forward := forwarding.io.reg2_forward_ex

  ex2mem.io.stall_flag := ctrl.io.ex_stall
  ex2mem.io.flush_enable := false.B
  ex2mem.io.regs_write_enable := id2ex.io.output_regs_write_enable
  ex2mem.io.regs_write_source := id2ex.io.output_regs_write_source
  ex2mem.io.regs_write_address := id2ex.io.output_regs_write_address
  ex2mem.io.instruction_address := id2ex.io.output_instruction_address
  ex2mem.io.instruction := id2ex.io.output_instruction
  ex2mem.io.reg1_data := id2ex.io.output_reg1_data
  ex2mem.io.reg2_data := id2ex.io.output_reg2_data
  ex2mem.io.memory_read_enable := id2ex.io.output_memory_read_enable
  ex2mem.io.memory_write_enable := id2ex.io.output_memory_write_enable
  ex2mem.io.alu_result := ex.io.mem_alu_result
  ex2mem.io.csr_read_data := id2ex.io.output_csr_read_data

  mem.io.alu_result := ex2mem.io.output_alu_result
  mem.io.reg2_data := ex2mem.io.output_reg2_data
  mem.io.memory_read_enable := ex2mem.io.output_memory_read_enable
  mem.io.memory_write_enable := ex2mem.io.output_memory_write_enable
  mem.io.funct3 := ex2mem.io.output_instruction(14, 12)
  mem.io.regs_write_source := ex2mem.io.output_regs_write_source
  mem.io.csr_read_data := ex2mem.io.output_csr_read_data
  mem.io.clint_exception_token := clint.io.exception_token

  mem2wb.io.instruction_address := ex2mem.io.output_instruction_address
  mem2wb.io.alu_result := ex2mem.io.output_alu_result
  mem2wb.io.regs_write_enable := ex2mem.io.output_regs_write_enable
  mem2wb.io.regs_write_source := ex2mem.io.output_regs_write_source
  mem2wb.io.regs_write_address := ex2mem.io.output_regs_write_address
  mem2wb.io.memory_read_data := mem.io.wb_memory_read_data
  mem2wb.io.csr_read_data := ex2mem.io.output_csr_read_data

  wb.io.instruction_address := mem2wb.io.output_instruction_address
  wb.io.alu_result := mem2wb.io.output_alu_result
  wb.io.memory_read_data := mem2wb.io.output_memory_read_data
  wb.io.regs_write_source := mem2wb.io.output_regs_write_source
  wb.io.csr_read_data := mem2wb.io.output_csr_read_data

  forwarding.io.rs1_id := id.io.regs_reg1_read_address
  forwarding.io.rs2_id := id.io.regs_reg2_read_address
  forwarding.io.rs1_ex := id2ex.io.output_instruction(19, 15)
  forwarding.io.rs2_ex := id2ex.io.output_instruction(24, 20)
  forwarding.io.rd_mem := ex2mem.io.output_regs_write_address
  forwarding.io.reg_write_enable_mem := ex2mem.io.output_regs_write_enable
  forwarding.io.rd_wb := mem2wb.io.output_regs_write_address
  forwarding.io.reg_write_enable_wb := mem2wb.io.output_regs_write_enable

  clint.io.instruction := if2id.io.output_instruction
  clint.io.instruction_address_if := inst_fetch.io.id_instruction_address
  clint.io.jump_flag := id.io.if_jump_flag
  clint.io.jump_address := id.io.clint_jump_address
  clint.io.csr_mepc := csr_regs.io.clint_csr_mepc
  clint.io.csr_mtvec := csr_regs.io.clint_csr_mtvec
  clint.io.csr_mstatus := csr_regs.io.clint_csr_mstatus
  clint.io.interrupt_enable := csr_regs.io.interrupt_enable
  clint.io.interrupt_flag := if2id.io.output_interrupt_flag
  //todo: change it for handling more exceptions
  clint.io.exception_signal := mmu.io.page_fault_signals
  clint.io.instruction_address_cause_exception := mmu.io.epc
  clint.io.exception_val := mmu.io.va_cause_page_fault
  clint.io.exception_cause := mmu.io.ecause

  csr_regs.io.reg_write_enable_ex := id2ex.io.output_csr_write_enable
  csr_regs.io.reg_write_address_ex := id2ex.io.output_csr_address
  csr_regs.io.reg_write_data_ex := ex.io.csr_write_data
  csr_regs.io.reg_read_address_id := id.io.ex_csr_address
  csr_regs.io.reg_write_enable_clint := clint.io.csr_reg_write_enable
  csr_regs.io.reg_write_address_clint := clint.io.csr_reg_write_address
  csr_regs.io.reg_write_data_clint := clint.io.csr_reg_write_data
  csr_regs.io.reg_read_address_clint := 0.U
}
