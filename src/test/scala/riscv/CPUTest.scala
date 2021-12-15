package riscv

import chisel3._
import chisel3.tester._
import org.scalatest._

class CPUTest extends FreeSpec with ChiselScalatestTester {
  var warned = false

  def run_instructions(instructions: Array[UInt], c: CPU, cycles: Int) = {
    warned = false
    for (i <- 1 to cycles) {
      if (i % 1000 == 0) println(f"$i%d cycles...")
      val pc = c.io.instruction_address.peek().litValue() >> 2
      if (pc >= instructions.length) {
        if (!warned) {
          println(f"Out of range at cycle $i%d pc=0x$i%x")
          warned = true
        }
        c.io.instruction.poke(0x00000013.U)
      } else {
        c.io.instruction.poke(instructions(pc.toInt))
      }
      c.clock.step()
    }
  }

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
        val instructions: Array[UInt] = Array(
          0x00f00293L.U,
          0x00500223L.U,
          0x00400303L.U,
          0x00000013L.U,
          0x00000013L.U,
          0x00000013L.U,
          0x00000013L.U,
          0x00000013L.U,
          0x00000013L.U,
        )
        run_instructions(instructions, c, 7)
        c.io.debug_read_address.poke(6.U)
        c.io.debug_read_data.expect(15.U)
      }
    }

    "should calculate 1+2+...+100=5050" in {
      test(new CPU) { c =>
        val instructions: Array[UInt] = Array(
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
          0x00000293L.U,
          0x00100313L.U,
          0x06400393L.U,
          0x00c0006fL.U,
          0x006282b3L.U,
          0x00130313L.U,
          0xfe63dce3L.U,
          0x0000006fL.U,
          0x00000013L.U,
          0x00000013L.U,
        )
        run_instructions(instructions, c, 6000)
        c.io.debug_read_address.poke(5.U)
        c.io.debug_read_data.expect(5050.U)
      }
    }

    "should execute recursive fibonacci" in {
      test(new CPU) { c =>
        val instructions: Array[UInt] = Array(
          0x3fc00113L.U,
          0x094000efL.U,
          0x0000006fL.U,
          0x00000013L.U,
          0x00000013L.U,
          0x00000013L.U,
          0x00000013L.U,
          0xfe010113L.U,
          0x00112e23L.U,
          0x00812c23L.U,
          0x00912a23L.U,
          0x02010413L.U,
          0xfea42623L.U,
          0xfec42703L.U,
          0x00100793L.U,
          0x00f70863L.U,
          0xfec42703L.U,
          0x00200793L.U,
          0x00f71663L.U,
          0x00100793L.U,
          0x0300006fL.U,
          0xfec42783L.U,
          0xfff78793L.U,
          0x00078513L.U,
          0xfbdff0efL.U,
          0x00050493L.U,
          0xfec42783L.U,
          0xffe78793L.U,
          0x00078513L.U,
          0xfa9ff0efL.U,
          0x00050793L.U,
          0x00f487b3L.U,
          0x00078513L.U,
          0x01c12083L.U,
          0x01812403L.U,
          0x01412483L.U,
          0x02010113L.U,
          0x00008067L.U,
          0xff010113L.U,
          0x00112623L.U,
          0x00812423L.U,
          0x00912223L.U,
          0x01010413L.U,
          0x00400493L.U,
          0x00a00513L.U,
          0xf69ff0efL.U,
          0x00050793L.U,
          0x00f4a023L.U,
          0x00000793L.U,
          0x00078513L.U,
          0x00c12083L.U,
          0x00812403L.U,
          0x00412483L.U,
          0x01010113L.U,
          0x00008067L.U,
        )
        run_instructions(instructions, c, 3500)
        c.io.debug_mem_read_address.poke(4.U)
        c.io.debug_mem_read_data.expect(55.U)
      }
    }

    "should quick sort 10 numbers" in {
      test(new CPU) { c =>
        val instructions: Array[UInt] = Array(
          0x00001137L.U,
          0xffc10113L.U,
          0x194000efL.U,
          0x0000006fL.U,
          0x00000013L.U,
          0x00000013L.U,
          0x00000013L.U,
          0x00000013L.U,
          0xfd010113L.U,
          0x02112623L.U,
          0x02812423L.U,
          0x03010413L.U,
          0xfca42e23L.U,
          0xfcb42c23L.U,
          0xfcc42a23L.U,
          0xfd842703L.U,
          0xfd442783L.U,
          0x14f75263L.U,
          0xfd842783L.U,
          0x00279793L.U,
          0xfdc42703L.U,
          0x00f707b3L.U,
          0x0007a783L.U,
          0xfef42223L.U,
          0xfd842783L.U,
          0xfef42623L.U,
          0xfd442783L.U,
          0xfef42423L.U,
          0x0c00006fL.U,
          0xfe842783L.U,
          0xfff78793L.U,
          0xfef42423L.U,
          0xfe842783L.U,
          0x00279793L.U,
          0xfdc42703L.U,
          0x00f707b3L.U,
          0x0007a783L.U,
          0xfe442703L.U,
          0x00e7c863L.U,
          0xfec42703L.U,
          0xfe842783L.U,
          0xfcf748e3L.U,
          0xfe842783L.U,
          0x00279793L.U,
          0xfdc42703L.U,
          0x00f70733L.U,
          0xfec42783L.U,
          0x00279793L.U,
          0xfdc42683L.U,
          0x00f687b3L.U,
          0x00072703L.U,
          0x00e7a023L.U,
          0x0100006fL.U,
          0xfec42783L.U,
          0x00178793L.U,
          0xfef42623L.U,
          0xfec42783L.U,
          0x00279793L.U,
          0xfdc42703L.U,
          0x00f707b3L.U,
          0x0007a783L.U,
          0xfe442703L.U,
          0x00e7d863L.U,
          0xfec42703L.U,
          0xfe842783L.U,
          0xfcf748e3L.U,
          0xfec42783L.U,
          0x00279793L.U,
          0xfdc42703L.U,
          0x00f70733L.U,
          0xfe842783L.U,
          0x00279793L.U,
          0xfdc42683L.U,
          0x00f687b3L.U,
          0x00072703L.U,
          0x00e7a023L.U,
          0xfec42703L.U,
          0xfe842783L.U,
          0xf4f744e3L.U,
          0xfec42783L.U,
          0x00279793L.U,
          0xfdc42703L.U,
          0x00f707b3L.U,
          0xfe442703L.U,
          0x00e7a023L.U,
          0xfec42783L.U,
          0xfff78793L.U,
          0x00078613L.U,
          0xfd842583L.U,
          0xfdc42503L.U,
          0xeb9ff0efL.U,
          0xfec42783L.U,
          0x00178793L.U,
          0xfd442603L.U,
          0x00078593L.U,
          0xfdc42503L.U,
          0xea1ff0efL.U,
          0x0080006fL.U,
          0x00000013L.U,
          0x02c12083L.U,
          0x02812403L.U,
          0x03010113L.U,
          0x00008067L.U,
          0xfc010113L.U,
          0x02112e23L.U,
          0x02812c23L.U,
          0x04010413L.U,
          0x00600793L.U,
          0xfcf42223L.U,
          0x00200793L.U,
          0xfcf42423L.U,
          0x00400793L.U,
          0xfcf42623L.U,
          0x00500793L.U,
          0xfcf42823L.U,
          0x00300793L.U,
          0xfcf42a23L.U,
          0x00100793L.U,
          0xfcf42c23L.U,
          0xfc042e23L.U,
          0x00900793L.U,
          0xfef42023L.U,
          0x00700793L.U,
          0xfef42223L.U,
          0x00800793L.U,
          0xfef42423L.U,
          0xfc440793L.U,
          0x00900613L.U,
          0x00000593L.U,
          0x00078513L.U,
          0xe19ff0efL.U,
          0x00100793L.U,
          0xfef42623L.U,
          0x0340006fL.U,
          0xfec42783L.U,
          0xfff78793L.U,
          0xfec42703L.U,
          0x00271713L.U,
          0x00279793L.U,
          0xff040693L.U,
          0x00f687b3L.U,
          0xfd47a783L.U,
          0x00f72023L.U,
          0xfec42783L.U,
          0x00178793L.U,
          0xfef42623L.U,
          0xfec42703L.U,
          0x00a00793L.U,
          0xfce7d4e3L.U,
          0x00000793L.U,
          0x00078513L.U,
          0x03c12083L.U,
          0x03812403L.U,
          0x04010113L.U,
          0x00008067L.U,
        )
        run_instructions(instructions, c, 1550)
        for (i <- 1 to 10) {
          c.io.debug_mem_read_address.poke((4 * i).U)
          c.io.debug_mem_read_data.expect((i - 1).U)
        }
      }
    }
  }
}
