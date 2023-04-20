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
import chisel3.util.MuxCase
import riscv.Parameters
import riscv.core.BusBundle

object ProgramCounter {
  val EntryAddress = Parameters.EntryAddress
}

object IFAccessStates extends ChiselEnum {
  val idle, read = Value
}

class InstructionFetch extends Module {
  val io = IO(new Bundle {
    val stall_flag_ctrl = Input(Bool())
    val jump_flag_id = Input(Bool())
    val jump_address_id = Input(UInt(Parameters.AddrWidth))

    val physical_address = Input(UInt(Parameters.AddrWidth))

    val ctrl_stall_flag = Output(Bool())
    val id_instruction_address = Output(UInt(Parameters.AddrWidth))
    val id_instruction = Output(UInt(Parameters.InstructionWidth))
    val pc_valid = Output(Bool())

    val bus = new BusBundle
  })
  val pending_jump = RegInit(false.B)
  val pc = RegInit(ProgramCounter.EntryAddress)
  val state = RegInit(IFAccessStates.idle)
  val pc_valid = RegInit(false.B) //because the romloader of verilator(sim_main.cpp) need no time cycle
  //it prevent fetching instruction from 0x0, when pc haven't been initailized

  io.bus.read := false.B
  io.bus.request := true.B
  io.bus.write := false.B
  io.bus.write_data := 0.U
  io.bus.write_strobe := VecInit(Seq.fill(Parameters.WordSize)(false.B))
  io.pc_valid := pc_valid

  when(!pc_valid && pc === ProgramCounter.EntryAddress) {
    pc_valid := true.B
  }

  pc := MuxCase(
    pc + 4.U,
    IndexedSeq(
      io.jump_flag_id -> io.jump_address_id,
      io.stall_flag_ctrl -> pc
    )
  )

  when(!io.bus.read_valid) {
    when(io.jump_flag_id) {
      pending_jump := true.B
    }
  }

  when(io.bus.read_valid) {
    when(pending_jump) {
      pending_jump := false.B
    }
  }

  when(io.bus.granted) {
    when(state === IFAccessStates.idle) {
      io.bus.request := true.B
      io.bus.read := true.B
      state := IFAccessStates.read
    }.elsewhen(state === IFAccessStates.read) {
      io.bus.read := false.B
      io.bus.request := true.B
      when(io.bus.read_valid) {
        state := IFAccessStates.idle
      }
    }
  }

  io.id_instruction := Mux(
    io.bus.read_valid && !pending_jump && !io.jump_flag_id,
    io.bus.read_data,
    InstructionsNop.nop
  )
  io.ctrl_stall_flag := !io.bus.read_valid || pending_jump
  io.id_instruction_address := pc
  io.bus.address := io.physical_address
}
