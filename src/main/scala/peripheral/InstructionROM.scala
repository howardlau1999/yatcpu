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

package peripheral

import chisel3._
import chisel3.experimental.{ChiselAnnotation, annotate}
import chisel3.util.experimental.loadMemoryFromFileInline
import firrtl.annotations.MemorySynthInit
import riscv.Parameters

import java.io.FileWriter
import java.nio.file.{Files, Paths}
import java.nio.{ByteBuffer, ByteOrder}

class InstructionROM(instructionFilename: String) extends Module {
  val io = IO(new Bundle {
    val address = Input(UInt(Parameters.AddrWidth))
    val data = Output(UInt(Parameters.InstructionWidth))
  })

  val (instructionsInitFile, capacity) = readAsmBinary(instructionFilename)
  val mem = SyncReadMem(capacity, UInt(Parameters.InstructionWidth))
  annotate(new ChiselAnnotation {
    override def toFirrtl =
      MemorySynthInit
  })
  loadMemoryFromFileInline(mem, instructionsInitFile.toString.replaceAll("\\\\", "/"))
  io.data := mem.read(io.address, true.B)

  def readAsmBinary(filename: String) = {
    val inputStream = if (Files.exists(Paths.get(filename))) {
      Files.newInputStream(Paths.get(filename))
    } else {
      getClass.getClassLoader.getResourceAsStream(filename)
    }
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
    val currentDir = System.getProperty("user.dir")
    val exeTxtPath = Paths.get(currentDir, "verilog", f"${instructionFilename}.txt")
    val writer = new FileWriter(exeTxtPath.toString)
    for (i <- instructions.indices) {
      writer.write(f"@$i%x\n${instructions(i)}%08x\n")
    }
    writer.close()
    (exeTxtPath, instructions.length)
  }
}
