package riscv

import chisel3._
import chisel3.util._

import javax.imageio.ImageIO

object GlyphInfo {
  val glyphWidth = 8
  val glyphHeight = 16
  // ASCII printable characters start from here
  val spaceIndex = 1
}

class FontROM extends Module {
  val glyphWidth = GlyphInfo.glyphWidth
  val glyphHeight = GlyphInfo.glyphHeight

  val io = IO(new Bundle {
    val glyph_index = Input(UInt(7.W))
    val glyph_x = Input(UInt(4.W))
    val glyph_y = Input(UInt(4.W))

    val glyph_pixel_on = Output(Bool())
  })

  val glyphs = RegInit(readFontBitmap())
  io.glyph_pixel_on := glyphs(io.glyph_index)(glyphWidth.U * io.glyph_y + io.glyph_x).asBool()

  def readFontBitmap() = {
    val inputStream = getClass.getClassLoader.getResourceAsStream("vga_font_8x16.bmp")
    val image = ImageIO.read(inputStream)


    val glyphColumns = image.getWidth() / glyphWidth
    val glyphRows = image.getHeight / glyphHeight
    val glyphCount = glyphColumns * glyphRows
    val glyphs = new Array[UInt](glyphCount)

    for (row <- 0 until glyphRows) {
      for (col <- 0 until glyphColumns) {
        val lines = new Array[Int](glyphHeight)
        for (i <- 0 until glyphHeight) {
          var lineInt = 0
          for (j <- 0 until glyphWidth) {
            if (image.getRGB(col * glyphWidth + j, row * glyphHeight + i) == 0xFF000000) {
              lineInt |= (1 << j)
            }
          }
          lines(i) = lineInt
        }
        glyphs(row * glyphColumns + col) = Cat(lines.map(l => l.U))
      }
    }
    VecInit(glyphs)
  }
}