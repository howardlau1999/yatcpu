package board.z710


import chisel3._
import chisel3.stage.ChiselStage
import chisel3.util._
import chisel3.{ChiselEnum, _}

// import circt.stage.ChiselStage
import chisel3.stage.ChiselGeneratorAnnotation

import bus._
import peripheral._
import riscv._
import riscv.Parameters
import riscv.core.CPU
import javax.print.SimpleDoc

object BootStates extends ChiselEnum {
  val Init, Loading, BusWait, Finished = Value
}


class Top(binaryFilename: String ="say_goodbye.asmbin") extends Module {
  // val binaryFilename = "say_goodbye.asmbin"
  val io = IO(new Bundle {
    // val switch = Input(UInt(16.W))

    // val rgb = Output(UInt(12.W))

    val led = Output(Bool())
    val tx = Output(Bool())
    val rx = Input(Bool())


  })
  val boot_state = RegInit(BootStates.Init)

  val uart = Module(new Uart(125_000_000, 115200))  // this freq is consistent with Zynq 7 PS UART module
  io.tx := uart.io.txd
  uart.io.rxd := io.rx

  val cpu = Module(new CPU)
  val mem = Module(new Memory(Parameters.MemorySizeInWords))
  val timer = Module(new Timer)
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
  rom_loader.io.load_address := Parameters.EntryAddress
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

  bus_switch.io.slaves(2) <> uart.io.channels
  bus_switch.io.slaves(4) <> timer.io.channels

  cpu.io.interrupt_flag := Cat(uart.io.signal_interrupt, timer.io.signal_interrupt)

  cpu.io.debug_read_address := 0.U
  mem.io.debug_read_address := 0.U


  
  val clock_freq = 100_000_000.U

  val led_count = RegInit(0.U(32.W))
  when (led_count >= clock_freq) { // the led blinks every second, clock freq is 100M
    led_count := 0.U
  }.otherwise {
    led_count := led_count + 1.U
  }

  io.led := (led_count >= (clock_freq >> 1))


}



object VerilogGenerator extends App {
    (new ChiselStage).execute(
        Array("-X", "verilog", "--target-dir", "verilog/z710"), 
        Seq(ChiselGeneratorAnnotation(() => new Top())) // default bin file
    )
    
}