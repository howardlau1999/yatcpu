package riscv

import chisel3.stage.{ChiselGeneratorAnnotation, ChiselStage}

object VerilogGenerator extends App {
  (new ChiselStage).execute(Array("-e", "verilog", "-td", "verilog"), Seq(ChiselGeneratorAnnotation(() => new Top)))
}
