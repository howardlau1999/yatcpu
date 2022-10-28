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

object DMRegisters {
  val DATA0 = 0x04.U
  val DATA11 = 0x0F.U
  val DMCONTROL = 0x10.U
  val DMSTATUS = 0x11.U
  val HARTINFO = 0x12.U
  val ABSTRACTCS = 0x16.U
  val COMMAND = 0x17.U
}

class DebugModule extends Module {
  val io = IO(new Bundle {

  })
}
