package riscv

import chisel3._
import chisel3.tester._
import org.scalatest._

import java.nio.{ByteBuffer, ByteOrder}

class CPUTest extends FreeSpec with ChiselScalatestTester {
  var warned = false

  def read_instructions(filename: String) = {
    val inputStream = getClass.getClassLoader.getResourceAsStream(filename)
    var instructions = new Array[UInt](0)
    val arr = new Array[Byte](4)
    while (inputStream.read(arr) == 4) {
      val instBuf = ByteBuffer.wrap(arr)
      instBuf.order(ByteOrder.LITTLE_ENDIAN)
      val inst = BigInt(instBuf.getInt() & 0xFFFFFFFFL)
      instructions = instructions :+ inst.U(32.W)
    }
    instructions
  }

  def run_instructions(instructions: Array[UInt], c: CPU, cycles: Int) = {
    warned = false
    for (i <- 1 to cycles) {
      if (i % 1000 == 0) println(f"$i%d cycles...")
      val pc = c.io.instruction_address.peek().litValue() >> 2
      if (pc >= instructions.length) {
        if (!warned) {
          println(f"PC is out of range at cycle $i%d PC=0x${pc * 4}%x. This means the IF is trying to fetch code " +
            f"after the program has ended, which is normal.")
          warned = true
        }
        c.io.instruction.poke(0x00000013.U)
      } else {
        c.io.instruction.poke(instructions(pc.toInt))
      }
      c.clock.step()
    }
  }

  "CPU " - {
    "should execute lui and addi" in {
      test(new CPU) { c =>
        // li ra, 1
        c.io.instruction.poke(0x00100093L.U)
        // li ra, 1 now in IF->ID, decode unit reacts
        c.clock.step()
        c.io.debug_read_address.poke(0x1L.U)
        c.io.debug_read_data.expect(0x0L.U)
        // addi t5, ra, 1
        c.io.instruction.poke(0x00108f13L.U)
        // li ra, 1 now in ID->EX, execute unit reacts
        // addi t5, ra, 1 now in IF->ID, decode unit reacts
        c.clock.step()
        c.io.debug_read_data.expect(0x1L.U)
        // nop
        c.io.instruction.poke(0x00000013L.U)
        // addi t5, ra, 1 now in ID->EX, execute unit reacts
        c.clock.step()
        // nop
        c.clock.step()
        c.io.debug_read_address.poke(30.U)
        c.io.debug_read_data.expect(2.U)
        c.clock.step()
        c.io.debug_read_data.expect(2.U)
      }
    }

    "should execute sb and lb" in {
      test(new CPU) { c =>
        /*
          li t0, 15
          sb t0, 4(x0)
          lb t1, 4(x0)
         */
        val instructions: Array[UInt] = Array(
          0x00f00293L.U,
          0x00500223L.U,
          0x00400303L.U,
        )
        run_instructions(instructions, c, 7)
        c.io.debug_read_address.poke(6.U)
        c.io.debug_read_data.expect(15.U)
      }
    }

    "should calculate 1+2+...+100=5050" in {
      test(new CPU) { c =>
        val instructions: Array[UInt] = Array(
          /*
            li t0, 0 # int sum = 0;
            li t1, 1 # int i = 1;
            li t2, 100 # int to = 100;
            j cond
            add:
            add t0, t0, t1 # sum += i;
            addi t1, t1, 1 # i += 1;
            cond:
            bge t2, t1, add # while (100 >= i) goto add;
            end:
            j end
           */
          0x00000293L.U,
          0x00100313L.U,
          0x06400393L.U,
          0x00c0006fL.U,
          0x006282b3L.U,
          0x00130313L.U,
          0xfe63dce3L.U,
          0x0000006fL.U,
        )
        run_instructions(instructions, c, 1000)
        c.io.debug_read_address.poke(5.U)
        c.io.debug_read_data.expect(5050.U)
      }
    }

    "should execute recursive fibonacci" in {
      test(new CPU) { c =>
        /*
          int fib(int a) {
                  if (a == 1 || a == 2) return 1;
                  return fib(a - 1) + fib(a - 2);
          }

          int main() {
                  *(int *)(4) = fib(10);
          }
         */
        run_instructions(read_instructions("fibonacci.asmbin"), c, 4000)
        c.io.debug_mem_read_address.poke(4.U)
        c.clock.step()
        c.io.debug_mem_read_data.expect(55.U)
      }
    }

    "should quick sort 10 numbers" in {
      /*
        void quicksort(int *arr, int l, int r) {
                if (l >= r) return;
                int pivot = arr[l];
                int i = l, j = r;
                while (i < j) {
                        while(arr[j] >= pivot && i < j) --j;
                        arr[i] = arr[j];
                        while(arr[i] < pivot && i < j) ++i;
                        arr[j] = arr[i];
                }
                arr[i] = pivot;
                quicksort(arr, l, i - 1);
                quicksort(arr, i + 1, r);
        }

        int main() {
                int nums[10];
                nums[0] = 6;
                nums[1] = 2;
                nums[2] = 4;
                nums[3] = 5;
                nums[4] = 3;
                nums[5] = 1;
                nums[6] = 0;
                nums[7] = 9;
                nums[8] = 7;
                nums[9] = 8;


                quicksort(nums, 0, 9);

                for (int i = 1; i <= 10; ++i) {
                        *(int *)(i * 4) = nums[i - 1];
                }
        }
       */
      test(new CPU) { c =>
        run_instructions(read_instructions("quicksort.asmbin"), c, 3000)
        for (i <- 1 to 10) {
          c.io.debug_mem_read_address.poke((4 * i).U)
          c.clock.step()
          c.io.debug_mem_read_data.expect((i - 1).U)
        }
      }
    }

    "should write hello world to video memory" in {
      test(new CPU) { c =>
        run_instructions(read_instructions("hello.asmbin"), c, 30000)
        val helloworld = "Hello, world!"
        for (r <- 0 until 30) {
          for (i <- 0 until 3) {
            c.io.char_mem_read_address.poke((i * 4 + 1024 + 80 * r).U)
            c.clock.step()
            val arr: Array[Byte] = Array(
              helloworld.charAt(i * 4).toByte,
              helloworld.charAt(i * 4 + 1).toByte,
              helloworld.charAt(i * 4 + 2).toByte,
              helloworld.charAt(i * 4 + 3).toByte,
            )
            val buf = ByteBuffer.wrap(arr)
            buf.order(ByteOrder.LITTLE_ENDIAN)
            c.io.char_mem_read_data.expect(BigInt(buf.getInt() & 0xFFFFFFFF).U)
          }
        }

        c.io.debug_mem_read_address.poke(0x4.U)
        c.clock.step()
        c.io.debug_mem_read_data.expect(0xDEADBEEFL.U)
      }
    }
  }
}
