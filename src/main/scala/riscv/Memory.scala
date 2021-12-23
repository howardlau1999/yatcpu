// Copyright 2021 Howard Lau
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

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
    val read_address = Input(UInt(Parameters.AddrWidth))
    val debug_read_address = Input(UInt(Parameters.AddrWidth))
    val char_read_address = Input(UInt(Parameters.AddrWidth))
    val instruction_read_address = Input(UInt(Parameters.AddrWidth))

    val write_address = Input(UInt(Parameters.AddrWidth))
    val write_enable = Input(Bool())
    val write_data = Input(UInt(Parameters.DataWidth))

    val read_data = Output(UInt(Parameters.DataWidth))
    val debug_read_data = Output(UInt(Parameters.DataWidth))
    val char_read_data = Output(UInt(Parameters.DataWidth))
    val instruction_read_data = Output(UInt(Parameters.DataWidth))
  })

  annotate(new ChiselAnnotation {
    override def toFirrtl =
      MemorySynthInit
  })

  val instructions = readAsmBinary(instructionFilename)
  val mem = SyncReadMem(capacity, UInt(Parameters.DataWidth))

  when(io.write_enable) {
    mem.write(io.write_address / 4.U, io.write_data)
  }
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
