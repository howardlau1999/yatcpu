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
import chisel3.experimental.ChiselEnum
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

class AXI4LiteWriteDataChannel(dataWidth: Int) extends Module {
  val io = IO(new Bundle {
    val WVALID = Output(Bool())
    val WREADY = Input(Bool())
    val WDATA = Output(UInt(dataWidth.W))
    val WSTRB = Output(UInt((dataWidth / 8).W))
  })
}

class AXI4LiteWriteResponseChannel extends Module {
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

class AXI4LiteChannels(addrWidth: Int, dataWidth: Int) extends Bundle {
  val write_address_channel = new AXI4LiteWriteAddressChannel(addrWidth)
  val write_data_channel = new AXI4LiteWriteDataChannel(dataWidth)
  val write_response_channel = new AXI4LiteWriteResponseChannel()
  val read_address_channel = new AXI4LiteReadAddressChannel(addrWidth)
  val read_data_channel = new AXI4LiteReadDataChannel(dataWidth)
}

object AXI4LiteStates extends ChiselEnum {
  val Idle, ReadAddr, ReadData, WriteAddr, WriteData, WriteResp = Value
}

class AXI4LiteSlave(addrWidth: Int, dataWidth: Int) extends Module {
  val io = IO(new Bundle {
    val channels = Flipped(new AXI4LiteChannels(addrWidth, dataWidth))

    val read = Output(Bool())
    val write = Output(Bool())
    val read_data = Input(UInt(dataWidth.W))
    val write_data = Output(UInt(dataWidth.W))
    val address = Output(UInt(addrWidth.W))
  })
  val state = RegInit(AXI4LiteStates.Idle)
  val addr = RegInit(0.U(dataWidth.W))
  io.address := addr
  val read = RegInit(false.B)
  io.read := read
  val write = RegInit(false.B)
  io.write := write
  val write_data = RegInit(0.U(dataWidth.W))
  io.write_data := write_data

  val ARREADY = RegInit(false.B)
  io.channels.read_address_channel.io.ARREADY := ARREADY
  val RVALID = RegInit(false.B)
  io.channels.read_data_channel.io.RVALID := RVALID
  val RRESP = WireInit(0.U(AXI4Lite.respWidth))
  io.channels.read_data_channel.io.RRESP := RRESP

  io.channels.read_data_channel.io.RDATA := io.read_data

  val AWREADY = RegInit(false.B)
  io.channels.write_address_channel.io.AWREADY := AWREADY
  val WREADY = RegInit(false.B)
  io.channels.write_data_channel.io.WREADY := WREADY
  write_data := io.channels.write_data_channel.io.WDATA
  val BVALID = RegInit(false.B)
  io.channels.write_response_channel.io.BVALID := BVALID
  val BRESP = WireInit(0.U(AXI4Lite.respWidth))
  io.channels.write_response_channel.io.BRESP := BRESP

  switch(state) {
    is(AXI4LiteStates.Idle) {
      read := false.B
      write := false.B
      RVALID := false.B
      BVALID := false.B
      when(io.channels.write_address_channel.io.AWVALID) {
        state := AXI4LiteStates.WriteAddr
      }.elsewhen(io.channels.read_address_channel.io.ARVALID) {
        state := AXI4LiteStates.ReadAddr
      }
    }
    is(AXI4LiteStates.ReadAddr) {
      ARREADY := true.B
      when(io.channels.read_address_channel.io.ARVALID && ARREADY) {
        state := AXI4LiteStates.ReadData
        addr := io.channels.read_address_channel.io.ARADDR
        read := true.B
        ARREADY := false.B
      }
    }
    is(AXI4LiteStates.ReadData) {
      RVALID := true.B
      when(io.channels.read_data_channel.io.RREADY && RVALID) {
        state := AXI4LiteStates.Idle
        RVALID := false.B
      }
    }
    is(AXI4LiteStates.WriteAddr) {
      AWREADY := true.B
      when(io.channels.write_address_channel.io.AWVALID && AWREADY) {
        addr := io.channels.write_address_channel.io.AWADDR
        state := AXI4LiteStates.WriteData
        AWREADY := false.B
      }
    }
    is(AXI4LiteStates.WriteData) {
      WREADY := true.B
      when(io.channels.write_data_channel.io.WVALID && WREADY) {
        state := AXI4LiteStates.WriteResp
        write_data := io.channels.write_data_channel.io.WDATA
        write := true.B
        WREADY := false.B
      }
    }
    is(AXI4LiteStates.WriteResp) {
      WREADY := false.B
      BVALID := true.B
      when(io.channels.write_response_channel.io.BREADY && BVALID) {
        state := AXI4LiteStates.Idle
        BVALID := false.B
      }
    }
  }
}

class AXI4LiteMaster(addrWidth: Int, dataWidth: Int) extends Module {
  val io = IO(new Bundle {
    val channels = new AXI4LiteChannels(addrWidth, dataWidth)

    val read = Input(Bool())
    val write = Input(Bool())
    val read_data = Output(UInt(dataWidth.W))
    val write_data = Input(UInt(dataWidth.W))
    val address = Input(UInt(addrWidth.W))

    val busy = Output(Bool())
    val read_ok = Output(Bool())
    val write_ok = Output(Bool())
  })
  val state = RegInit(AXI4LiteStates.Idle)
  io.busy := state =/= AXI4LiteStates.Idle

  val addr = RegInit(0.U(dataWidth.W))
  val read_ok = RegInit(false.B)
  io.read_ok := read_ok
  val write_ok = RegInit(false.B)
  io.write_ok := write_ok
  val write_data = RegInit(0.U(dataWidth.W))
  val read_data = RegInit(0.U(dataWidth.W))

  val ARVALID = RegInit(false.B)
  io.channels.read_address_channel.io.ARVALID := ARVALID
  val RREADY = RegInit(false.B)
  io.channels.read_data_channel.io.RREADY := RREADY

  io.read_data := io.channels.read_data_channel.io.RDATA
  val AWVALID = RegInit(false.B)
  io.channels.write_address_channel.io.AWVALID := AWVALID
  val WVALID = RegInit(false.B)
  io.channels.write_data_channel.io.WVALID := WVALID
  io.channels.write_data_channel.io.WDATA := write_data
  val BREADY = RegInit(false.B)
  io.channels.write_response_channel.io.BREADY := BREADY

  switch(state) {
    is(AXI4LiteStates.Idle) {
      WVALID := false.B
      AWVALID := false.B
      ARVALID := false.B
      read_ok := false.B
      write_ok := false.B
      when(io.write) {
        state := AXI4LiteStates.WriteAddr
        addr := io.address
        write_data := io.write_data
      }.elsewhen(io.read) {
        state := AXI4LiteStates.ReadAddr
        addr := io.address
      }
    }
    is(AXI4LiteStates.ReadAddr) {
      ARVALID := true.B
      when(io.channels.read_address_channel.io.ARREADY && ARVALID) {
        state := AXI4LiteStates.ReadData
        io.channels.read_address_channel.io.ARADDR := addr
        ARVALID := false.B
      }
    }
    is(AXI4LiteStates.ReadData) {
      RREADY := true.B
      when(io.channels.read_data_channel.io.RVALID && RREADY) {
        state := AXI4LiteStates.Idle
        read_ok := true.B
        read_data := io.channels.read_data_channel.io.RDATA
        RREADY := false.B
      }
    }
    is(AXI4LiteStates.WriteAddr) {
      AWVALID := true.B
      when(io.channels.write_address_channel.io.AWREADY && AWVALID) {
        state := AXI4LiteStates.WriteData
        io.channels.write_address_channel.io.AWADDR := addr
        AWVALID := false.B
      }
    }
    is(AXI4LiteStates.WriteData) {
      WVALID := true.B
      when(io.channels.write_data_channel.io.WREADY && WVALID) {
        state := AXI4LiteStates.WriteResp
        io.channels.write_data_channel.io.WDATA := write_data
        WVALID := false.B
      }
    }
    is(AXI4LiteStates.WriteResp) {
      BREADY := true.B
      when(io.channels.write_response_channel.io.BVALID && BREADY) {
        state := AXI4LiteStates.Idle
        write_ok := true.B
        BREADY := false.B
      }
    }
  }
}
