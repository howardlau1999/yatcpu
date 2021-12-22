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
import chisel3.util._

// TODO(howard): connect to the bus
// TODO(howard): allow setting different frequency
class Timer extends Module{
  val io = IO(new Bundle{
    val signal_interrupt = Output(Bool())
  })
  val count = RegInit(0.U(32.W))
  val limit = 100000000.U(32.W)

  io.signal_interrupt := count >= (limit - 10.U)

  when(count >= limit) {
    count := 0.U
  }.otherwise {
    count := count + 1.U
  }
}
