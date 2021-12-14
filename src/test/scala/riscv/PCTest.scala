package riscv

import chisel3.iotesters.PeekPokeTester
import org.scalatest._
import chisel3._
import chisel3.util._
import chisel3.tester._
import chisel3.tester.RawTester.test

class PCTest extends FreeSpec with ChiselScalatestTester {
  "PC should " - {
    "be reset to 0 when reset is true" in {
      test(new PC) { c =>
        c.io.jump_enable.poke(false.B)
        c.io.jump_address.poke(0.U)
        c.io.hold_flag.poke(0.U)
        c.clock.step()
        c.io.pc.expect(0x4L.U)
        c.reset.poke(true.B)
        c.clock.step()
        c.reset.poke(false.B)
        c.io.pc.expect(0x0L.U)
      }
    }

    "jump when jump flag is true" in {
      test(new PC) { c =>
        c.io.jump_address.poke(0xDEADBEEFL.U)
        c.io.jump_enable.poke(true.B)
        c.io.hold_flag.poke(0.U)
        c.clock.step()
        c.io.pc.expect(0xDEADBEEFL.U)
      }
    }

    "hold when hold flag is true" in {
      test(new PC) { c =>
        c.io.jump_enable.poke(false.B)
        c.io.hold_flag.poke(0.U)
        c.clock.step()
        c.io.pc.expect(0x4L.U)

        c.io.hold_flag.poke(1.U)
        c.clock.step()
        c.io.pc.expect(0x4L.U)
      }
    }
  }
}
