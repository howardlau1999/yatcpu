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

package peripheral

import bus.{AXI4LiteChannels, AXI4LiteSlave}
import chisel3._
import chisel3.util.log2Up
import riscv.Parameters

class PixelDisplay extends Module {
  val io = IO(new Bundle() {
    val channels = Flipped(new AXI4LiteChannels(32, Parameters.DataBits))

    val x = Input(UInt(16.W))
    val y = Input(UInt(16.W))
    val video_on = Input(Bool())

    val rgb = Output(UInt(24.W))
  })
  val slave = Module(new AXI4LiteSlave(log2Up(CharacterBufferInfo.Chars), Parameters.DataBits))
  slave.io.channels <> io.channels

  // RGB565
  val mem = Module(new BlockRAM(320 * 240 * 2 / Parameters.WordSize))
  slave.io.bundle.read_valid := true.B
  mem.io.write_enable := slave.io.bundle.write
  mem.io.write_data := slave.io.bundle.write_data
  mem.io.write_address := slave.io.bundle.address
  mem.io.write_strobe := slave.io.bundle.write_strobe

  mem.io.read_address := slave.io.bundle.address
  slave.io.bundle.read_data := mem.io.read_data

  val pixel_y = Wire(UInt(8.W))
  val pixel_x = Wire(UInt(8.W))
  pixel_y := io.y >> 1
  pixel_x := io.x >> 1
  val word_address = (pixel_y * 320.U + pixel_x) >> 1
  mem.io.debug_read_address := word_address
  val two_pixels = mem.io.debug_read_data
  val pixel = Mux(pixel_x(0), two_pixels(31, 16), two_pixels(15, 0))
  val r = Wire(UInt(8.W))
  val g = Wire(UInt(8.W))
  val b = Wire(UInt(8.W))
  r := pixel(15, 11) << 3
  g := pixel(10, 5) << 2
  b := pixel(4, 0) << 3
  io.rgb := Mux(io.video_on, r ## g ## b, 0.U)
}
