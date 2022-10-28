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

import chisel3._
import riscv.Parameters

class BusBundle extends Bundle {
  val read = Output(Bool())
  val address = Output(UInt(Parameters.AddrWidth))
  val read_data = Input(UInt(Parameters.DataWidth))
  val read_valid = Input(Bool())
  val write = Output(Bool())
  val write_data = Output(UInt(Parameters.DataWidth))
  val write_strobe = Output(Vec(Parameters.WordSize, Bool()))
  val write_valid = Input(Bool())
  val busy = Input(Bool())
  val request = Output(Bool())
  val granted = Input(Bool())
}
