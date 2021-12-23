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

object AXI4Lite {
  val protWidth = 3
  val respWidth = 2
}

class AXI4LiteWriteAddressChannel(addrWidth: Int) extends Module {
  val io = IO(new Bundle {
    val AWVALID = Output(Bool())
    val AWREADY = Input(Bool())
    val AWADDR = Output(UInt(addrWidth.W))
    val AWPROT = Output(UInt(AXI4Lite.protWidth.W))
  })
}

class AXI4LiteWriteDataChannel(dataWidth: Int) extends  Module {
  val io = IO(new Bundle {
    val WVALID = Output(Bool())
    val WREADY = Input(Bool())
    val WDATA = Output(UInt(dataWidth.W))
    val WSTRB = Output(UInt((dataWidth / 8).W))
  })
}

class AXI4LiteWriteResponseChannel extends  Module {
  val io = IO(new Bundle {
    val BVALID = Input(Bool())
    val BREADY = Output(Bool())
    val BRESP = Input(UInt(AXI4Lite.respWidth.W))
  })
}

class AXI4LiteReadAddressChannel(addrWidth: Int) extends Module {
  val io = IO(new Bundle {
    val ARVALID = Output(Bool())
    val ARREADY = Input(Bool())
    val ARADDR = Output(UInt(addrWidth.W))
    val ARPROT = Output(UInt(AXI4Lite.protWidth.W))
  })
}

class AXI4LiteReadDataChannel(dataWidth: Int) extends Module {
  val io = IO(new Bundle {
    val RVALID = Input(Bool())
    val RREADY = Output(Bool())
    val RDATA = Input(UInt(dataWidth.W))
    val RRESP = Input(UInt(AXI4Lite.respWidth.W))
  })
}

class AXI4Lite(addrWidth: Int, dataWidth: Int) extends Bundle {
  val write_address_channel = new AXI4LiteWriteAddressChannel(addrWidth)
  val write_data_channel = new AXI4LiteWriteDataChannel(dataWidth)
  val write_response_channel = new AXI4LiteWriteResponseChannel()
  val read_address_channel = new AXI4LiteReadAddressChannel(addrWidth)
  val read_data_channel = new AXI4LiteReadDataChannel(dataWidth)
}
