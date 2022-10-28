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

object ImplementationType {
  val ThreeStage = 0
  val FiveStage = 1
}

object Parameters {
  val AddrBits = 32
  val AddrWidth = AddrBits.W

  val InstructionBits = 32
  val InstructionWidth = InstructionBits.W
  val DataBits = 32
  val DataWidth = DataBits.W
  val ByteBits = 8
  val ByteWidth = ByteBits.W
  val WordSize = Math.ceil(DataBits / ByteBits).toInt

  val PhysicalRegisters = 32
  val PhysicalRegisterAddrBits = log2Up(PhysicalRegisters)
  val PhysicalRegisterAddrWidth = PhysicalRegisterAddrBits.W

  val CSRRegisterAddrBits = 12
  val CSRRegisterAddrWidth = CSRRegisterAddrBits.W

  val InterruptFlagBits = 32
  val InterruptFlagWidth = InterruptFlagBits.W

  val HoldStateBits = 3
  val StallStateWidth = HoldStateBits.W

  val MemorySizeInBytes = 32768
  val MemorySizeInWords = MemorySizeInBytes / 4

  val EntryAddress = 0x1000.U(Parameters.AddrWidth)

  val MasterDeviceCount = 1
  val SlaveDeviceCount = 8
  val SlaveDeviceCountBits = log2Up(Parameters.SlaveDeviceCount)
  // mmu
  val PageSize = 4096
  val PageOffsetBits = log2Up(Parameters.PageSize)
  val PageOffsetWidth = PageOffsetBits.W

  val PTESize = 32
  val PTEWidth = Parameters.PTESize.W


  val PhysicalPageCount = Math.ceil(MemorySizeInBytes / PageSize).toInt
}
