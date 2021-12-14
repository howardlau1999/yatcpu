package riscv

import chisel3._
import chisel3.tester._
import org.scalatest._

class CPUTest extends FreeSpec with ChiselScalatestTester {
  "CPU " - {
    "should execute lui and addi" in {
      test(new CPU) { c =>
        // li ra, 1
        c.io.instruction.poke(0x00100093L.U)
        // li ra, 1 now in IF->ID, decode unit reacts
        c.clock.step()
        c.io.debug_read_address.poke(0x1L.U)
        c.io.debug_read_data.expect(0x0L.U)
        // addi t5, ra, 1
        c.io.instruction.poke(0x00108f13L.U)
        // li ra, 1 now in ID->EX, execute unit reacts
        // addi t5, ra, 1 now in IF->ID, decode unit reacts
        c.clock.step()
        c.io.debug_read_data.expect(0x1L.U)
        // nop
        c.io.instruction.poke(0x00000013L.U)
        // addi t5, ra, 1 now in ID->EX, execute unit reacts
        c.clock.step()
        // nop
        c.clock.step()
        c.io.debug_read_address.poke(30.U)
        c.io.debug_read_data.expect(2.U)
        c.clock.step()
        c.io.debug_read_data.expect(2.U)
      }
    }
  }
}
