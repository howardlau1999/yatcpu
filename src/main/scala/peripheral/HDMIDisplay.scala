// Copyright 2022 hrpccs
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
import chisel3.util._

class HDMISync extends Module {
  val io = IO(new Bundle {
    val hsync = Output(Bool())
    val vsync = Output(Bool())
    val video_on = Output(Bool())
    val p_tick = Output(Bool())
    val f_tick = Output(Bool())
    val x = Output(UInt(10.W))
    val y = Output(UInt(10.W))
    val x_next = Output(UInt(10.W))
    val y_next = Output(UInt(10.W))
  })

  val DisplayHorizontal = ScreenInfo.DisplayHorizontal
  val DisplayVertical = ScreenInfo.DisplayVertical

  val BorderLeft = 48
  val BorderRight = 16
  val BorderTop = 10
  val BorderBottom = 33

  val RetraceHorizontal = 96
  val RetraceVertical = 2

  val MaxHorizontal = DisplayHorizontal + BorderLeft + BorderRight + RetraceHorizontal - 1
  val MaxVertical = DisplayVertical + BorderTop + BorderBottom + RetraceVertical - 1

  val RetraceHorizontalStart = DisplayHorizontal + BorderRight
  val RetraceHorizontalEnd = RetraceHorizontalStart + RetraceHorizontal - 1

  val RetraceVerticalStart = DisplayVertical + BorderBottom
  val RetraceVerticalEnd = RetraceVerticalStart + RetraceVertical - 1

  val pixel = RegInit(UInt(3.W), 0.U)
  val pixel_next = Wire(UInt(3.W))
  val pixel_tick = Wire(Bool())

  val v_count_reg = RegInit(UInt(10.W), 0.U)
  val h_count_reg = RegInit(UInt(10.W), 0.U)

  val v_count_next = Wire(UInt(10.W))
  val h_count_next = Wire(UInt(10.W))

  val vsync_reg = RegInit(Bool(), false.B)
  val hsync_reg = RegInit(Bool(), false.B)

  val vsync_next = Wire(Bool())
  val hsync_next = Wire(Bool())

  pixel_next := Mux(pixel === 4.U, 0.U, pixel + 1.U)
  pixel_tick := pixel === 0.U

  h_count_next := Mux(
    pixel_tick,
    Mux(h_count_reg === MaxHorizontal.U, 0.U, h_count_reg + 1.U),
    h_count_reg
  )

  v_count_next := Mux(
    pixel_tick && h_count_reg === MaxHorizontal.U,
    Mux(v_count_reg === MaxVertical.U, 0.U, v_count_reg + 1.U),
    v_count_reg
  )

  hsync_next := h_count_reg >= RetraceHorizontalStart.U && h_count_reg <= RetraceHorizontalEnd.U
  vsync_next := v_count_reg >= RetraceVerticalStart.U && v_count_reg <= RetraceVerticalEnd.U

  pixel := pixel_next
  hsync_reg := hsync_next
  vsync_reg := vsync_next
  v_count_reg := v_count_next
  h_count_reg := h_count_next

  io.video_on := h_count_reg < DisplayHorizontal.U && v_count_reg < DisplayVertical.U
  io.hsync := hsync_reg
  io.vsync := vsync_reg
  io.x := h_count_reg
  io.y := v_count_reg
  io.x_next := h_count_next
  io.y_next := v_count_next
  io.p_tick := pixel_tick
  io.f_tick := io.x === 0.U && io.y === 0.U
}

class TMDS_encoder extends Module {
  val io = IO(new Bundle() {
    val video_data = Input(UInt(8.W)) //r,g,b,8bit
    val control_data = Input(UInt(2.W))
    val video_on = Input(Bool())
    val TMDS = Output(UInt(10.W))
  })
  val Nb1s = PopCount(io.video_data)
  val xored = xorfct(io.video_data)
  val xnored = xnorfct(io.video_data)
  val XNOR = (Nb1s > 4.U(4.W)) || (Nb1s === 4.U(4.W) && io.video_data(0) === 0.U)
  val q_m = RegInit(0.U(9.W))
  val diffSize = 4
  val diff = RegInit(0.S(diffSize.W))
  q_m := Mux(
    XNOR,
    xnored,
    xored
  )
  val disparitySize = 4
  val disparityReg = RegInit(0.S(disparitySize.W))
  diff := PopCount(q_m).asSInt - 4.S
  val doutReg = RegInit("b1010101011".U(10.W))

  def xorfct(value: UInt): UInt = {
    val vin = VecInit(value.asBools)
    val res = VecInit(511.U.asBools)
    res(0) := vin(0)
    for (i <- 1 to 7) {
      res(i) := res(i - 1) ^ vin(i)
    }
    res(8) := 1.U
    res.asUInt
  }

  def xnorfct(value: UInt): UInt = {
    val vin = VecInit(value.asBools)
    val res = VecInit(511.U.asBools)
    res(0) := vin(0)
    for (i <- 1 to 7) {
      res(i) := !(res(i - 1) ^ vin(i))
    }
    res(8) := 0.U
    res.asUInt
  }

  when(io.video_on === false.B) {
    disparityReg := 0.S
    doutReg := "b1010101011".U(10.W)
    switch(io.control_data) {
      is("b00".U(2.W)) {
        doutReg := "b1101010100".U(10.W)
      }
      is("b01".U(2.W)) {
        doutReg := "b0010101011".U(10.W)
      }
      is("b10".U(2.W)) {
        doutReg := "b0101010100".U(10.W)
      }
    }
  }.otherwise {
    when(disparityReg === 0.S || diff === 0.S) {
      when(q_m(8) === false.B) {
        doutReg := "b10".U(2.W) ## ~q_m(7, 0)
        disparityReg := disparityReg - diff
      }.otherwise {
        doutReg := "b01".U(2.W) ## q_m(7, 0)
        disparityReg := disparityReg + diff
      }
    }.elsewhen((!diff(diffSize - 1) && !disparityReg(disparitySize - 1))
      || (diff(diffSize - 1) && disparityReg(disparitySize - 1))) {
      doutReg := 1.U(1.W) ## q_m(8) ## ~q_m(7, 0)
      when(q_m(8)) {
        disparityReg := disparityReg + 1.S - diff
      }.otherwise {
        disparityReg := disparityReg - diff
      }
    }.otherwise {
      doutReg := 0.U(1.W) ## q_m
      when(q_m(8)) {
        disparityReg := disparityReg + diff
      }.otherwise {
        disparityReg := disparityReg - 1.S + diff
      }
    }
  }

  io.TMDS := doutReg
}

class HDMIDisplay extends Module {
  val io = IO(new Bundle() {
    val rgb = Input(UInt(24.W))
    val x = Output(UInt(16.W))
    val y = Output(UInt(16.W))
    val x_next = Output(UInt(16.W))
    val y_next = Output(UInt(16.W))
    val video_on = Output(Bool())

    val TMDSclk_p = Output(Bool())
    val TMDSdata_p = Output(UInt(3.W))
    val TMDSclk_n = Output(Bool())
    val TMDSdata_n = Output(UInt(3.W))
  })
  val rgb = io.rgb
  val pixel_clk = Wire(Bool())
  val hsync = Wire(Bool())
  val vsync = Wire(Bool())
  val sync = Module(new HDMISync)

  io.x := sync.io.x
  io.y := sync.io.y
  io.x_next := sync.io.x_next
  io.y_next := sync.io.y_next
  io.video_on := sync.io.video_on

  hsync := sync.io.hsync
  vsync := sync.io.vsync
  pixel_clk := sync.io.p_tick

  // TMDS_PLLVR is a vivado IP core, check it in /verilog/pynq/TMDS_PLLVR.v
  val serial_clk = Wire(Clock())
  val pll_lock = Wire(Bool())
  val tmdspll = Module(new TMDS_PLLVR)
  val rst = Wire(Reset())
  tmdspll.io.clkin := pixel_clk.asClock
  serial_clk := tmdspll.io.clkout
  pll_lock := tmdspll.io.lock
  tmdspll.io.reset := reset
  rst := ~pll_lock

  val tmds = Wire(UInt(3.W))
  val tmds_clk = Wire(Bool())
  withClockAndReset(pixel_clk.asClock, rst) {
    val tmds_channel1 = Wire(UInt(10.W))
    val tmds_channel2 = Wire(UInt(10.W))
    val tmds_channel0 = Wire(UInt(10.W))

    val tmds_green = Module(new TMDS_encoder)
    val tmds_red = Module(new TMDS_encoder)
    val tmds_blue = Module(new TMDS_encoder)

    tmds_red.io.video_on := sync.io.video_on
    tmds_blue.io.video_on := sync.io.video_on
    tmds_green.io.video_on := sync.io.video_on

    tmds_blue.io.control_data := sync.io.vsync ## sync.io.hsync
    tmds_green.io.control_data := 0.U
    tmds_red.io.control_data := 0.U

    tmds_red.io.video_data := rgb(23, 16)
    tmds_blue.io.video_data := rgb(7, 0)
    tmds_green.io.video_data := rgb(15, 8)

    tmds_channel0 := tmds_blue.io.TMDS
    tmds_channel1 := tmds_green.io.TMDS
    tmds_channel2 := tmds_red.io.TMDS

    val serdesBlue = Module(new Oser10Module())
    serdesBlue.io.data := tmds_channel0
    serdesBlue.io.fclk := serial_clk

    val serdesGreen = Module(new Oser10Module())
    serdesGreen.io.data := tmds_channel1
    serdesGreen.io.fclk := serial_clk

    val serdesRed = Module(new Oser10Module())
    serdesRed.io.data := tmds_channel2
    serdesRed.io.fclk := serial_clk

    tmds := serdesRed.io.q ## serdesGreen.io.q ## serdesBlue.io.q

    //serdesCLk : 25Mhz ,Why not directly use p_tick?
    //cause Duty Ratio of p_tick is 10% , while which of serdesCLk is 50%
    val serdesClk = Module(new Oser10Module())
    serdesClk.io.data := "b1111100000".U(10.W)
    serdesClk.io.fclk := serial_clk

    tmds_clk := serdesClk.io.q

    val buffDiffBlue = Module(new OBUFDS)
    buffDiffBlue.io.I := tmds(0)
    val buffDiffGreen = Module(new OBUFDS)
    buffDiffGreen.io.I := tmds(1)
    val buffDiffRed = Module(new OBUFDS)
    buffDiffRed.io.I := tmds(2)
    val buffDiffClk = Module(new OBUFDS)
    buffDiffClk.io.I := tmds_clk

    io.TMDSclk_p := buffDiffClk.io.O
    io.TMDSclk_n := buffDiffClk.io.OB
    io.TMDSdata_p := buffDiffRed.io.O ## buffDiffGreen.io.O ## buffDiffBlue.io.O
    io.TMDSdata_n := buffDiffRed.io.OB ## buffDiffGreen.io.OB ## buffDiffBlue.io.OB
  }
}

//----------------------------------------
//PLL frequency multiplier using BlackBox
class TMDS_PLLVR extends BlackBox {
  val io = IO(new Bundle {
    val clkin = Input(Clock())
    val reset = Input(Reset())
    val clkout = Output(Clock())
    val clkoutd = Output(Clock())
    val lock = Output(Bool())
  })
}

/* OSER10 : serializer 10:1*/
class OSER10 extends Module {
  val io = IO(new Bundle {
    val Q = Output(Bool()) // OSER10 data output signal
    val D0 = Input(Bool())
    val D1 = Input(Bool())
    val D2 = Input(Bool())
    val D3 = Input(Bool())
    val D4 = Input(Bool())
    val D5 = Input(Bool())
    val D6 = Input(Bool())
    val D7 = Input(Bool())
    val D8 = Input(Bool())
    val D9 = Input(Bool()) //  OSER10 data input signal
    val PCLK = Input(Clock()) // Primary clock input signal
    val FCLK = Input(Clock()) // High speed clock input signal
    val RESET = Input(Reset()) // Asynchronous reset input signal,
    //active-high.
  })
  withClockAndReset(io.FCLK, io.RESET) {
    val count = RegInit(0.U(4.W))
    val countnext = Wire(UInt(4.W))
    io.Q := MuxLookup(count, 0.U)(
      IndexedSeq(
        0.U -> io.D0.asBool,
        1.U -> io.D1.asBool,
        2.U -> io.D2.asBool,
        3.U -> io.D3.asBool,
        4.U -> io.D4.asBool,
        5.U -> io.D5.asBool,
        6.U -> io.D6.asBool,
        7.U -> io.D7.asBool,
        8.U -> io.D8.asBool,
        9.U -> io.D9.asBool
      )
    )
    countnext := Mux(
      count === 9.U, 0.U, count + 1.U
    )
    count := countnext
  }
}

class Oser10Module extends Module {
  val io = IO(new Bundle {
    val q = Output(Bool())
    val data = Input(UInt(10.W))
    val fclk = Input(Clock()) // Fast clock
  })

  val osr10 = Module(new OSER10())
  io.q := osr10.io.Q
  osr10.io.D0 := io.data(0)
  osr10.io.D1 := io.data(1)
  osr10.io.D2 := io.data(2)
  osr10.io.D3 := io.data(3)
  osr10.io.D4 := io.data(4)
  osr10.io.D5 := io.data(5)
  osr10.io.D6 := io.data(6)
  osr10.io.D7 := io.data(7)
  osr10.io.D8 := io.data(8)
  osr10.io.D9 := io.data(9)
  osr10.io.PCLK := clock
  osr10.io.FCLK := io.fclk
  osr10.io.RESET := reset
}

/* lvds output */
class OBUFDS extends BlackBox {
  val io = IO(new Bundle {
    val O = Output(Bool())
    val OB = Output(Bool())
    val I = Input(Bool())
  })
}
//-----------------------------------------


