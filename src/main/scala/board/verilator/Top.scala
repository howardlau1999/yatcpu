// Copyright 2022 Howard Lau
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

package board.verilator

import bus.{BusArbiter, BusSwitch}
import chisel3._
import chisel3.experimental.ChiselEnum
import chisel3.stage.{ChiselGeneratorAnnotation, ChiselStage}
import chisel3.util.{is, switch}
import peripheral.{DummySlave, InstructionROM, Memory, ROMLoader}
import riscv.Parameters
import riscv.core.{CPU, ProgramCounter}

object BootStates extends ChiselEnum {
  val Init, Loading, Finished = Value
}

class Top(binaryFilename: String = "tetris.asmbin") extends Module {
  val io = IO(new Bundle {
    val signal_interrupt = Input(Bool())

    val mem_debug_read_address = Input(UInt(Parameters.AddrWidth))
    val mem_debug_read_data = Output(UInt(Parameters.DataWidth))

    val cpu_debug_read_address = Input(UInt(Parameters.PhysicalRegisterAddrWidth))
    val cpu_debug_read_data = Output(UInt(Parameters.DataWidth))
  })

  val boot_state = RegInit(BootStates.Init)
  val cpu = Module(new CPU)
  val mem = Module(new Memory(Parameters.MemorySizeInWords))
  val dummy = Module(new DummySlave)
  val bus_arbiter = Module(new BusArbiter)
  val bus_switch = Module(new BusSwitch)
  val instruction_rom = Module(new InstructionROM(binaryFilename))
  val rom_loader = Module(new ROMLoader(instruction_rom.capacity))

  bus_arbiter.io.bus_request(0) := true.B

  bus_switch.io.master <> cpu.io.axi4_channels
  bus_switch.io.address := cpu.io.bus_address
  for (i <- 0 until Parameters.SlaveDeviceCount) {
    bus_switch.io.slaves(i) <> dummy.io.channels
  }

  rom_loader.io.load_address := ProgramCounter.EntryAddress
  rom_loader.io.load_start := false.B
  rom_loader.io.rom_data := instruction_rom.io.data
  instruction_rom.io.address := rom_loader.io.rom_address
  cpu.io.stall_flag_bus := true.B
  cpu.io.instruction_valid := false.B
  bus_switch.io.slaves(0) <> mem.io.channels
  rom_loader.io.channels <> dummy.io.channels
  switch(boot_state) {
    is(BootStates.Init) {
      rom_loader.io.load_start := true.B
      boot_state := BootStates.Loading
      rom_loader.io.channels <> mem.io.channels
    }
    is(BootStates.Loading) {
      rom_loader.io.load_start := false.B
      rom_loader.io.channels <> mem.io.channels
      when(rom_loader.io.load_finished) {
        boot_state := BootStates.Finished
      }
    }
    is(BootStates.Finished) {
      cpu.io.stall_flag_bus := false.B
      cpu.io.instruction_valid := true.B
    }
  }

  cpu.io.interrupt_flag := io.signal_interrupt

  cpu.io.debug_read_address := io.cpu_debug_read_address
  io.cpu_debug_read_data := cpu.io.debug_read_data
  mem.io.debug_read_address := io.mem_debug_read_address
  io.mem_debug_read_data := mem.io.debug_read_data
}

object VerilogGenerator extends App {
  (new ChiselStage).execute(Array("-X", "verilog", "-td", "verilog/verilator"), Seq(ChiselGeneratorAnnotation(() =>
    new Top(args(1)))))
}