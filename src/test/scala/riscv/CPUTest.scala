package riscv

import board.basys3.Top
import chisel3.stage.{ChiselGeneratorAnnotation, ChiselStage}
import chisel3.tester._
import org.scalatest._

class CPUTest extends FreeSpec with ChiselScalatestTester {
  var warned = false

  "CPU " - {
    "should compile to verilog" in {
      (new ChiselStage).execute(Array("-X", "verilog", "-td", "verilog"), Seq(ChiselGeneratorAnnotation(() => new Top)))
    }
  }
}
