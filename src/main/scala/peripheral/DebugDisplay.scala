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

import chisel3._

class DebugDisplay extends Module {
  val io = IO(new Bundle {
    val x = Input(UInt(16.W))
    val y = Input(UInt(16.W))
    val video_on = Input(Bool())

    val rgb = Output(UInt(24.W))
  })

  val rgb = Wire(UInt(24.W))

  rgb := io.y(7, 0) ## io.x(9, 0)
  when(io.x < 32.U) {
    rgb := 0x0000FF.U
  }.elsewhen(32.U <= io.x < 64.U) {
    rgb := 0x00FF00.U
  }.elsewhen(64.U <= io.x < 96.U) {
    rgb := 0xFF0000.U
  }.elsewhen(96.U <= io.x < 128.U) {
    rgb := 0x00FFFF.U
  }.elsewhen(128.U <= io.x < 160.U) {
    rgb := 0xFFFF00.U
  }.elsewhen(160.U <= io.x < 192.U) {
    rgb := 0xFF00FF.U
  }.elsewhen(192.U <= io.x < 224.U) {
    rgb := 0x000000.U
  }.elsewhen(224.U <= io.x < 256.U) {
    rgb := 0xFFFFFF.U
  }
  when(io.y >= 256.U || io.x >= 256.U) {
    rgb := io.y(7, 0) ## io.x(9, 0)
  }

  io.rgb := Mux(io.video_on, rgb, 0.U)
}
