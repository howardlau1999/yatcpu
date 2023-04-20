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

class SYSULogo extends Module {
  val io = IO(new Bundle {
    val digit_mask = Input(UInt(4.W))
    val segs = Output(UInt(8.W))
  })

  io.segs := MuxLookup(io.digit_mask, "b00100100".U)(
    // "b0111".U, "b1101".U -> S
    IndexedSeq(
      "b1011".U -> "b01000100".U, // Y
      "b1110".U -> "b01000001".U, // U
    )
  )
}
