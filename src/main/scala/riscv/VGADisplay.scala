package riscv

import chisel3._
import chisel3.util._

class VGADisplay extends Module {
  val io = IO(new Bundle() {
    val screen_x = Input(UInt(10.W))
    val screen_y = Input(UInt(10.W))

    val char_mem_address = Output(UInt(32.W))
    val char_mem_data = Input(UInt(32.W))

    val glyph_rom_index = Output(UInt(7.W))
    val glyph_x = Output(UInt(7.W))
    val glyph_y = Output(UInt(7.W))
  })
  val row = io.screen_y / GlyphInfo.glyphHeight.U
  val col = io.screen_x / GlyphInfo.glyphWidth.U
  val glyph_index = (row * (ScreenInfo.DisplayHorizontal.U / GlyphInfo.glyphWidth.U)) + col
  io.char_mem_address := glyph_index + 1024.U
  val offset = glyph_index % 4.U
  val ch = Wire(UInt(8.W))

  ch := MuxLookup(
    offset,
    0.U,
    Array(
      0.U -> io.char_mem_data(7, 0).asUInt(),
      1.U -> io.char_mem_data(15, 8).asUInt(),
      2.U -> io.char_mem_data(23, 16).asUInt(),
      3.U -> io.char_mem_data(31, 24).asUInt()
    )
  )
  io.glyph_rom_index := Mux(ch >= 32.U, ch - 31.U, 0.U)

  io.glyph_x := io.screen_x % GlyphInfo.glyphWidth.U
  io.glyph_y := io.screen_y % GlyphInfo.glyphHeight.U
}
