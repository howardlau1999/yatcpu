package peripheral

import chisel3.experimental.{ChiselAnnotation, annotate}
import chisel3.util.experimental.loadMemoryFromFileInline
import chisel3.{Bundle, Input, Module, Output, SyncReadMem, UInt, _}
import firrtl.annotations.MemorySynthInit

import java.io.FileWriter
import java.nio.file.Paths
import javax.imageio.ImageIO

class FontROM(fontBitmapFilename: String = "vga_font_8x16.bmp") extends Module {
  val glyphWidth = GlyphInfo.glyphWidth
  val glyphHeight = GlyphInfo.glyphHeight

  val io = IO(new Bundle {
    val glyph_index = Input(UInt(7.W))
    val glyph_y = Input(UInt(4.W))

    val glyph_pixel_byte = Output(UInt(8.W))
  })

  annotate(new ChiselAnnotation {
    override def toFirrtl =
      MemorySynthInit
  })

  val (hexTxtPath, glyphCount) = readFontBitmap()
  val mem = SyncReadMem(glyphCount, UInt(8.W))
  loadMemoryFromFileInline(mem, hexTxtPath.toString.replaceAll("\\\\", "/"))
  io.glyph_pixel_byte := mem.read(io.glyph_index * GlyphInfo.glyphHeight.U + io.glyph_y, true.B)

  def readFontBitmap() = {
    val inputStream = getClass.getClassLoader.getResourceAsStream(fontBitmapFilename)
    val image = ImageIO.read(inputStream)

    val glyphColumns = image.getWidth() / glyphWidth
    val glyphRows = image.getHeight / glyphHeight
    val glyphCount = glyphColumns * glyphRows
    val glyphs = new Array[UInt](glyphCount * GlyphInfo.glyphHeight)

    for (row <- 0 until glyphRows) {
      for (col <- 0 until glyphColumns) {
        for (i <- 0 until glyphHeight) {
          var lineInt = 0
          for (j <- 0 until glyphWidth) {
            if (image.getRGB(col * glyphWidth + j, row * glyphHeight + i) != 0xFFFFFFFF) {
              lineInt |= (1 << j)
            }
          }
          glyphs((row * glyphColumns + col) * GlyphInfo.glyphHeight + i) = lineInt.U(8.W)
        }
      }
    }
    val currentDir = System.getProperty("user.dir")
    val hexTxtPath = Paths.get(currentDir, "verilog", f"${fontBitmapFilename}.txt")
    val writer = new FileWriter(hexTxtPath.toString)
    for (i <- glyphs.indices) {
      writer.write(f"@$i%x\n${glyphs(i).litValue}%02x\n")
    }
    writer.close()
    (hexTxtPath, glyphs.length)
  }
}
