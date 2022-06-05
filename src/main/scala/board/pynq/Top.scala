// Copyright 2022 Canbin Huang
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

package board.pynq

import bus.{AXI4LiteChannels, AXI4LiteInterface, BusArbiter, BusSwitch}
import chisel3._
import chisel3.experimental.ChiselEnum
import chisel3.stage.{ChiselGeneratorAnnotation, ChiselStage}
import chisel3.util.{Cat, is, switch}
import peripheral._
import riscv.{ImplementationType, Parameters}
import riscv.core.{CPU, CPUBundle}

object BootStates extends ChiselEnum {
  val Init, Loading, BusWait, Finished = Value
}

class Top extends Module {
  val binaryFilename = "tetris.asmbin"
  val io = IO(new Bundle() {
    val hdmi_clk_n = Output(Bool())
    val hdmi_clk_p = Output(Bool())
    val hdmi_data_n = Output(UInt(3.W))
    val hdmi_data_p = Output(UInt(3.W))
    val hdmi_hpdn = Output(Bool())

    val axi_mem = new AXI4LiteInterface(32, 32)

    val tx = Output(Bool())
    val rx = Input(Bool())

    val led = Output(UInt(4.W))

    val debug = Output(Vec(8, UInt(32.W)))
  })
  val boot_state = RegInit(BootStates.Init)
  io.led := boot_state.asUInt

  val uart = Module(new Uart(100000000, 115200))
  io.tx := uart.io.txd
  uart.io.rxd := io.rx

  val cpu = Module(new CPU)
  val mem = Wire(new AXI4LiteChannels(32, 32))
  val timer = Module(new Timer)
  val dummy = Module(new DummySlave)
  val bus_arbiter = Module(new BusArbiter)
  val bus_switch = Module(new BusSwitch)
  mem.write_address_channel.AWADDR <> io.axi_mem.AWADDR
  mem.write_address_channel.AWPROT <> io.axi_mem.AWPROT
  mem.write_address_channel.AWREADY <> io.axi_mem.AWREADY
  mem.write_address_channel.AWVALID <> io.axi_mem.AWVALID
  mem.read_address_channel.ARADDR <> io.axi_mem.ARADDR
  mem.read_address_channel.ARPROT <> io.axi_mem.ARPROT
  mem.read_address_channel.ARREADY <> io.axi_mem.ARREADY
  mem.read_address_channel.ARVALID <> io.axi_mem.ARVALID
  mem.read_data_channel.RDATA <> io.axi_mem.RDATA
  mem.read_data_channel.RVALID <> io.axi_mem.RVALID
  mem.read_data_channel.RRESP <> io.axi_mem.RRESP
  mem.read_data_channel.RREADY <> io.axi_mem.RREADY
  mem.write_data_channel.WDATA <> io.axi_mem.WDATA
  mem.write_data_channel.WVALID <> io.axi_mem.WVALID
  mem.write_data_channel.WSTRB <> io.axi_mem.WSTRB
  mem.write_data_channel.WREADY <> io.axi_mem.WREADY
  mem.write_response_channel.BVALID <> io.axi_mem.BVALID
  mem.write_response_channel.BRESP <> io.axi_mem.BRESP
  mem.write_response_channel.BREADY <> io.axi_mem.BREADY

  val instruction_rom = Module(new InstructionROM(binaryFilename))
  val rom_loader = Module(new ROMLoader(instruction_rom.capacity))

  val hdmi_display = Module(new HDMIDisplay)
  bus_arbiter.io.bus_request(0) := true.B

  bus_switch.io.master <> cpu.io.axi4_channels
  bus_switch.io.address := cpu.io.bus_address
  for (i <- 0 until Parameters.SlaveDeviceCount) {
    bus_switch.io.slaves(i) <> dummy.io.channels
  }
  rom_loader.io.load_address := Parameters.EntryAddress
  rom_loader.io.load_start := false.B
  rom_loader.io.rom_data := instruction_rom.io.data
  instruction_rom.io.address := rom_loader.io.rom_address
  cpu.io.stall_flag_bus := true.B
  cpu.io.instruction_valid := false.B
  rom_loader.io.channels <> dummy.io.channels
  bus_switch.io.slaves(0) <> mem
  switch(boot_state) {
    is(BootStates.Init) {
      rom_loader.io.load_start := true.B
      boot_state := BootStates.Loading
      rom_loader.io.channels <> mem
      bus_switch.io.slaves(0) <> dummy.io.channels
    }
    is(BootStates.Loading) {
      rom_loader.io.load_start := false.B
      rom_loader.io.channels <> mem
      bus_switch.io.slaves(0) <> dummy.io.channels
      when(rom_loader.io.load_finished) {
        boot_state := BootStates.BusWait
      }
    }
    is(BootStates.BusWait) {
      when(!cpu.io.bus_busy) {
        boot_state := BootStates.Finished
      }
    }
    is(BootStates.Finished) {
      cpu.io.stall_flag_bus := false.B
      cpu.io.instruction_valid := true.B
      rom_loader.io.channels <> dummy.io.channels
      bus_switch.io.slaves(0) <> mem
    }
  }
  io.debug(0) := cpu.io.instruction_valid
  io.debug(1) := cpu.io.debug(0)
  io.debug(2) := cpu.io.debug(1)
  io.debug(3) := cpu.io.debug(2)
  io.debug(4) := cpu.io.debug(3)
  io.debug(5) := cpu.io.debug(4)
  io.debug(6) := cpu.io.debug(5)
  io.debug(7) := cpu.io.interrupt_flag

  val display = Module(new CharacterDisplay)
  bus_switch.io.slaves(1) <> display.io.channels
  bus_switch.io.slaves(2) <> uart.io.channels
  bus_switch.io.slaves(4) <> timer.io.channels

  cpu.io.interrupt_flag := Cat(uart.io.signal_interrupt, timer.io.signal_interrupt)

  cpu.io.debug_read_address := 0.U

  display.io.x := hdmi_display.io.x
  display.io.y := hdmi_display.io.y
  display.io.video_on := hdmi_display.io.video_on
  hdmi_display.io.rgb := display.io.rgb

  io.hdmi_hpdn := 1.U
  io.hdmi_data_n := hdmi_display.io.TMDSdata_n
  io.hdmi_data_p := hdmi_display.io.TMDSdata_p
  io.hdmi_clk_n := hdmi_display.io.TMDSclk_n
  io.hdmi_clk_p := hdmi_display.io.TMDSclk_p
}

object VerilogGenerator extends App {
  (new ChiselStage).execute(Array("-X", "verilog", "-td", "verilog/pynq"), Seq(ChiselGeneratorAnnotation(() => new Top)))
}