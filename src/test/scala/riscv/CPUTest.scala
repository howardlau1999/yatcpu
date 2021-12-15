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

    "should execute sb and lb" in {
      test(new CPU) { c =>
        /*
          li t0, 15
          sb t0, 4(x0)
          lb t1, 4(x0)
         */
        val instructions: Array[BigInt] = Array(
          0x00f00293L,
          0x00500223L,
          0x00400303L,
          0x00000013L,
          0x00000013L,
          0x00000013L,
          0x00000013L,
          0x00000013L,
          0x00000013L,
        )
        for (i <- 0 until 7) {
          val pc = c.io.instruction_address.peek().litValue() >> 2
          c.io.instruction.poke(instructions(pc.toInt).U)
          c.clock.step()
        }
        c.io.debug_read_address.poke(6.U)
        c.io.debug_read_data.expect(15.U)
      }
    }

    "should calculate 1+2+...+100=5050" in {
      test(new CPU) { c =>
        val instructions: Array[BigInt] = Array(
          /*
            li t0, 0 # int sum = 0;
            li t1, 1 # int i = 1;
            li t2, 100 # int to = 100;
            j cond
            add:
            add t0, t0, t1 # sum += i;
            addi t1, t1, 1 # i += 1;
            cond:
            bge t2, t1, add # while (100 >= i) goto add;
            end:
            j end
           */
          0x00000293L,
          0x00100313L,
          0x06400393L,
          0x00c0006fL,
          0x006282b3L,
          0x00130313L,
          0xfe63dce3L,
          0x0000006fL,
          0x00000013L,
          0x00000013L,
        )
        for (i <- 1 until 6000) {
          val pc = c.io.instruction_address.peek().litValue() >> 2
          c.io.instruction.poke(instructions(pc.toInt).U)
          c.clock.step()
        }
        c.io.debug_read_address.poke(5.U)
        c.io.debug_read_data.expect(5050.U)
      }
    }
  }
}
