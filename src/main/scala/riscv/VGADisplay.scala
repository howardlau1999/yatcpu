package riscv

import chisel3._
import chisel3.util._

class VGADisplay extends Module {
  val io = IO(new Bundle() {
    val screen_x = Input(UInt(10.W))
    val screen_y = Input(UInt(10.W))

    val char_mem_address = Output(UInt(32.W))
    val char_mem_data = Input(UInt(32.W))

    val glyph_index = Output(UInt(7.W))
    val glyph_x = Output(UInt(7.W))
    val glyph_y = Output(UInt(7.W))
    val glyph_pixel_on = Input(Bool())

    val rgb = Output(UInt(12.W))
  })

  io.char_mem_address := (io.screen_y / GlyphInfo.glyphHeight.U * ScreenInfo.DisplayHorizontal.U / GlyphInfo.glyphWidth
    .U / 4.U) + (io.screen_x
    / GlyphInfo.glyphWidth.U / 4.U) + 1024.U
  val chars = Wire(Vec(4, UInt(32.W)))
  chars(0) := io.char_mem_data(7, 0)
  chars(1) := io.char_mem_data(15, 8)
  chars(2) := io.char_mem_data(23, 16)
  chars(3) := io.char_mem_data(31, 24)
  io.glyph_index := io.char_mem_data(chars(io.screen_x % 4.U)) - 31.U
  io.glyph_x := io.screen_x % GlyphInfo.glyphWidth.U
  io.glyph_y := io.screen_y % GlyphInfo.glyphHeight.U

  when(io.glyph_pixel_on) {
    io.rgb := 0xFF.U
  }.otherwise {
    io.rgb := 0.U
  }
}
