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

class SegmentMux extends Module {
  val io = IO(new Bundle {
    val digit_mask = Input(UInt(4.W))
    val numbers = Input(UInt(16.W))
    val segs = Output(UInt(8.W))
  })


  val digit = RegInit(UInt(4.W), 0.U)
  val bcd2segs = Module(new BCD2Segments)

  bcd2segs.io.bcd := digit
  io.segs := bcd2segs.io.segs
  digit := MuxLookup(io.digit_mask, io.numbers(3, 0))(
    // "b1110".U
    IndexedSeq(
      "b1101".U -> io.numbers(7, 4),
      "b1011".U -> io.numbers(11, 8),
      "b0111".U -> io.numbers(15, 12)
    )
  )
}
