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

package peripheral

import chisel3._
import riscv.Parameters

// TODO(howard): implementation
class InterruptController extends Module {
  val io = IO(new Bundle {
    val interrupts = Vec(Parameters.SlaveDeviceCount, Bool())
    val cpu_interrupt_flag = Output(UInt(Parameters.InterruptFlagWidth))
  })
}
