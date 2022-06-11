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
  val slave = Module(new AXI4LiteSlave(32, Parameters.DataBits))
  slave.io.channels <> io.channels

  // RGB565
  val mem = Module(new BlockRAM(320 * 240))
  slave.io.bundle.read_valid := true.B
  mem.io.write_enable := slave.io.bundle.write
  mem.io.write_data := slave.io.bundle.write_data
  mem.io.write_address := slave.io.bundle.address
  mem.io.write_strobe := slave.io.bundle.write_strobe

  mem.io.read_address := slave.io.bundle.address
  slave.io.bundle.read_data := mem.io.read_data

  val pixel_x = (io.x)(15, 1).asUInt
  val pixel_y = (io.y)(15, 1).asUInt
  mem.io.debug_read_address := (pixel_y * 320.U + pixel_x) << 2

  val pixel = mem.io.debug_read_data
  val r = pixel(23, 16)
  val g = pixel(15, 8)
  val b = pixel(7, 0)
  io.rgb := Mux(io.video_on, r ## g ## b, 0.U)
}
