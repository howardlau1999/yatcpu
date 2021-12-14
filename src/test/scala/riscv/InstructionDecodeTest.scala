package riscv
import chisel3._
import chisel3.tester._
import org.scalatest._
class InstructionDecodeTest extends FreeSpec with ChiselScalatestTester {
  "ID should" - {
    "decode type I instructions" in {
      test(new InstructionDecode) {c=>

      }
    }
  }
}
