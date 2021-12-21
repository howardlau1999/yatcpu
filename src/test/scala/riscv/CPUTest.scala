package riscv

import chisel3._
import chisel3.tester._
import org.scalatest._

import java.nio.{ByteBuffer, ByteOrder}

class CPUTest extends FreeSpec with ChiselScalatestTester {
  class TestMemory(capacity: Int = 8192, asmBin: String) extends Module {
    val io = IO(new Bundle {
      val read_address = Input(UInt(32.W))
      val debug_read_address = Input(UInt(32.W))
      val instruction_read_address = Input(UInt(32.W))

      val write_address = Input(UInt(32.W))
      val write_enable = Input(Bool())
      val write_data = Input(UInt(32.W))

      val read_data = Output(UInt(32.W))
      val debug_read_data = Output(UInt(32.W))
      val instruction_read_data = Output(UInt(32.W))
    })
    val mem = RegInit(loadAsmBinary(asmBin))
    val read_data = Reg(UInt(32.W))
    val inst_data = Reg(UInt(32.W))
    val dbg_data = Reg(UInt(32.W))
    read_data := mem(io.read_address / 4.U)
    inst_data := mem((io.instruction_read_address - 0x1000.U + 1024.U) / 4.U)
    dbg_data := mem(io.debug_read_address / 4.U)
    io.read_data := read_data
    io.instruction_read_data := inst_data
    io.debug_read_data := dbg_data

    when(io.write_enable) {
      mem(io.write_address / 4.U) := io.write_data
    }

    def loadAsmBinary(filename: String) = {
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
      VecInit((Seq.fill(256)(BigInt(0)) ++ instructions).map(inst => inst.U(32.W)))
    }
  }

  class TestTopModule(exeFilename: String) extends Module {
    val io = IO(new Bundle {
      val mem_debug_read_address = Input(UInt(32.W))
      val regs_debug_read_address = Input(UInt(5.W))
      val regs_debug_read_data = Output(UInt(32.W))
      val mem_debug_read_data = Output(UInt(32.W))

      val a = Output(UInt(32.W))
      val interrupt = Input(UInt(32.W))
    })
    val mem = Module(new TestMemory(8192, exeFilename))
    val cpu = Module(new CPU)
    io.a := cpu.io.instruction_read_address
    mem.io.debug_read_address := io.mem_debug_read_address
    mem.io.instruction_read_address := cpu.io.instruction_read_address
    cpu.io.instruction_read_data := mem.io.instruction_read_data
    cpu.io.debug_read_address := io.regs_debug_read_address
    mem.io.read_address := cpu.io.mem_read_address
    cpu.io.mem_read_data := mem.io.read_data
    mem.io.write_enable := cpu.io.mem_write_enable
    mem.io.write_address := cpu.io.mem_write_address
    mem.io.write_data := cpu.io.mem_write_data
    io.regs_debug_read_data := cpu.io.debug_read_data
    io.mem_debug_read_data := mem.io.debug_read_data

    cpu.io.interrupt_flag := io.interrupt
  }

  "CPU " - {
    "should calculate recursive fibonacci" in {
      test(new TestTopModule("fibonacci.asmbin")) { c =>
        c.io.interrupt.poke(0.U)
        for (i <- 1 to 5000) {
          c.clock.step()
          c.io.mem_debug_read_address.poke((i * 4).U) // Avoid timeout
        }
        c.io.mem_debug_read_address.poke(4.U)
        c.clock.step()
        c.io.mem_debug_read_data.expect(55.U)
      }
    }

    "should quicksort 10 numbers" in {
      test(new TestTopModule("quicksort.asmbin")) { c =>
        c.io.interrupt.poke(0.U)
        for (i <- 1 to 3000) {
          c.clock.step()
          c.io.mem_debug_read_address.poke((i * 4).U) // Avoid timeout
        }
        for (i <- 1 to 10) {
          c.io.mem_debug_read_address.poke((4 * i).U)
          c.clock.step()
          c.io.mem_debug_read_data.expect((i - 1).U)
        }
      }
    }
  }
}
