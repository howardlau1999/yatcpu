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

import board.basys3.Top
import chisel3._
import chisel3.stage.{ChiselGeneratorAnnotation, ChiselStage}
import chisel3.tester._
import org.scalatest._

class BusTest extends FreeSpec with ChiselScalatestTester {
  class TestTimerLimit extends Module {
    val io = IO(new Bundle {
      val limit = Output(UInt())
      val bundle = new AXI4LiteMasterBundle(Parameters.AddrBits, Parameters.DataBits)
    })
    val timer = Module(new Timer)
    val master = Module(new AXI4LiteMaster(Parameters.AddrBits, Parameters.DataBits))
    io.limit := timer.io.debug_limit
    master.io.bundle <> io.bundle
    timer.io.channels <> master.io.channels
  }

  "Timer" - {
    "should be able to read and write limit" in {
      test(new TestTimerLimit) { c =>
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
        c.io.bundle.write_ok.expect(true.B)
        c.io.limit.expect(0x990315.U)
        c.io.bundle.read.poke(true.B)
        c.io.bundle.address.poke(0x4.U)
        c.clock.step()
        c.io.bundle.busy.expect(true.B)
        c.clock.step(6)
        c.io.bundle.busy.expect(false.B)
        c.io.bundle.read_ok.expect(true.B)
        c.io.bundle.read_data.expect(0x990315.U)
      }
    }
  }
}
