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
