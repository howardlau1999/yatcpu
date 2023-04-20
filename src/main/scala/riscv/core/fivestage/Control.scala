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
import riscv.Parameters

class Control extends Module {
  val io = IO(new Bundle {
    val jump_flag = Input(Bool())
    val jump_instruction_id = Input(Bool())
    val stall_flag_if = Input(Bool())
    val stall_flag_mem = Input(Bool())
    val stall_flag_clint = Input(Bool())
    val stall_flag_bus = Input(Bool())
    val rs1_id = Input(UInt(Parameters.PhysicalRegisterAddrWidth))
    val rs2_id = Input(UInt(Parameters.PhysicalRegisterAddrWidth))
    val memory_read_enable_ex = Input(Bool())
    val rd_ex = Input(UInt(Parameters.PhysicalRegisterAddrWidth))
    val memory_read_enable_mem = Input(Bool())
    val rd_mem = Input(UInt(Parameters.PhysicalRegisterAddrWidth))
    val csr_start_paging = Input(Bool())

    val if_flush = Output(Bool())
    val id_flush = Output(Bool())
    val pc_stall = Output(Bool())
    val if_stall = Output(Bool())
    val id_stall = Output(Bool())
    val ex_stall = Output(Bool())
  })

  val id_hazard = (io.memory_read_enable_ex || io.jump_instruction_id) && io.rd_ex =/= 0.U && (io.rd_ex === io.rs1_id
    || io.rd_ex === io.rs2_id) ||
    io.jump_instruction_id && io.memory_read_enable_mem && io.rd_mem =/= 0.U && (io.rd_mem === io.rs1_id || io.rd_mem
      === io.rs2_id)
  io.if_flush := io.jump_flag && !id_hazard || io.csr_start_paging
  io.id_flush := id_hazard || io.csr_start_paging

  io.pc_stall := io.stall_flag_mem || io.stall_flag_clint || id_hazard || io.stall_flag_bus || io.stall_flag_if
  io.if_stall := io.stall_flag_mem || io.stall_flag_clint || id_hazard
  io.id_stall := io.stall_flag_mem || io.stall_flag_clint
  io.ex_stall := io.stall_flag_mem || io.stall_flag_clint
}
