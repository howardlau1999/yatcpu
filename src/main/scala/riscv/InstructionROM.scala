package riscv

import chisel3._

import java.nio.{ByteBuffer, ByteOrder}

class InstructionROM(asmBinaryFilename: String) extends Module {
  val io = IO(new Bundle {
    val instruction_address = Input(UInt(32.W))
    val instruction = Output(UInt(32.W))
  })

  val instructions = readAsmBinary(asmBinaryFilename)
  val instruction = RegInit(UInt(32.W), 0x13.U)
  instruction := instructions(io.instruction_address / 4.U)
  io.instruction := instruction

  def readAsmBinary(filename: String) = {
    val inputStream = getClass.getClassLoader.getResourceAsStream(filename)
    var instructions = new Array[UInt](0)
    val arr = new Array[Byte](4)
    while (inputStream.read(arr) == 4) {
      val instBuf = ByteBuffer.wrap(arr)
      instBuf.order(ByteOrder.LITTLE_ENDIAN)
      val inst = BigInt(instBuf.getInt() & 0xFFFFFFFFL)
      instructions = instructions :+ inst.U(32.W)
    }
    instructions = instructions :+ 0x00000013.U(32.W)
    instructions = instructions :+ 0x00000013.U(32.W)
    instructions = instructions :+ 0x00000013.U(32.W)
    VecInit(instructions)
  }
}
