package riscv

import chisel3.iotesters.PeekPokeTester
import org.scalatest._
import chisel3._
import chisel3.util._
import chisel3.tester._
import chisel3.tester.RawTester.test

import scala.util.Random

class PCNextTest extends FreeSpec with ChiselScalatestTester {
  "PCNext = PC + 4" in {
    test(new PCNext) { c =>
      val cycles = 10000
      for (i <- 0 until cycles) {
        val pc = Random.nextInt(0x1000000) * 4
        c.io.pc.poke(pc.U)
        c.io.pc_next.expect((pc + 4).U)
      }
    }
  }

  "PCNext = 0 when PC = 0xFFFFFFFC" in {
    test(new PCNext) { c =>
      c.io.pc.poke(0xFFFFFFFCL.U)
      c.io.pc_next.expect(0.U)
    }
  }
}
