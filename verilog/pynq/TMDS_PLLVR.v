
// file: TMDS_PLLVR.v
// 
// (c) Copyright 2008 - 2013 Xilinx, Inc. All rights reserved.
// 
// This file contains confidential and proprietary information
// of Xilinx, Inc. and is protected under U.S. and
// international copyright and other intellectual property
// laws.
// 
// DISCLAIMER
// This disclaimer is not a license and does not grant any
// rights to the materials distributed herewith. Except as
// otherwise provided in a valid license issued to you by
// Xilinx, and to the maximum extent permitted by applicable
// law: (1) THESE MATERIALS ARE MADE AVAILABLE "AS IS" AND
// WITH ALL FAULTS, AND XILINX HEREBY DISCLAIMS ALL WARRANTIES
// AND CONDITIONS, EXPRESS, IMPLIED, OR STATUTORY, INCLUDING
// BUT NOT LIMITED TO WARRANTIES OF MERCHANTABILITY, NON-
// INFRINGEMENT, OR FITNESS FOR ANY PARTICULAR PURPOSE; and
// (2) Xilinx shall not be liable (whether in contract or tort,
// including negligence, or under any other theory of
// liability) for any loss or damage of any kind or nature
// related to, arising under or in connection with these
// materials, including for any direct, or any indirect,
// special, incidental, or consequential loss or damage
// (including loss of data, profits, goodwill, or any type of
// loss or damage suffered as a result of any action brought
// by a third party) even if such damage or loss was
// reasonably foreseeable or Xilinx had been advised of the
// possibility of the same.
// 
// CRITICAL APPLICATIONS
// Xilinx products are not designed or intended to be fail-
// safe, or for use in any application requiring fail-safe
// performance, such as life-support or safety devices or
// systems, Class III medical devices, nuclear facilities,
// applications related to the deployment of airbags, or any
// other applications that could lead to death, personal
// injury, or severe property or environmental damage
// (individually and collectively, "Critical
// Applications"). Customer assumes the sole risk and
// liability of any use of Xilinx products in Critical
// Applications, subject only to applicable laws and
// regulations governing limitations on product liability.
// 
// THIS COPYRIGHT NOTICE AND DISCLAIMER MUST BE RETAINED AS
// PART OF THIS FILE AT ALL TIMES.
// 
//----------------------------------------------------------------------------
// User entered comments
//----------------------------------------------------------------------------
// None
//
//----------------------------------------------------------------------------
//  Output     Output      Phase    Duty Cycle   Pk-to-Pk     Phase
//   Clock     Freq (MHz)  (degrees)    (%)     Jitter (ps)  Error (ps)
//----------------------------------------------------------------------------
// __clkout__250.00000______0.000______50.0______200.536____237.727
// _clkoutd__100.00000______0.000______50.0______226.965____237.727
//
//----------------------------------------------------------------------------
// Input Clock   Freq (MHz)    Input Jitter (UI)
//----------------------------------------------------------------------------
// __primary__________25.000____________0.010

`timescale 1ps/1ps

(* CORE_GENERATION_INFO = "TMDS_PLLVR,clk_wiz_v6_0_5_0_0,{component_name=TMDS_PLLVR,use_phase_alignment=true,use_min_o_jitter=false,use_max_i_jitter=false,use_dyn_phase_shift=false,use_inclk_switchover=false,use_dyn_reconfig=false,enable_axi=0,feedback_source=FDBK_AUTO,PRIMITIVE=PLL,num_out_clk=2,clkin1_period=40.000,clkin2_period=10.0,use_power_down=false,use_reset=true,use_locked=true,use_inclk_stopped=false,feedback_type=SINGLE,CLOCK_MGR_TYPE=NA,manual_override=true}" *)

module TMDS_PLLVR
 (
  // Clock out ports
  output        clkout,
  output        clkoutd,
  // Status and control signals
  input         reset,
  output        lock,
 // Clock in ports
  input         clkin
 );

  TMDS_PLLVR_clk_wiz inst
  (
  // Clock out ports  
  .clkout(clkout),
  .clkoutd(clkoutd),
  // Status and control signals               
  .reset(reset), 
  .lock(lock),
 // Clock in ports
  .clk_in1(clkin)
  );

endmodule


module TMDS_PLLVR_clk_wiz

 (// Clock in ports
  // Clock out ports
  output        clkout,
  output        clkoutd,
  // Status and control signals
  input         reset,
  output        lock,
  input         clk_in1
 );
  // Input buffering
  //------------------------------------
wire clk_in1_TMDS_PLLVR;
wire clk_in2_TMDS_PLLVR;
  IBUF clkin1_ibufg
   (.O (clk_in1_TMDS_PLLVR),
    .I (clk_in1));




  // Clocking PRIMITIVE
  //------------------------------------

  // Instantiation of the MMCM PRIMITIVE
  //    * Unused inputs are tied off
  //    * Unused outputs are labeled unused

  wire        clkout_TMDS_PLLVR;
  wire        clkoutd_TMDS_PLLVR;
  wire        clk_out3_TMDS_PLLVR;
  wire        clk_out4_TMDS_PLLVR;
  wire        clk_out5_TMDS_PLLVR;
  wire        clk_out6_TMDS_PLLVR;
  wire        clk_out7_TMDS_PLLVR;

  wire [15:0] do_unused;
  wire        drdy_unused;
  wire        psdone_unused;
  wire        lock_int;
  wire        clkfbout_TMDS_PLLVR;
  wire        clkfbout_buf_TMDS_PLLVR;
  wire        clkfboutb_unused;
   wire clkout2_unused;
   wire clkout3_unused;
   wire clkout4_unused;
  wire        clkout5_unused;
  wire        clkout6_unused;
  wire        clkfbstopped_unused;
  wire        clkinstopped_unused;
  wire        reset_high;

  PLLE2_ADV
  #(.BANDWIDTH            ("OPTIMIZED"),
    .COMPENSATION         ("ZHOLD"),
    .STARTUP_WAIT         ("FALSE"),
    .DIVCLK_DIVIDE        (1),
    .CLKFBOUT_MULT        (40),
    .CLKFBOUT_PHASE       (0.000),
    .CLKOUT0_DIVIDE       (4),
    .CLKOUT0_PHASE        (0.000),
    .CLKOUT0_DUTY_CYCLE   (0.500),
    .CLKOUT1_DIVIDE       (10),
    .CLKOUT1_PHASE        (0.000),
    .CLKOUT1_DUTY_CYCLE   (0.500),
    .CLKIN1_PERIOD        (40.000))
  plle2_adv_inst
    // Output clocks
   (
    .CLKFBOUT            (clkfbout_TMDS_PLLVR),
    .CLKOUT0             (clkout_TMDS_PLLVR),
    .CLKOUT1             (clkoutd_TMDS_PLLVR),
    .CLKOUT2             (clkout2_unused),
    .CLKOUT3             (clkout3_unused),
    .CLKOUT4             (clkout4_unused),
    .CLKOUT5             (clkout5_unused),
     // Input clock control
    .CLKFBIN             (clkfbout_buf_TMDS_PLLVR),
    .CLKIN1              (clk_in1_TMDS_PLLVR),
    .CLKIN2              (1'b0),
     // Tied to always select the primary input clock
    .CLKINSEL            (1'b1),
    // Ports for dynamic reconfiguration
    .DADDR               (7'h0),
    .DCLK                (1'b0),
    .DEN                 (1'b0),
    .DI                  (16'h0),
    .DO                  (do_unused),
    .DRDY                (drdy_unused),
    .DWE                 (1'b0),
    // Other control and status signals
    .LOCKED              (lock_int),
    .PWRDWN              (1'b0),
    .RST                 (reset_high));
  assign reset_high = reset; 

  assign lock = lock_int;
// Clock Monitor clock assigning
//--------------------------------------
 // Output buffering
  //-----------------------------------

  BUFG clkf_buf
   (.O (clkfbout_buf_TMDS_PLLVR),
    .I (clkfbout_TMDS_PLLVR));






  BUFG clkout1_buf
   (.O   (clkout),
    .I   (clkout_TMDS_PLLVR));


  BUFG clkout2_buf
   (.O   (clkoutd),
    .I   (clkoutd_TMDS_PLLVR));



endmodule
