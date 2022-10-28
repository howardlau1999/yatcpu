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

class OnboardDigitDisplay extends Module {
  val io = IO(new Bundle {
    val digit_mask = Output(UInt(4.W))
  })

  val counter = RegInit(UInt(16.W), 0.U)
  val digit_mask = RegInit(UInt(4.W), "b0111".U)

  counter := counter + 1.U
  when(counter === 0.U) {
    digit_mask := (digit_mask << 1.U).asUInt + digit_mask(3)
  }
  io.digit_mask := digit_mask
}
