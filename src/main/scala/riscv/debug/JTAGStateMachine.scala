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

package riscv.debug

import chisel3._
import chisel3.util._
import chisel3.experimental.ChiselEnum

object JTAGStates extends ChiselEnum {
  val TestLogicReset,
  RunTestIdle,
  SelectDRScan,
  CaptureDR,
  ShiftDR,
  Exit1DR,
  PauseDR,
  Exit2DR,
  UpdateDR,
  SelectIRScan,
  CaptureIR,
  ShiftIR,
  Exit1IR,
  PauseIR,
  Exit2IR,
  UpdateIR = Value
}

class JTAGStateMachine extends Module {
  val io = IO(new Bundle {
    val TMS = Input(Bool())
  })
  val state = RegInit(UInt(), JTAGStates.TestLogicReset)
  switch(state) {
    is(JTAGStates.TestLogicReset) {
      state := Mux(io.TMS, JTAGStates.TestLogicReset, JTAGStates.RunTestIdle)
    }
    is(JTAGStates.RunTestIdle) {
      state := Mux(io.TMS, JTAGStates.SelectDRScan, JTAGStates.RunTestIdle)
    }
    is(JTAGStates.SelectDRScan) {
      state := Mux(io.TMS, JTAGStates.SelectIRScan, JTAGStates.CaptureDR)
    }
    is(JTAGStates.CaptureDR) {
      state := Mux(io.TMS, JTAGStates.Exit1DR, JTAGStates.ShiftDR)
    }
    is(JTAGStates.ShiftDR) {
      state := Mux(io.TMS, JTAGStates.Exit1DR, JTAGStates.ShiftDR)
    }
    is(JTAGStates.Exit1DR) {
      state := Mux(io.TMS, JTAGStates.UpdateDR, JTAGStates.PauseDR)
    }
    is(JTAGStates.PauseDR) {
      state := Mux(io.TMS, JTAGStates.Exit2DR, JTAGStates.PauseDR)
    }
    is(JTAGStates.Exit2DR) {
      state := Mux(io.TMS, JTAGStates.UpdateDR, JTAGStates.ShiftDR)
    }
    is(JTAGStates.UpdateDR) {
      state := Mux(io.TMS, JTAGStates.SelectDRScan, JTAGStates.RunTestIdle)
    }
    is(JTAGStates.SelectIRScan) {
      state := Mux(io.TMS, JTAGStates.TestLogicReset, JTAGStates.CaptureIR)
    }
    is(JTAGStates.CaptureIR) {
      state := Mux(io.TMS, JTAGStates.Exit1IR, JTAGStates.ShiftIR)
    }
    is(JTAGStates.ShiftIR) {
      state := Mux(io.TMS, JTAGStates.Exit1IR, JTAGStates.ShiftIR)
    }
    is(JTAGStates.Exit1IR) {
      state := Mux(io.TMS, JTAGStates.UpdateIR, JTAGStates.PauseIR)
    }
    is(JTAGStates.PauseIR) {
      state := Mux(io.TMS, JTAGStates.Exit2IR, JTAGStates.PauseIR)
    }
    is(JTAGStates.Exit2IR) {
      state := Mux(io.TMS, JTAGStates.UpdateIR, JTAGStates.ShiftIR)
    }
    is(JTAGStates.UpdateIR) {
      state := Mux(io.TMS, JTAGStates.SelectDRScan, JTAGStates.RunTestIdle)
    }
  }
}
