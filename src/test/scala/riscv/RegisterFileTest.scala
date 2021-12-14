package riscv

import chisel3._
import chisel3.tester._
import org.scalatest._

class RegisterFileTest extends FreeSpec with ChiselScalatestTester {
  "Register file " - {
    "should read the written content" in {
      test(new RegisterFile) { c =>
        timescope {
          c.io.write_enable.poke(true.B)
          c.io.write_address.poke(1.U)
          c.io.write_data.poke(0xDEADBEEFL.U)
          c.clock.step()
        }
        c.io.read_address1.poke(1.U)
        c.io.read_data1.expect(0xDEADBEEFL.U)
      }
    }

    "x0 should always be zero" in {
      test(new RegisterFile) { c =>
        timescope {
          c.io.write_enable.poke(true.B)
          c.io.write_address.poke(0.U)
          c.io.write_data.poke(0xDEADBEEFL.U)
          c.clock.step()
        }
        c.io.read_address1.poke(0.U)
        c.io.read_data1.expect(0.U)
      }
    }

    "should read the writing content" in {
      test(new RegisterFile) { c =>
        timescope {
          c.io.read_address1.poke(2.U)
          c.io.read_data1.expect(0.U)
          c.io.write_enable.poke(true.B)
          c.io.write_address.poke(2.U)
          c.io.write_data.poke(0xDEADBEEFL.U)
          c.io.read_address1.poke(2.U)
          c.io.read_data1.expect(0xDEADBEEFL.U)
          c.clock.step()
        }
        c.io.read_address1.poke(2.U)
        c.io.read_data1.expect(0xDEADBEEFL.U)
      }
    }
  }
}
