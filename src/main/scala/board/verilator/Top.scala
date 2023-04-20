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

import bus.{AXI4LiteSlave, AXI4LiteSlaveBundle, BusArbiter, BusSwitch}
import chisel3._
import chisel3.stage.{ChiselGeneratorAnnotation, ChiselStage}
import peripheral.DummySlave
import riscv.Parameters
import riscv.core.CPU

class Top extends Module {

  val io = IO(new Bundle {
    val signal_interrupt = Input(Bool())

    val mem_slave = new AXI4LiteSlaveBundle(Parameters.AddrBits, Parameters.DataBits)
    val uart_slave = new AXI4LiteSlaveBundle(Parameters.AddrBits, Parameters.DataBits)

    val cpu_debug_read_address = Input(UInt(Parameters.PhysicalRegisterAddrWidth))
    val cpu_debug_read_data = Output(UInt(Parameters.DataWidth))
  })

  // Memory is controlled in C++ code
  val mem_slave = Module(new AXI4LiteSlave(Parameters.AddrBits, Parameters.DataBits))
  io.mem_slave <> mem_slave.io.bundle

  // UART is controlled in C++ code
  val uart_slave = Module(new AXI4LiteSlave(Parameters.AddrBits, Parameters.DataBits))
  io.uart_slave <> uart_slave.io.bundle

  val cpu = Module(new CPU)
  val dummy = Module(new DummySlave)
  val bus_arbiter = Module(new BusArbiter)
  val bus_switch = Module(new BusSwitch)

  bus_arbiter.io.bus_request(0) := true.B

  bus_switch.io.master <> cpu.io.axi4_channels
  bus_switch.io.address := cpu.io.bus_address
  for (i <- 0 until Parameters.SlaveDeviceCount) {
    bus_switch.io.slaves(i) <> dummy.io.channels
  }

  cpu.io.stall_flag_bus := false.B
  cpu.io.instruction_valid := true.B
  bus_switch.io.slaves(0) <> mem_slave.io.channels
  bus_switch.io.slaves(2) <> uart_slave.io.channels

  cpu.io.interrupt_flag := io.signal_interrupt

  cpu.io.debug_read_address := io.cpu_debug_read_address
  io.cpu_debug_read_data := cpu.io.debug_read_data
}

object VerilogGenerator extends App {
  (new ChiselStage).execute(Array("-X", "verilog", "-td", "verilog/verilator"), Seq(ChiselGeneratorAnnotation(() =>
    new Top)))
}