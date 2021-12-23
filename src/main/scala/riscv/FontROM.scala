// Copyright 2021 Howard Lau
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

package riscv

import chisel3._
import chisel3.experimental.{ChiselAnnotation, annotate}
import chisel3.util._
import chisel3.util.experimental.loadMemoryFromFileInline
import firrtl.annotations.MemorySynthInit

import java.io.FileWriter
import java.nio.file.Paths
import javax.imageio.ImageIO

object GlyphInfo {
  val glyphWidth = 8
  val glyphHeight = 16
  // ASCII printable characters start from here
  val spaceIndex = 1
}

class FontROM(fontBitmapFilename: String = "vga_font_8x16.bmp") extends Module {
  val glyphWidth = GlyphInfo.glyphWidth
  val glyphHeight = GlyphInfo.glyphHeight

  val io = IO(new Bundle {
    val glyph_index = Input(UInt(7.W))
    val glyph_x = Input(UInt(4.W))
    val glyph_y = Input(UInt(4.W))

    val glyph_pixel_byte = Output(Bool())
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
      writer.write(f"@$i%x\n${glyphs(i).litValue()}%02x\n")
    }
    writer.close()
    (hexTxtPath, glyphCount)
  }
}