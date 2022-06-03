package peripheral

import chisel3._
import bus.{AXI4LiteChannels, AXI4LiteSlave}
import chisel3.{Bool, Bundle, Flipped, Module, Mux, Output, UInt, Wire}
import chisel3.util.{MuxLookup, log2Up}
import riscv.Parameters

class CharacterDisplay extends Module {
  val io = IO(new Bundle() {
    val channels = Flipped(new AXI4LiteChannels(log2Up(ScreenInfo.Chars), Parameters.DataBits))

    val x = Input(UInt(16.W))
    val y = Input(UInt(16.W))
    val video_on = Input(Bool())

    val rgb = Output(UInt(24.W))
  })
  val slave = Module(new AXI4LiteSlave(log2Up(ScreenInfo.Chars), Parameters.DataBits))
  slave.io.channels <> io.channels
  val mem = Module(new BlockRAM(ScreenInfo.Chars / Parameters.WordSize))
  slave.io.bundle.read_valid := true.B
  mem.io.write_enable := slave.io.bundle.write
  mem.io.write_data := slave.io.bundle.write_data
  mem.io.write_address := slave.io.bundle.address
  mem.io.write_strobe := slave.io.bundle.write_strobe

  mem.io.read_address := slave.io.bundle.address
  slave.io.bundle.read_data := mem.io.read_data


  val font_rom = Module(new FontROM)
  val row = (io.y >> log2Up(GlyphInfo.glyphHeight)).asUInt
  val col = (io.x >> log2Up(GlyphInfo.glyphWidth)).asUInt
  val char_index = (row * ScreenInfo.CharCols.U) + col
  val offset = char_index(1, 0)
  val ch = Wire(UInt(8.W))

  mem.io.debug_read_address := char_index
  ch := MuxLookup(
    offset,
    0.U,
    IndexedSeq(
      0.U -> mem.io.debug_read_data(7, 0).asUInt,
      1.U -> mem.io.debug_read_data(15, 8).asUInt,
      2.U -> mem.io.debug_read_data(23, 16).asUInt,
      3.U -> mem.io.debug_read_data(31, 24).asUInt
    )
  )
  font_rom.io.glyph_index := Mux(ch >= 32.U, ch - 31.U, 0.U)
  font_rom.io.glyph_y := io.y(log2Up(GlyphInfo.glyphHeight) - 1, 0)

  // White if pixel_on and glyph pixel on
  val glyph_x = io.x(log2Up(GlyphInfo.glyphWidth) - 1, 0)
  io.rgb := Mux(io.video_on && font_rom.io.glyph_pixel_byte(glyph_x), 0xFFFFFF.U, 0.U)

}
