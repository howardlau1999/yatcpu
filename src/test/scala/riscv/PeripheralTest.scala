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

import bus.{AXI4LiteMaster, AXI4LiteMasterBundle, AXI4LiteSlave, AXI4LiteSlaveBundle}
import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import peripheral.{Memory, ROMLoader}

class TimerTest extends AnyFlatSpec with ChiselScalatestTester {
  class TestTimerLimit extends Module {
    val io = IO(new Bundle {
      val limit = Output(UInt())
      val bundle = new AXI4LiteMasterBundle(Parameters.AddrBits, Parameters.DataBits)
    })
    val timer = Module(new peripheral.Timer)
    val master = Module(new AXI4LiteMaster(Parameters.AddrBits, Parameters.DataBits))
    io.limit := timer.io.debug_limit
    master.io.bundle <> io.bundle
    timer.io.channels <> master.io.channels
  }

  behavior of "Timer"
  it should "read and write the limit" in {
    test(new TestTimerLimit).withAnnotations(TestAnnotations.annos) {
      c =>
        c.io.bundle.read.poke(false.B)
        c.io.bundle.write.poke(true.B)
        c.io.bundle.address.poke(0x4.U)
        c.io.bundle.write_data.poke(0x990315.U)
        c.clock.step()
        c.io.bundle.busy.expect(true.B)
        c.io.bundle.write.poke(false.B)
        c.io.bundle.address.poke(0x0.U)
        c.io.bundle.write_data.poke(0.U)
        c.clock.step(8)
        c.io.bundle.busy.expect(false.B)
        c.io.bundle.write_valid.expect(true.B)
        c.io.limit.expect(0x990315.U)
        c.io.bundle.read.poke(true.B)
        c.io.bundle.address.poke(0x4.U)
        c.clock.step()
        c.io.bundle.busy.expect(true.B)
        c.clock.step(6)
        c.io.bundle.busy.expect(false.B)
        c.io.bundle.read_valid.expect(true.B)
        c.io.bundle.read_data.expect(0x990315.U)
    }
  }
}

class MemoryTest extends AnyFlatSpec with ChiselScalatestTester {
  class MemoryTest extends Module {
    val io = IO(new Bundle {
      val bundle = new AXI4LiteMasterBundle(Parameters.AddrBits, Parameters.DataBits)

      val write_strobe = Input(UInt(4.W))
    })
    val memory = Module(new Memory(4096))
    val master = Module(new AXI4LiteMaster(Parameters.AddrBits, Parameters.DataBits))

    master.io.bundle <> io.bundle
    master.io.bundle.write_strobe := VecInit(io.write_strobe.asBools)
    master.io.channels <> memory.io.channels

    memory.io.debug_read_address := 0.U
  }

  behavior of "Memory"
  it should "perform read and write" in {
    test(new MemoryTest).withAnnotations(TestAnnotations.annos) { c =>
      c.io.bundle.read.poke(false.B)
      c.io.bundle.write.poke(true.B)
      c.io.write_strobe.poke(0xF.U)
      c.io.bundle.address.poke(0x4.U)
      c.io.bundle.write_data.poke(0xDEADBEEFL.U)
      c.clock.step()
      c.io.bundle.busy.expect(true.B)
      c.io.bundle.write.poke(false.B)
      c.io.bundle.address.poke(0x0.U)
      c.io.bundle.write_data.poke(0.U)
      c.clock.step(8)
      c.io.bundle.busy.expect(false.B)
      c.io.bundle.write_valid.expect(true.B)
      c.io.bundle.read.poke(true.B)
      c.io.bundle.address.poke(0x4.U)
      c.clock.step()
      c.io.bundle.busy.expect(true.B)
      c.clock.step(6)
      c.io.bundle.busy.expect(false.B)
      c.io.bundle.read_valid.expect(true.B)
      c.io.bundle.read_data.expect(0xDEADBEEFL.U)
    }
  }

}

class ROMLoaderTest extends AnyFlatSpec with ChiselScalatestTester {

  class ROMLoaderTest extends Module {
    val io = IO(new Bundle {
      val rom_address = Output(UInt(32.W))
      val rom_data = Input(UInt(32.W))
      val load_start = Input(Bool())
      val load_address = Input(UInt(32.W))
      val load_finished = Output(Bool())

      val bundle = new AXI4LiteSlaveBundle(32, 32)
    })

    val rom_loader = Module(new ROMLoader(2))
    rom_loader.io.rom_data := io.rom_data
    rom_loader.io.load_start := io.load_start
    rom_loader.io.load_address := io.load_address
    io.load_finished := rom_loader.io.load_finished
    io.rom_address := rom_loader.io.rom_address

    val slave = Module(new AXI4LiteSlave(Parameters.AddrBits, Parameters.DataBits))
    slave.io.bundle <> io.bundle
    slave.io.channels <> rom_loader.io.channels
    slave.io.bundle.read_data := 0.U
  }


  behavior of "ROMLoader"
  it should "load program" in {
    test(new ROMLoaderTest).withAnnotations(TestAnnotations.annos) { c =>
      c.io.load_address.poke(0x100.U)
      c.io.load_start.poke(true.B)
      c.clock.step()
      c.io.load_start.poke(false.B)
      c.io.rom_address.expect(0x0.U)
      c.clock.step(8)
      c.io.bundle.write.expect(true.B)
      c.io.bundle.address.expect(0x100.U)
      c.clock.step(4)
      c.io.rom_address.expect(0x1.U)
      c.clock.step(7)
      c.io.rom_address.expect(0x1.U)
      c.io.bundle.write.expect(true.B)
      c.io.bundle.address.expect(0x104.U)
      c.clock.step()
      c.io.rom_address.expect(0x1.U)
      c.clock.step(3)
      c.io.load_finished.expect(true.B)
    }
  }
}



