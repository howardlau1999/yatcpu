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
import chisel3.tester._
import org.scalatest._

import java.nio.{ByteBuffer, ByteOrder}

class CPUTest extends FreeSpec with ChiselScalatestTester {
  class TestMemory(capacity: Int = 8192, asmBin: String) extends Module {
    val io = IO(new Bundle {
      val read_address = Input(UInt(Parameters.AddrWidth))
      val debug_read_address = Input(UInt(Parameters.AddrWidth))
      val instruction_read_address = Input(UInt(Parameters.AddrWidth))

      val write_address = Input(UInt(Parameters.AddrWidth))
      val write_enable = Input(Bool())
      val write_data = Input(UInt(Parameters.DataWidth))

      val read_data = Output(UInt(Parameters.DataWidth))
      val debug_read_data = Output(UInt(Parameters.DataWidth))
      val instruction_read_data = Output(UInt(Parameters.DataWidth))
    })
    val mem = RegInit(loadAsmBinary(asmBin))
    val read_data = Reg(UInt(Parameters.DataWidth))
    val inst_data = Reg(UInt(Parameters.DataWidth))
    val dbg_data = Reg(UInt(Parameters.DataWidth))
    read_data := mem(io.read_address / 4.U)
    inst_data := mem((io.instruction_read_address - Parameters.EntryAddress + 1024.U) / 4.U)
    dbg_data := mem(io.debug_read_address / 4.U)
    io.read_data := read_data
    io.instruction_read_data := inst_data
    io.debug_read_data := dbg_data

    when(io.write_enable) {
      mem(io.write_address / 4.U) := io.write_data
    }

    def loadAsmBinary(filename: String) = {
      val inputStream = getClass.getClassLoader.getResourceAsStream(filename)
      var instructions = new Array[BigInt](0)
      val arr = new Array[Byte](4)
      while (inputStream.read(arr) == 4) {
        val instBuf = ByteBuffer.wrap(arr)
        instBuf.order(ByteOrder.LITTLE_ENDIAN)
        val inst = BigInt(instBuf.getInt() & 0xFFFFFFFFL)
        instructions = instructions :+ inst
      }
      VecInit((Seq.fill(256)(BigInt(0)) ++ instructions).map(inst => inst.U(32.W)))
    }
  }

  class TestTopModule(exeFilename: String) extends Module {
    val io = IO(new Bundle {
      val mem_debug_read_address = Input(UInt(Parameters.AddrWidth))
      val regs_debug_read_address = Input(UInt(Parameters.PhysicalRegisterAddrWidth))
      val regs_debug_read_data = Output(UInt(Parameters.DataWidth))
      val mem_debug_read_data = Output(UInt(Parameters.DataWidth))

      val interrupt = Input(UInt(Parameters.InterruptFlagWidth))
    })
    val mem = Module(new TestMemory(8192, exeFilename))
    val cpu = Module(new CPU)
    
    mem.io.debug_read_address := io.mem_debug_read_address
    mem.io.instruction_read_address := cpu.io.instruction_read_address
    cpu.io.instruction_read_data := mem.io.instruction_read_data
    cpu.io.debug_read_address := io.regs_debug_read_address
    mem.io.read_address := cpu.io.mem_read_address
    cpu.io.mem_read_data := mem.io.read_data
    mem.io.write_enable := cpu.io.mem_write_enable
    mem.io.write_address := cpu.io.mem_write_address
    mem.io.write_data := cpu.io.mem_write_data
    io.regs_debug_read_data := cpu.io.debug_read_data
    io.mem_debug_read_data := mem.io.debug_read_data

    cpu.io.interrupt_flag := io.interrupt
  }

  "CPU " - {
    "should calculate recursive fibonacci" in {
      test(new TestTopModule("fibonacci.asmbin")) { c =>
        c.io.interrupt.poke(0.U)
        for (i <- 1 to 5000) {
          c.clock.step()
          c.io.mem_debug_read_address.poke((i * 4).U) // Avoid timeout
        }
        c.io.mem_debug_read_address.poke(4.U)
        c.clock.step()
        c.io.mem_debug_read_data.expect(55.U)
      }
    }

    "should quicksort 10 numbers" in {
      test(new TestTopModule("quicksort.asmbin")) { c =>
        c.io.interrupt.poke(0.U)
        for (i <- 1 to 3000) {
          c.clock.step()
          c.io.mem_debug_read_address.poke((i * 4).U) // Avoid timeout
        }
        for (i <- 1 to 10) {
          c.io.mem_debug_read_address.poke((4 * i).U)
          c.clock.step()
          c.io.mem_debug_read_data.expect((i - 1).U)
        }
      }
    }
  }
}
