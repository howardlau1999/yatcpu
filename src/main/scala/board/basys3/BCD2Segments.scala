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

package board.basys3

import chisel3._
import chisel3.util._

class BCD2Segments extends Module {
  val io = IO(new Bundle {
    val bcd = Input(UInt(4.W))
    val segs = Output(UInt(8.W))
  })

  val bcd = io.bcd
  val segs = Wire(UInt(8.W))

  segs := MuxLookup(bcd, 0xFF.U)(
    IndexedSeq(
      0.U -> "b10000001".U,
      1.U -> "b11001111".U,
      2.U -> "b10010010".U,
      3.U -> "b10000110".U,
      4.U -> "b11001100".U,
      5.U -> "b10100100".U,
      6.U -> "b10100000".U,
      7.U -> "b10001111".U,
      8.U -> "b10000000".U,
      9.U -> "b10000100".U,
      10.U -> "b00001000".U,
      11.U -> "b01100000".U,
      12.U -> "b00110001".U,
      13.U -> "b01000010".U,
      14.U -> "b00110000".U,
      15.U -> "b00111000".U,
    )
  )

  io.segs := segs
}

