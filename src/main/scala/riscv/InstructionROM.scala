package riscv

import chisel3._

import java.nio.{ByteBuffer, ByteOrder}

class InstructionROM(asmBinaryFilename: String) extends Module {
  val io = IO(new Bundle {
    val instruction_address = Input(UInt(32.W))
    val instruction = Output(UInt(32.W))
  })

  val instructions = readAsmBinary(asmBinaryFilename)
  io.instruction := instructions(io.instruction_address / 4.U)

  def readAsmBinary(filename: String) = {
    val inputStream = getClass.getClassLoader.getResourceAsStream(filename)
    var instructions = new Array[UInt](0)
    val arr = new Array[Byte](4)
    while (inputStream.read(arr) == 4) {
      val instBuf = ByteBuffer.wrap(arr)
      instBuf.order(ByteOrder.LITTLE_ENDIAN)

      instructions = instructions :+ BigInt(instBuf.getInt() & 0xFFFFFFFFL).U(32.W)
    }
    VecInit(instructions)
  }
}
