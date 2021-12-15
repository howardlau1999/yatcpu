package riscv

import chisel3.stage.{ChiselGeneratorAnnotation, ChiselStage}

object VerilogGenerator extends App {
  (new ChiselStage).execute(Array("-X", "verilog", "-e", "verilog"), Seq(ChiselGeneratorAnnotation(() => new Top)))
}
