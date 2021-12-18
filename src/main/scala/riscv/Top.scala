package riscv

import chisel3._
import chisel3.util._

class Top extends Module {
  val binaryFilename = "hello.asmbin"
  val io = IO(new Bundle {
    val switch = Input(UInt(16.W))

    val segs = Output(UInt(7.W))
    val digit_mask = Output(UInt(4.W))
    val dp = Output(Bool())

    val hsync = Output(Bool())
    val vsync = Output(Bool())
    val rgb = Output(UInt(12.W))
    val led = Output(UInt(16.W))
  })

  val numbers = RegInit(UInt(16.W), 0.U)
  val counter = RegInit(UInt(31.W), 0.U)
  when(counter === 100000000.U) {
    counter := 0.U
    numbers := numbers + 1.U
  }.otherwise {
    counter := counter + 1.U
  }

  val onboard_display = Module(new OnboardDigitDisplay)

  io.segs := onboard_display.io.segs
  io.digit_mask := onboard_display.io.digit_mask

  val instruction_rom = Module(new InstructionROM(binaryFilename))
  val cpu = Module(new CPU)

  instruction_rom.io.instruction_address := cpu.io.instruction_address
  cpu.io.instruction := instruction_rom.io.instruction

  cpu.io.debug_read_address := 0.U
  cpu.io.debug_mem_read_address := 0.U

  val vga_display = Module(new VGADisplay)
  val vga_sync = Module(new VGASync)
  val font_rom = Module(new FontROM)
  vga_display.io.screen_x := vga_sync.io.x
  vga_display.io.screen_y := vga_sync.io.y
  io.hsync := vga_sync.io.hsync
  io.vsync := vga_sync.io.vsync

  cpu.io.char_mem_read_address := vga_display.io.char_mem_address
  vga_display.io.char_mem_data := cpu.io.char_mem_read_data

  font_rom.io.glyph_index := vga_display.io.glyph_rom_index
  font_rom.io.glyph_x := vga_display.io.glyph_x
  font_rom.io.glyph_y := vga_display.io.glyph_y
  when(vga_sync.io.video_on && font_rom.io.glyph_pixel_on) {
    io.rgb := 0xFFFF.U
  }.otherwise {
    io.rgb := 0.U
  }

  io.dp := true.B
  cpu.io.debug_mem_read_address := io.switch(15, 1).asUInt() * 4.U
  io.led := 0.U
  when(io.switch(0) === 0.U) {
    onboard_display.io.numbers := cpu.io.debug_mem_read_data(15, 0).asUInt()
  }.otherwise {
    onboard_display.io.numbers := cpu.io.debug_mem_read_data(31, 16).asUInt()
  }
}
