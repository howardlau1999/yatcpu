package board.basys3

import chisel3.stage.{ChiselGeneratorAnnotation, ChiselStage}

object VerilogGenerator extends App {
  (new ChiselStage).execute(Array("-X", "verilog", "-td", "verilog"), Seq(ChiselGeneratorAnnotation(() => new Top)))
}
