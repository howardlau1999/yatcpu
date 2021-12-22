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

class ProgramCounterTest extends FreeSpec with ChiselScalatestTester {
  "PC should " - {
    "be reset to entry address when reset is true" in {
      test(new ProgramCounter) { c =>
        c.io.jump_enable.poke(false.B)
        c.io.jump_address.poke(0.U)
        c.io.hold_flag.poke(0.U)
        c.clock.step()
        c.io.pc.expect((ProgramCounter.EntryAddress.litValue() + 0x4).U)
        c.reset.poke(true.B)
        c.clock.step()
        c.reset.poke(false.B)
        c.io.pc.expect(ProgramCounter.EntryAddress)
      }
    }

    "jump when jump flag is true" in {
      test(new ProgramCounter) { c =>
        c.io.jump_address.poke(0xDEADBEEFL.U)
        c.io.jump_enable.poke(true.B)
        c.io.hold_flag.poke(0.U)
        c.clock.step()
        c.io.pc.expect(0xDEADBEEFL.U)
      }
    }

    "hold when hold flag is true" in {
      test(new ProgramCounter) { c =>
        c.io.jump_enable.poke(false.B)
        c.io.hold_flag.poke(0.U)
        c.clock.step()
        c.io.pc.expect((ProgramCounter.EntryAddress.litValue() + 0x4).U)

        c.io.hold_flag.poke(1.U)
        c.clock.step()
        c.io.pc.expect((ProgramCounter.EntryAddress.litValue() + 0x4).U)
      }
    }
  }
}
