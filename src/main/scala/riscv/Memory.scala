package riscv

import chisel3._
import chisel3.experimental._
import chisel3.util._
import chisel3.util.experimental._
import firrtl.annotations.MemorySynthInit

import java.io.FileWriter
import java.nio.file.{Path, Paths}
import java.nio.{ByteBuffer, ByteOrder}

class Memory(capacity: Int, instructionFilename: String = "hello.asmbin") extends Module {
  val io = IO(new Bundle {
    val read_address = Input(UInt(32.W))
    val debug_read_address = Input(UInt(32.W))
    val char_read_address = Input(UInt(32.W))
    val instruction_read_address = Input(UInt(32.W))

    val write_address = Input(UInt(32.W))
    val write_enable = Input(Bool())
    val write_data = Input(UInt(32.W))

    val read_data = Output(UInt(32.W))
    val debug_read_data = Output(UInt(32.W))
    val char_read_data = Output(UInt(32.W))
    val instruction_read_data = Output(UInt(32.W))
  })

  annotate(new ChiselAnnotation {
    override def toFirrtl =
      MemorySynthInit
  })

  val instructions = readAsmBinary(instructionFilename)
  val mem = SyncReadMem(capacity, UInt(32.W))

  mem.write((io.write_address / 4.U) & Fill(32, io.write_enable), io.write_data)
  loadMemoryFromFileInline(mem, instructions.toString.replaceAll("\\\\", "/"))

  io.read_data := mem.read(io.read_address / 4.U, true.B)
  io.debug_read_data := mem.read(io.debug_read_address / 4.U, true.B)
  io.char_read_data := mem.read(io.char_read_address / 4.U, true.B)
  io.instruction_read_data := mem.read(io.instruction_read_address / 4.U, true.B)

  def readAsmBinary(filename: String) = {
    val inputStream = getClass.getClassLoader.getResourceAsStream(filename)
    var instructions = new Array[BigInt](0)
    val arr = new Array[Byte](4)
    while (inputStream.read(arr) == 4) {
      val instBuf = ByteBuffer.wrap(arr)
      instBuf.order(ByteOrder.LITTLE_ENDIAN)
      val inst = BigInt(instBuf.getInt() & 0xFFFFFFFFL)
      instructions = instructions :+ inst
    }
    instructions = instructions :+ BigInt(0x00000013L)
    instructions = instructions :+ BigInt(0x00000013L)
    instructions = instructions :+ BigInt(0x00000013L)
    val entry = ProgramCounter.EntryAddress.litValue() / 4
    val currentDir = System.getProperty("user.dir")
    val exeTxtPath = Paths.get(currentDir.toString, "verilog", f"${instructionFilename}.txt")
    val writer = new FileWriter(exeTxtPath.toString)
    for (i <- entry until instructions.length + entry) {
      writer.write(f"@$i%x\n${instructions((i - entry).toInt)}%08x\n")
    }
    writer.close()
    exeTxtPath
  }
}
