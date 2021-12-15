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

    "should execute recursive fibonacci" in {
      test(new CPU) { c =>
        val instructions: Array[BigInt] = Array(
          0x3fc00113L,
          0x094000efL,
          0x0000006fL,
          0x00000013L,
          0x00000013L,
          0x00000013L,
          0x00000013L,
          0xfe010113L,
          0x00112e23L,
          0x00812c23L,
          0x00912a23L,
          0x02010413L,
          0xfea42623L,
          0xfec42703L,
          0x00100793L,
          0x00f70863L,
          0xfec42703L,
          0x00200793L,
          0x00f71663L,
          0x00100793L,
          0x0300006fL,
          0xfec42783L,
          0xfff78793L,
          0x00078513L,
          0xfbdff0efL,
          0x00050493L,
          0xfec42783L,
          0xffe78793L,
          0x00078513L,
          0xfa9ff0efL,
          0x00050793L,
          0x00f487b3L,
          0x00078513L,
          0x01c12083L,
          0x01812403L,
          0x01412483L,
          0x02010113L,
          0x00008067L,
          0xff010113L,
          0x00112623L,
          0x00812423L,
          0x00912223L,
          0x01010413L,
          0x00400493L,
          0x00a00513L,
          0xf69ff0efL,
          0x00050793L,
          0x00f4a023L,
          0x00000793L,
          0x00078513L,
          0x00c12083L,
          0x00812403L,
          0x00412483L,
          0x01010113L,
          0x00008067L,
          0x00000013L,
          0x00000013L,
          0x00000013L,
        )
        for (i <- 1 until 10000) {
          val pc = c.io.instruction_address.peek().litValue() >> 2
          c.io.instruction.poke(instructions(pc.toInt).U)
          c.clock.step()
        }
        c.io.debug_mem_read_address.poke(4.U)
        c.io.debug_mem_read_data.expect(55.U)
      }
    }
  }
}
