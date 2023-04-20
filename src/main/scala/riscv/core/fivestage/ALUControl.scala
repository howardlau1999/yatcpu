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

package riscv.core.fivestage

import chisel3._
import chisel3.util._

class ALUControl extends Module {
  val io = IO(new Bundle {
    val opcode = Input(UInt(7.W))
    val funct3 = Input(UInt(3.W))
    val funct7 = Input(UInt(7.W))

    val alu_funct = Output(ALUFunctions())
  })

  io.alu_funct := ALUFunctions.zero

  switch(io.opcode) {
    is(InstructionTypes.I) {
      io.alu_funct := MuxLookup(io.funct3, ALUFunctions.zero)(
        IndexedSeq(
          InstructionsTypeI.addi -> ALUFunctions.add,
          InstructionsTypeI.slli -> ALUFunctions.sll,
          InstructionsTypeI.slti -> ALUFunctions.slt,
          InstructionsTypeI.sltiu -> ALUFunctions.sltu,
          InstructionsTypeI.xori -> ALUFunctions.xor,
          InstructionsTypeI.ori -> ALUFunctions.or,
          InstructionsTypeI.andi -> ALUFunctions.and,
          InstructionsTypeI.sri -> Mux(io.funct7(5), ALUFunctions.sra, ALUFunctions.srl)
        ),
      )
    }
    is(InstructionTypes.RM) {
      io.alu_funct := MuxLookup(io.funct3, ALUFunctions.zero)(
        IndexedSeq(
          InstructionsTypeR.add_sub -> Mux(io.funct7(5), ALUFunctions.sub, ALUFunctions.add),
          InstructionsTypeR.sll -> ALUFunctions.sll,
          InstructionsTypeR.slt -> ALUFunctions.slt,
          InstructionsTypeR.sltu -> ALUFunctions.sltu,
          InstructionsTypeR.xor -> ALUFunctions.xor,
          InstructionsTypeR.or -> ALUFunctions.or,
          InstructionsTypeR.and -> ALUFunctions.and,
          InstructionsTypeR.sr -> Mux(io.funct7(5), ALUFunctions.sra, ALUFunctions.srl)
        ),
      )
    }
    is(InstructionTypes.B) {
      io.alu_funct := ALUFunctions.add
    }
    is(InstructionTypes.L) {
      io.alu_funct := ALUFunctions.add
    }
    is(InstructionTypes.S) {
      io.alu_funct := ALUFunctions.add
    }
    is(Instructions.jal) {
      io.alu_funct := ALUFunctions.add
    }
    is(Instructions.jalr) {
      io.alu_funct := ALUFunctions.add
    }
    is(Instructions.lui) {
      io.alu_funct := ALUFunctions.add
    }
    is(Instructions.auipc) {
      io.alu_funct := ALUFunctions.add
    }
  }
}
