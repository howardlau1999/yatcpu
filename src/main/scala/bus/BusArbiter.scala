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

package bus

import chisel3._
import riscv.Parameters

class BusArbiter extends Module {
  val io = IO(new Bundle {
    val bus_request = Input(Vec(Parameters.MasterDeviceCount, Bool()))
    val bus_granted = Output(Vec(Parameters.MasterDeviceCount, Bool()))

    val ctrl_stall_flag = Output(Bool())
  })
  val granted = Wire(UInt())
  // Static Priority Arbitration
  // Higher number = Higher priority
  granted := 0.U
  for (i <- 0 until Parameters.MasterDeviceCount) {
    when(io.bus_request(i.U)) {
      granted := i.U
    }
  }
  for (i <- 0 until Parameters.MasterDeviceCount) {
    io.bus_granted(i.U) := i.U === granted
  }
  io.ctrl_stall_flag := !io.bus_granted(0.U)
}
