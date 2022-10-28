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

import bus.{AXI4LiteChannels, AXI4LiteSlave}
import chisel3._
import chisel3.util._
import riscv.Parameters

class UartIO extends DecoupledIO(UInt(8.W)) {
}


/**
 * Transmit part of the UART.
 * A minimal version without any additional buffering.
 * Use a ready/valid handshaking.
 */
class Tx(frequency: Int, baudRate: Int) extends Module {
  val io = IO(new Bundle {
    val txd = Output(UInt(1.W))
    val channel = Flipped(new UartIO())

  })

  val BIT_CNT = ((frequency + baudRate / 2) / baudRate - 1).U

  val shiftReg = RegInit(0x7ff.U)
  val cntReg = RegInit(0.U(20.W))
  val bitsReg = RegInit(0.U(4.W))

  io.channel.ready := (cntReg === 0.U) && (bitsReg === 0.U)
  io.txd := shiftReg(0)

  when(cntReg === 0.U) {

    cntReg := BIT_CNT
    when(bitsReg =/= 0.U) {
      val shift = shiftReg >> 1
      shiftReg := Cat(1.U, shift(9, 0))
      bitsReg := bitsReg - 1.U
    }.otherwise {
      when(io.channel.valid) {
        shiftReg := Cat(Cat(3.U, io.channel.bits), 0.U) // two stop bits, data, one start bit
        bitsReg := 11.U
      }.otherwise {
        shiftReg := 0x7ff.U
      }
    }

  }.otherwise {
    cntReg := cntReg - 1.U
  }
}

/**
 * Receive part of the UART.
 * A minimal version without any additional buffering.
 * Use a ready/valid handshaking.
 *
 * The following code is inspired by Tommy's receive code at:
 * https://github.com/tommythorn/yarvi
 */
class Rx(frequency: Int, baudRate: Int) extends Module {
  val io = IO(new Bundle {
    val rxd = Input(UInt(1.W))
    val channel = new UartIO()

  })

  val BIT_CNT = ((frequency + baudRate / 2) / baudRate - 1).U
  val START_CNT = ((3 * frequency / 2 + baudRate / 2) / baudRate - 1).U

  // Sync in the asynchronous RX data, reset to 1 to not start reading after a reset
  val rxReg = RegNext(RegNext(io.rxd, 1.U), 1.U)

  val shiftReg = RegInit(0.U(8.W))
  val cntReg = RegInit(0.U(20.W))
  val bitsReg = RegInit(0.U(4.W))
  val valReg = RegInit(false.B)

  when(cntReg =/= 0.U) {
    cntReg := cntReg - 1.U
  }.elsewhen(bitsReg =/= 0.U) {
    cntReg := BIT_CNT
    shiftReg := Cat(rxReg, shiftReg >> 1)
    bitsReg := bitsReg - 1.U
    // the last shifted in
    when(bitsReg === 1.U) {
      valReg := true.B
    }
  }.elsewhen(rxReg === 0.U) { // wait 1.5 bits after falling edge of start
    cntReg := START_CNT
    bitsReg := 8.U
  }

  when(valReg && io.channel.ready) {
    valReg := false.B
  }

  io.channel.bits := shiftReg
  io.channel.valid := valReg
}

/**
 * A single byte buffer with a ready/valid interface
 */
class Buffer extends Module {
  val io = IO(new Bundle {
    val in = Flipped(new UartIO())
    val out = new UartIO()
  })

  val empty :: full :: Nil = Enum(2)
  val stateReg = RegInit(empty)
  val dataReg = RegInit(0.U(8.W))

  io.in.ready := stateReg === empty
  io.out.valid := stateReg === full

  when(stateReg === empty) {
    when(io.in.valid) {
      dataReg := io.in.bits
      stateReg := full
    }
  }.otherwise { // full
    when(io.out.ready) {
      stateReg := empty
    }
  }
  io.out.bits := dataReg
}

/**
 * A transmitter with a single buffer.
 */
class BufferedTx(frequency: Int, baudRate: Int) extends Module {
  val io = IO(new Bundle {
    val txd = Output(UInt(1.W))
    val channel = Flipped(new UartIO())

  })
  val tx = Module(new Tx(frequency, baudRate))
  val buf = Module(new Buffer)

  buf.io.in <> io.channel
  tx.io.channel <> buf.io.out
  io.txd <> tx.io.txd
}

class Uart(frequency: Int, baudRate: Int) extends Module {
  val io = IO(new Bundle {
    val channels = Flipped(new AXI4LiteChannels(8, Parameters.DataBits))
    val rxd = Input(UInt(1.W))
    val txd = Output(UInt(1.W))

    val signal_interrupt = Output(Bool())
  })
  val interrupt = RegInit(false.B)
  val rxData = RegInit(0.U)
  val slave = Module(new AXI4LiteSlave(8, Parameters.DataBits))
  slave.io.channels <> io.channels

  val tx = Module(new BufferedTx(frequency, baudRate))
  val rx = Module(new Rx(frequency, baudRate))

  slave.io.bundle.read_data := 0.U
  slave.io.bundle.read_valid := true.B
  when(slave.io.bundle.read) {
    when(slave.io.bundle.address === 0x4.U) {
      slave.io.bundle.read_data := baudRate.U
    }.elsewhen(slave.io.bundle.address === 0xC.U) {
      slave.io.bundle.read_data := rxData
      interrupt := false.B
    }
  }

  tx.io.channel.valid := false.B
  tx.io.channel.bits := 0.U
  when(slave.io.bundle.write) {
    when(slave.io.bundle.address === 0x8.U) {
      interrupt := slave.io.bundle.write_data =/= 0.U
    }.elsewhen(slave.io.bundle.address === 0x10.U) {
      tx.io.channel.valid := true.B
      tx.io.channel.bits := slave.io.bundle.write_data
    }
  }

  io.txd := tx.io.txd
  rx.io.rxd := io.rxd

  io.signal_interrupt := interrupt
  rx.io.channel.ready := false.B
  when(rx.io.channel.valid) {
    rx.io.channel.ready := true.B
    rxData := rx.io.channel.bits
    interrupt := true.B
  }
}
