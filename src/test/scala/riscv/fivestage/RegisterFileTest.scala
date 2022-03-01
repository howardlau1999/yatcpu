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

package riscv.fivestage

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import riscv.TestAnnotations
import riscv.core.fivestage.RegisterFile

class RegisterFileTest extends AnyFlatSpec with ChiselScalatestTester {
  behavior of "Register File of Five-stage CPU"
  it should "read the written content" in {
    test(new RegisterFile).withAnnotations(TestAnnotations.annos) { c =>
      timescope {
        c.io.write_enable.poke(true.B)
        c.io.write_address.poke(1.U)
        c.io.write_data.poke(0xDEADBEEFL.U)
        c.clock.step()
      }
      c.io.read_address1.poke(1.U)
      c.io.read_data1.expect(0xDEADBEEFL.U)
    }
  }

  it should "x0 always be zero" in {
    test(new RegisterFile).withAnnotations(TestAnnotations.annos) { c =>
      timescope {
        c.io.write_enable.poke(true.B)
        c.io.write_address.poke(0.U)
        c.io.write_data.poke(0xDEADBEEFL.U)
        c.clock.step()
      }
      c.io.read_address1.poke(0.U)
      c.io.read_data1.expect(0.U)
    }
  }

  it should "read the writing content" in {
    test(new RegisterFile).withAnnotations(TestAnnotations.annos) { c =>
      timescope {
        c.io.read_address1.poke(2.U)
        c.io.read_data1.expect(0.U)
        c.io.write_enable.poke(true.B)
        c.io.write_address.poke(2.U)
        c.io.write_data.poke(0xDEADBEEFL.U)
        c.io.read_address1.poke(2.U)
        c.io.read_data1.expect(0xDEADBEEFL.U)
        c.clock.step()
      }
      c.io.read_address1.poke(2.U)
      c.io.read_data1.expect(0xDEADBEEFL.U)
    }
  }

}
