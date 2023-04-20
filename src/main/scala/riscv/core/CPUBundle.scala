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

package riscv.core

import bus.AXI4LiteChannels
import chisel3._
import riscv.Parameters

class CPUBundle extends Bundle {
  val axi4_channels = new AXI4LiteChannels(Parameters.AddrBits, Parameters.DataBits)
  val bus_address = Output(UInt(Parameters.AddrWidth))
  val interrupt_flag = Input(UInt(Parameters.InterruptFlagWidth))
  val stall_flag_bus = Input(Bool())
  val debug_read_address = Input(UInt(Parameters.PhysicalRegisterAddrWidth))
  val debug_read_data = Output(UInt(Parameters.DataWidth))

  val instruction_valid = Input(Bool())
  val bus_busy = Output(Bool())
  val debug = Output(Vec(6, UInt(32.W)))
}
