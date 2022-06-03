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
import chisel3.experimental.ChiselEnum
import chisel3.util._
import riscv.Parameters

object AXI4 {
  val ProtWidth = 3
  val RespWidth = 2
  val BurstWidth = 8
}

class AXI4WriteAddressChannel(addrWidth: Int) extends Bundle {
  val AWID = Output(UInt(32.W))
  val AWVALID = Output(Bool())
  val AWREADY = Input(Bool())
  val AWADDR = Output(UInt(addrWidth.W))
  val AWPROT = Output(UInt(AXI4.ProtWidth.W))
  val AWSIZE = Output(UInt(8.W))
  val AWBURST = Output(UInt(AXI4.BurstWidth.W))
  val AWLOCK = Output(UInt(3.W))
  val AWCACHE = Output(UInt(4.W))
  val AWQOS = Output(UInt(4.W))
}

class AXI4WriteDataChannel(dataWidth: Int) extends Bundle {
  val WVALID = Output(Bool())
  val WREADY = Input(Bool())
  val WDATA = Output(UInt(dataWidth.W))
  val WSTRB = Output(UInt((dataWidth / 8).W))
  val WLAST = Output(Bool())
}

class AXI4WriteResponseChannel extends Bundle {
  val BID = Input(UInt(32.W))
  val BVALID = Input(Bool())
  val BREADY = Output(Bool())
  val BRESP = Input(UInt(AXI4.RespWidth.W))
}

class AXI4ReadAddressChannel(addrWidth: Int) extends Bundle {
  val ARID = Output(UInt(32.W))
  val ARVALID = Output(Bool())
  val ARREADY = Input(Bool())
  val ARADDR = Output(UInt(addrWidth.W))
  val ARPROT = Output(UInt(AXI4.ProtWidth.W))
  val ARSIZE = Output(UInt(8.W))
  val ARBURST = Output(UInt(3.W))
  val ARLOCK = Output(UInt(3.W))
  val ARCACHE = Output(UInt(4.W))
  val ARQOS = Output(UInt(4.W))
}

class AXI4ReadDataChannel(dataWidth: Int) extends Bundle {
  val RID = Input(UInt(32.W))
  val RVALID = Input(Bool())
  val RREADY = Output(Bool())
  val RDATA = Input(UInt(dataWidth.W))
  val RRESP = Input(UInt(AXI4.RespWidth.W))
  val RLAST = Input(Bool())
}

class AXI4Channels(addrWidth: Int, dataWidth: Int) extends Bundle {
  val write_address_channel = new AXI4WriteAddressChannel(addrWidth)
  val write_data_channel = new AXI4WriteDataChannel(dataWidth)
  val write_response_channel = new AXI4WriteResponseChannel()
  val read_address_channel = new AXI4ReadAddressChannel(addrWidth)
  val read_data_channel = new AXI4ReadDataChannel(dataWidth)
}

class AXI4SlaveBundle(addrWidth: Int, dataWidth: Int) extends Bundle {
  val read = Output(Bool())
  val write = Output(Bool())
  val read_data = Input(UInt(dataWidth.W))
  val read_valid = Input(Bool())
  val write_data = Output(UInt(dataWidth.W))
  val write_strobe = Output(Vec(Parameters.WordSize, Bool()))
  val address = Output(UInt(addrWidth.W))
}

class AXI4MasterBundle(addrWidth: Int, dataWidth: Int) extends Bundle {
  val read = Input(Bool())
  val write = Input(Bool())
  val read_data = Output(UInt(dataWidth.W))
  val write_data = Input(UInt(dataWidth.W))
  val write_strobe = Input(Vec(Parameters.WordSize, Bool()))
  val address = Input(UInt(addrWidth.W))

  val busy = Output(Bool())
  val read_valid = Output(Bool())
  val write_valid = Output(Bool())
}

object AXI4States extends ChiselEnum {
  val Idle, ReadAddr, ReadData, WriteAddr, WriteData, WriteResp = Value
}

// TODO(howard): implement full duplex
class AXI4Slave(addrWidth: Int, dataWidth: Int) extends Module {
  val io = IO(new Bundle {
    val channels = Flipped(new AXI4Channels(addrWidth, dataWidth))
    val bundle = new AXI4SlaveBundle(addrWidth, dataWidth)
  })
  val state = RegInit(AXI4States.Idle)
  val addr = RegInit(0.U(dataWidth.W))
  io.bundle.address := addr
  val read = RegInit(false.B)
  io.bundle.read := read
  val write = RegInit(false.B)
  io.bundle.write := write
  val write_data = RegInit(0.U(dataWidth.W))
  io.bundle.write_data := write_data
  val write_strobe = RegInit(VecInit(Seq.fill(Parameters.WordSize)(false.B)))
  io.bundle.write_strobe := write_strobe

  val ARREADY = RegInit(false.B)
  io.channels.read_address_channel.ARREADY := ARREADY
  val RVALID = RegInit(false.B)
  io.channels.read_data_channel.RVALID := RVALID
  val RRESP = WireInit(0.U(AXI4.RespWidth))
  io.channels.read_data_channel.RRESP := RRESP
  io.channels.read_data_channel.RID := 0.U
  io.channels.read_data_channel.RLAST := true.B

  io.channels.read_data_channel.RDATA := io.bundle.read_data

  val AWREADY = RegInit(false.B)
  io.channels.write_address_channel.AWREADY := AWREADY
  val WREADY = RegInit(false.B)
  io.channels.write_data_channel.WREADY := WREADY
  write_data := io.channels.write_data_channel.WDATA
  val BVALID = RegInit(false.B)
  io.channels.write_response_channel.BVALID := BVALID
  val BRESP = WireInit(0.U(AXI4.RespWidth))
  io.channels.write_response_channel.BRESP := BRESP
  io.channels.write_response_channel.BID := 0.U

  switch(state) {
    is(AXI4States.Idle) {
      read := false.B
      write := false.B
      RVALID := false.B
      BVALID := false.B
      when(io.channels.write_address_channel.AWVALID) {
        state := AXI4States.WriteAddr
      }.elsewhen(io.channels.read_address_channel.ARVALID) {
        state := AXI4States.ReadAddr
      }
    }
    is(AXI4States.ReadAddr) {
      ARREADY := true.B
      when(io.channels.read_address_channel.ARVALID && ARREADY) {
        state := AXI4States.ReadData
        addr := io.channels.read_address_channel.ARADDR
        read := true.B
        ARREADY := false.B
      }
    }
    is(AXI4States.ReadData) {
      RVALID := io.bundle.read_valid
      when(io.channels.read_data_channel.RREADY && RVALID) {
        state := AXI4States.Idle
        RVALID := false.B
      }
    }
    is(AXI4States.WriteAddr) {
      AWREADY := true.B
      when(io.channels.write_address_channel.AWVALID && AWREADY) {
        addr := io.channels.write_address_channel.AWADDR
        state := AXI4States.WriteData
        AWREADY := false.B
      }
    }
    is(AXI4States.WriteData) {
      WREADY := true.B
      when(io.channels.write_data_channel.WVALID && WREADY) {
        state := AXI4States.WriteResp
        write_data := io.channels.write_data_channel.WDATA
        write_strobe := io.channels.write_data_channel.WSTRB.asBools()
        write := true.B
        WREADY := false.B
      }
    }
    is(AXI4States.WriteResp) {
      WREADY := false.B
      BVALID := true.B
      when(io.channels.write_response_channel.BREADY && BVALID) {
        state := AXI4States.Idle
        write := false.B
        BVALID := false.B
      }
    }
  }
}

class AXI4Master(addrWidth: Int, dataWidth: Int) extends Module {
  val io = IO(new Bundle {
    val channels = new AXI4Channels(addrWidth, dataWidth)
    val bundle = new AXI4MasterBundle(addrWidth, dataWidth)
  })
  val state = RegInit(AXI4States.Idle)
  io.bundle.busy := state =/= AXI4States.Idle

  val addr = RegInit(0.U(dataWidth.W))
  val read_valid = RegInit(false.B)
  io.bundle.read_valid := read_valid
  val write_valid = RegInit(false.B)
  io.bundle.write_valid := write_valid
  val write_data = RegInit(0.U(dataWidth.W))
  val write_strobe = RegInit(VecInit(Seq.fill(Parameters.WordSize)(false.B)))
  val read_data = RegInit(0.U(dataWidth.W))

  io.channels.read_address_channel.ARID := 0.U
  io.channels.read_address_channel.ARLOCK := 0.U
  io.channels.read_address_channel.ARQOS := 0.U
  io.channels.read_address_channel.ARADDR := 0.U
  io.channels.read_address_channel.ARSIZE := 0.U
  io.channels.read_address_channel.ARCACHE := 0.U
  io.channels.read_address_channel.ARBURST := 0.U
  val ARVALID = RegInit(false.B)
  io.channels.read_address_channel.ARVALID := ARVALID
  io.channels.read_address_channel.ARPROT := 0.U
  val RREADY = RegInit(false.B)
  io.channels.read_data_channel.RREADY := RREADY

  io.bundle.read_data := io.channels.read_data_channel.RDATA
  val AWVALID = RegInit(false.B)
  io.channels.write_address_channel.AWID := 0.U
  io.channels.write_address_channel.AWADDR := 0.U
  io.channels.write_address_channel.AWVALID := AWVALID
  val WVALID = RegInit(false.B)
  io.channels.write_data_channel.WVALID := WVALID
  io.channels.write_data_channel.WDATA := write_data
  io.channels.write_data_channel.WLAST := true.B
  io.channels.write_address_channel.AWBURST := 1.U
  io.channels.write_address_channel.AWCACHE := 0.U
  io.channels.write_address_channel.AWPROT := 0.U
  io.channels.write_address_channel.AWSIZE := 0.U
  io.channels.write_address_channel.AWQOS := 0.U
  io.channels.write_address_channel.AWLOCK := 0.U
  io.channels.write_data_channel.WSTRB := write_strobe.asUInt
  val BREADY = RegInit(false.B)
  io.channels.write_response_channel.BREADY := BREADY

  switch(state) {
    is(AXI4States.Idle) {
      WVALID := false.B
      AWVALID := false.B
      ARVALID := false.B
      read_valid := false.B
      write_valid := false.B
      when(io.bundle.write) {
        state := AXI4States.WriteAddr
        addr := io.bundle.address
        write_data := io.bundle.write_data
        write_strobe := io.bundle.write_strobe
      }.elsewhen(io.bundle.read) {
        state := AXI4States.ReadAddr
        addr := io.bundle.address
      }
    }
    is(AXI4States.ReadAddr) {
      ARVALID := true.B
      when(io.channels.read_address_channel.ARREADY && ARVALID) {
        state := AXI4States.ReadData
        io.channels.read_address_channel.ARADDR := addr
        ARVALID := false.B
      }
    }
    is(AXI4States.ReadData) {
      RREADY := true.B
      when(io.channels.read_data_channel.RVALID && RREADY) {
        state := AXI4States.Idle
        read_valid := true.B
        read_data := io.channels.read_data_channel.RDATA
        RREADY := false.B
      }
    }
    is(AXI4States.WriteAddr) {
      AWVALID := true.B
      when(io.channels.write_address_channel.AWREADY && AWVALID) {
        state := AXI4States.WriteData
        io.channels.write_address_channel.AWADDR := addr
        AWVALID := false.B
      }
    }
    is(AXI4States.WriteData) {
      WVALID := true.B
      when(io.channels.write_data_channel.WREADY && WVALID) {
        state := AXI4States.WriteResp
        WVALID := false.B
      }
    }
    is(AXI4States.WriteResp) {
      BREADY := true.B
      when(io.channels.write_response_channel.BVALID && BREADY) {
        state := AXI4States.Idle
        write_valid := true.B
        BREADY := false.B
      }
    }
  }
}
