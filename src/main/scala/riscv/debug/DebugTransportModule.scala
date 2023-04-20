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

package riscv.debug

import chisel3._

object DTMRegisters {
  val IDCODE = 0x01.U
  val DTMCS = 0x10.U
  val DMI = 0x11.U
  val BYPASS1F = 0x1F.U
}

class DebugTransportModule extends Module {
  val io = IO(new Bundle {

  })

  val idcode = 0x1e200151.U
  val dtmcs = RegInit("b00000000000000000101000011100001".U)
  val dmi = RegInit(UInt(48.W))
}
