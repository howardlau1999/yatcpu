# Copyright 2021 Howard Lau
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

## Clock signal
set_property PACKAGE_PIN W5 [get_ports clock]
    set_property IOSTANDARD LVCMOS33 [get_ports clock]
    create_clock -add -name sys_clk_pin -period 20.00 -waveform {0 5} [get_ports clock]

## Switches
set_property PACKAGE_PIN V17 [get_ports {io_switch[0]}]
    set_property IOSTANDARD LVCMOS33 [get_ports {io_switch[0]}]
set_property PACKAGE_PIN V16 [get_ports {io_switch[1]}]
    set_property IOSTANDARD LVCMOS33 [get_ports {io_switch[1]}]
set_property PACKAGE_PIN W16 [get_ports {io_switch[2]}]
    set_property IOSTANDARD LVCMOS33 [get_ports {io_switch[2]}]
set_property PACKAGE_PIN W17 [get_ports {io_switch[3]}]
    set_property IOSTANDARD LVCMOS33 [get_ports {io_switch[3]}]
set_property PACKAGE_PIN W15 [get_ports {io_switch[4]}]
    set_property IOSTANDARD LVCMOS33 [get_ports {io_switch[4]}]
set_property PACKAGE_PIN V15 [get_ports {io_switch[5]}]
    set_property IOSTANDARD LVCMOS33 [get_ports {io_switch[5]}]
set_property PACKAGE_PIN W14 [get_ports {io_switch[6]}]
    set_property IOSTANDARD LVCMOS33 [get_ports {io_switch[6]}]
set_property PACKAGE_PIN W13 [get_ports {io_switch[7]}]
    set_property IOSTANDARD LVCMOS33 [get_ports {io_switch[7]}]
set_property PACKAGE_PIN V2 [get_ports {io_switch[8]}]
    set_property IOSTANDARD LVCMOS33 [get_ports {io_switch[8]}]
set_property PACKAGE_PIN T3 [get_ports {io_switch[9]}]
    set_property IOSTANDARD LVCMOS33 [get_ports {io_switch[9]}]
set_property PACKAGE_PIN T2 [get_ports {io_switch[10]}]
    set_property IOSTANDARD LVCMOS33 [get_ports {io_switch[10]}]
set_property PACKAGE_PIN R3 [get_ports {io_switch[11]}]
    set_property IOSTANDARD LVCMOS33 [get_ports {io_switch[11]}]
set_property PACKAGE_PIN W2 [get_ports {io_switch[12]}]
    set_property IOSTANDARD LVCMOS33 [get_ports {io_switch[12]}]
set_property PACKAGE_PIN U1 [get_ports {io_switch[13]}]
    set_property IOSTANDARD LVCMOS33 [get_ports {io_switch[13]}]
set_property PACKAGE_PIN T1 [get_ports {io_switch[14]}]
    set_property IOSTANDARD LVCMOS33 [get_ports {io_switch[14]}]
set_property PACKAGE_PIN R2 [get_ports {io_switch[15]}]
    set_property IOSTANDARD LVCMOS33 [get_ports {io_switch[15]}]


## LEDs
set_property PACKAGE_PIN U16 [get_ports {io_led[0]}]
    set_property IOSTANDARD LVCMOS33 [get_ports {io_led[0]}]
set_property PACKAGE_PIN E19 [get_ports {io_led[1]}]
    set_property IOSTANDARD LVCMOS33 [get_ports {io_led[1]}]
set_property PACKAGE_PIN U19 [get_ports {io_led[2]}]
    set_property IOSTANDARD LVCMOS33 [get_ports {io_led[2]}]
set_property PACKAGE_PIN V19 [get_ports {io_led[3]}]
    set_property IOSTANDARD LVCMOS33 [get_ports {io_led[3]}]
set_property PACKAGE_PIN W18 [get_ports {io_led[4]}]
    set_property IOSTANDARD LVCMOS33 [get_ports {io_led[4]}]
set_property PACKAGE_PIN U15 [get_ports {io_led[5]}]
    set_property IOSTANDARD LVCMOS33 [get_ports {io_led[5]}]
set_property PACKAGE_PIN U14 [get_ports {io_led[6]}]
    set_property IOSTANDARD LVCMOS33 [get_ports {io_led[6]}]
set_property PACKAGE_PIN V14 [get_ports {io_led[7]}]
    set_property IOSTANDARD LVCMOS33 [get_ports {io_led[7]}]
set_property PACKAGE_PIN V13 [get_ports {io_led[8]}]
    set_property IOSTANDARD LVCMOS33 [get_ports {io_led[8]}]
set_property PACKAGE_PIN V3 [get_ports {io_led[9]}]
    set_property IOSTANDARD LVCMOS33 [get_ports {io_led[9]}]
set_property PACKAGE_PIN W3 [get_ports {io_led[10]}]
    set_property IOSTANDARD LVCMOS33 [get_ports {io_led[10]}]
set_property PACKAGE_PIN U3 [get_ports {io_led[11]}]
    set_property IOSTANDARD LVCMOS33 [get_ports {io_led[11]}]
set_property PACKAGE_PIN P3 [get_ports {io_led[12]}]
    set_property IOSTANDARD LVCMOS33 [get_ports {io_led[12]}]
set_property PACKAGE_PIN N3 [get_ports {io_led[13]}]
    set_property IOSTANDARD LVCMOS33 [get_ports {io_led[13]}]
set_property PACKAGE_PIN P1 [get_ports {io_led[14]}]
    set_property IOSTANDARD LVCMOS33 [get_ports {io_led[14]}]
set_property PACKAGE_PIN L1 [get_ports {io_led[15]}]
    set_property IOSTANDARD LVCMOS33 [get_ports {io_led[15]}]


##7 segment display
set_property PACKAGE_PIN U7 [get_ports io_segs[0]]
    set_property IOSTANDARD LVCMOS33 [get_ports io_segs[0]]
set_property PACKAGE_PIN V5 [get_ports io_segs[1]]
    set_property IOSTANDARD LVCMOS33 [get_ports io_segs[1]]
set_property PACKAGE_PIN U5 [get_ports io_segs[2]]
    set_property IOSTANDARD LVCMOS33 [get_ports io_segs[2]]
set_property PACKAGE_PIN V8 [get_ports io_segs[3]]
    set_property IOSTANDARD LVCMOS33 [get_ports io_segs[3]]
set_property PACKAGE_PIN U8 [get_ports io_segs[4]]
    set_property IOSTANDARD LVCMOS33 [get_ports io_segs[4]]
set_property PACKAGE_PIN W6 [get_ports io_segs[5]]
    set_property IOSTANDARD LVCMOS33 [get_ports io_segs[5]]
set_property PACKAGE_PIN W7 [get_ports io_segs[6]]
    set_property IOSTANDARD LVCMOS33 [get_ports io_segs[6]]
set_property PACKAGE_PIN V7 [get_ports io_segs[7]]
    set_property IOSTANDARD LVCMOS33 [get_ports io_segs[7]]

set_property PACKAGE_PIN U2 [get_ports {io_digit_mask[0]}]
    set_property IOSTANDARD LVCMOS33 [get_ports {io_digit_mask[0]}]
set_property PACKAGE_PIN U4 [get_ports {io_digit_mask[1]}]
    set_property IOSTANDARD LVCMOS33 [get_ports {io_digit_mask[1]}]
set_property PACKAGE_PIN V4 [get_ports {io_digit_mask[2]}]
    set_property IOSTANDARD LVCMOS33 [get_ports {io_digit_mask[2]}]
set_property PACKAGE_PIN W4 [get_ports {io_digit_mask[3]}]
    set_property IOSTANDARD LVCMOS33 [get_ports {io_digit_mask[3]}]


##Buttons
set_property PACKAGE_PIN U18 [get_ports reset]
    set_property IOSTANDARD LVCMOS33 [get_ports reset]
#set_property PACKAGE_PIN T18 [get_ports io_freqIncrease]
    #set_property IOSTANDARD LVCMOS33 [get_ports io_freqIncrease]
#set_property PACKAGE_PIN W19 [get_ports io_widthIncrease]
    #set_property IOSTANDARD LVCMOS33 [get_ports io_widthIncrease]
#set_property PACKAGE_PIN T17 [get_ports io_widthDecrease]
    #set_property IOSTANDARD LVCMOS33 [get_ports io_widthDecrease]
#set_property PACKAGE_PIN U17 [get_ports io_freqDecrease]
    #set_property IOSTANDARD LVCMOS33 [get_ports io_freqDecrease]



##Pmod Header JA
##Sch name = JA1
#set_property PACKAGE_PIN J1 [get_ports {JA[0]}]
    #set_property IOSTANDARD LVCMOS33 [get_ports {JA[0]}]
##Sch name = JA2
#set_property PACKAGE_PIN L2 [get_ports {JA[1]}]
    #set_property IOSTANDARD LVCMOS33 [get_ports {JA[1]}]
##Sch name = JA3
#set_property PACKAGE_PIN J2 [get_ports {JA[2]}]
    #set_property IOSTANDARD LVCMOS33 [get_ports {JA[2]}]
##Sch name = JA4
#set_property PACKAGE_PIN G2 [get_ports {JA[3]}]
    #set_property IOSTANDARD LVCMOS33 [get_ports {JA[3]}]
##Sch name = JA7
#set_property PACKAGE_PIN H1 [get_ports {JA[4]}]
    #set_property IOSTANDARD LVCMOS33 [get_ports {JA[4]}]
##Sch name = JA8
#set_property PACKAGE_PIN K2 [get_ports {JA[5]}]
    #set_property IOSTANDARD LVCMOS33 [get_ports {JA[5]}]
##Sch name = JA9
#set_property PACKAGE_PIN H2 [get_ports {JA[6]}]
    #set_property IOSTANDARD LVCMOS33 [get_ports {JA[6]}]
##Sch name = JA10
#set_property PACKAGE_PIN G3 [get_ports {JA[7]}]
    #set_property IOSTANDARD LVCMOS33 [get_ports {JA[7]}]



##Pmod Header JB
##Sch name = JB1
#set_property PACKAGE_PIN A14 [get_ports {JB[0]}]
    #set_property IOSTANDARD LVCMOS33 [get_ports {JB[0]}]
##Sch name = JB2
#set_property PACKAGE_PIN A16 [get_ports {JB[1]}]
    #set_property IOSTANDARD LVCMOS33 [get_ports {JB[1]}]
##Sch name = JB3
#set_property PACKAGE_PIN B15 [get_ports {JB[2]}]
    #set_property IOSTANDARD LVCMOS33 [get_ports {JB[2]}]
##Sch name = JB4
#set_property PACKAGE_PIN B16 [get_ports {JB[3]}]
    #set_property IOSTANDARD LVCMOS33 [get_ports {JB[3]}]
##Sch name = JB7
#set_property PACKAGE_PIN A15 [get_ports {JB[4]}]
    #set_property IOSTANDARD LVCMOS33 [get_ports {JB[4]}]
##Sch name = JB8
#set_property PACKAGE_PIN A17 [get_ports {JB[5]}]
    #set_property IOSTANDARD LVCMOS33 [get_ports {JB[5]}]
##Sch name = JB9
#set_property PACKAGE_PIN C15 [get_ports {JB[6]}]
    #set_property IOSTANDARD LVCMOS33 [get_ports {JB[6]}]
##Sch name = JB10
#set_property PACKAGE_PIN C16 [get_ports {JB[7]}]
    #set_property IOSTANDARD LVCMOS33 [get_ports {JB[7]}]



##Pmod Header JC
##Sch name = JC1
#set_property PACKAGE_PIN K17 [get_ports {JC[0]}]
    #set_property IOSTANDARD LVCMOS33 [get_ports {JC[0]}]
##Sch name = JC2
#set_property PACKAGE_PIN M18 [get_ports {JC[1]}]
    #set_property IOSTANDARD LVCMOS33 [get_ports {JC[1]}]
##Sch name = JC3
#set_property PACKAGE_PIN N17 [get_ports {JC[2]}]
    #set_property IOSTANDARD LVCMOS33 [get_ports {JC[2]}]
##Sch name = JC4
#set_property PACKAGE_PIN P18 [get_ports {JC[3]}]
    #set_property IOSTANDARD LVCMOS33 [get_ports {JC[3]}]
##Sch name = JC7
#set_property PACKAGE_PIN L17 [get_ports {JC[4]}]
    #set_property IOSTANDARD LVCMOS33 [get_ports {JC[4]}]
##Sch name = JC8
#set_property PACKAGE_PIN M19 [get_ports {JC[5]}]
    #set_property IOSTANDARD LVCMOS33 [get_ports {JC[5]}]
##Sch name = JC9
#set_property PACKAGE_PIN P17 [get_ports {JC[6]}]
    #set_property IOSTANDARD LVCMOS33 [get_ports {JC[6]}]
##Sch name = JC10
#set_property PACKAGE_PIN R18 [get_ports {JC[7]}]
    #set_property IOSTANDARD LVCMOS33 [get_ports {JC[7]}]


##Pmod Header JXADC
##Sch name = XA1_P
#set_property PACKAGE_PIN J3 [get_ports {JXADC[0]}]
    #set_property IOSTANDARD LVCMOS33 [get_ports {JXADC[0]}]
##Sch name = XA2_P
#set_property PACKAGE_PIN L3 [get_ports {JXADC[1]}]
    #set_property IOSTANDARD LVCMOS33 [get_ports {JXADC[1]}]
##Sch name = XA3_P
#set_property PACKAGE_PIN M2 [get_ports {JXADC[2]}]
    #set_property IOSTANDARD LVCMOS33 [get_ports {JXADC[2]}]
##Sch name = XA4_P
#set_property PACKAGE_PIN N2 [get_ports {JXADC[3]}]
    #set_property IOSTANDARD LVCMOS33 [get_ports {JXADC[3]}]
##Sch name = XA1_N
#set_property PACKAGE_PIN K3 [get_ports {JXADC[4]}]
    #set_property IOSTANDARD LVCMOS33 [get_ports {JXADC[4]}]
##Sch name = XA2_N
#set_property PACKAGE_PIN M3 [get_ports {JXADC[5]}]
    #set_property IOSTANDARD LVCMOS33 [get_ports {JXADC[5]}]
##Sch name = XA3_N
#set_property PACKAGE_PIN M1 [get_ports {JXADC[6]}]
    #set_property IOSTANDARD LVCMOS33 [get_ports {JXADC[6]}]
##Sch name = XA4_N
#set_property PACKAGE_PIN N1 [get_ports {JXADC[7]}]
    #set_property IOSTANDARD LVCMOS33 [get_ports {JXADC[7]}]



##VGA Connector
set_property PACKAGE_PIN G19 [get_ports {io_rgb[8]}]
    set_property IOSTANDARD LVCMOS33 [get_ports {io_rgb[8]}]
set_property PACKAGE_PIN H19 [get_ports {io_rgb[9]}]
    set_property IOSTANDARD LVCMOS33 [get_ports {io_rgb[9]}]
set_property PACKAGE_PIN J19 [get_ports {io_rgb[10]}]
    set_property IOSTANDARD LVCMOS33 [get_ports {io_rgb[10]}]
set_property PACKAGE_PIN N19 [get_ports {io_rgb[11]}]
    set_property IOSTANDARD LVCMOS33 [get_ports {io_rgb[11]}]
set_property PACKAGE_PIN N18 [get_ports {io_rgb[0]}]
    set_property IOSTANDARD LVCMOS33 [get_ports {io_rgb[0]}]
set_property PACKAGE_PIN L18 [get_ports {io_rgb[1]}]
    set_property IOSTANDARD LVCMOS33 [get_ports {io_rgb[1]}]
set_property PACKAGE_PIN K18 [get_ports {io_rgb[2]}]
    set_property IOSTANDARD LVCMOS33 [get_ports {io_rgb[2]}]
set_property PACKAGE_PIN J18 [get_ports {io_rgb[3]}]
    set_property IOSTANDARD LVCMOS33 [get_ports {io_rgb[3]}]
set_property PACKAGE_PIN J17 [get_ports {io_rgb[4]}]
    set_property IOSTANDARD LVCMOS33 [get_ports {io_rgb[4]}]
set_property PACKAGE_PIN H17 [get_ports {io_rgb[5]}]
    set_property IOSTANDARD LVCMOS33 [get_ports {io_rgb[5]}]
set_property PACKAGE_PIN G17 [get_ports {io_rgb[6]}]
    set_property IOSTANDARD LVCMOS33 [get_ports {io_rgb[6]}]
set_property PACKAGE_PIN D17 [get_ports {io_rgb[7]}]
    set_property IOSTANDARD LVCMOS33 [get_ports {io_rgb[7]}]
set_property PACKAGE_PIN P19 [get_ports io_hsync]		
    set_property IOSTANDARD LVCMOS33 [get_ports io_hsync]
set_property PACKAGE_PIN R19 [get_ports io_vsync]
    set_property IOSTANDARD LVCMOS33 [get_ports io_vsync]


##USB-RS232 Interface
set_property PACKAGE_PIN B18 [get_ports io_rx]
    set_property IOSTANDARD LVCMOS33 [get_ports io_rx]
set_property PACKAGE_PIN A18 [get_ports io_tx]
    set_property IOSTANDARD LVCMOS33 [get_ports io_tx]


##USB HID (PS/2)
#set_property PACKAGE_PIN C17 [get_ports PS2Clk]
    #set_property IOSTANDARD LVCMOS33 [get_ports PS2Clk]
    #set_property PULLUP true [get_ports PS2Clk]
#set_property PACKAGE_PIN B17 [get_ports PS2Data]
    #set_property IOSTANDARD LVCMOS33 [get_ports PS2Data]
    #set_property PULLUP true [get_ports PS2Data]


##Quad SPI Flash
##Note that CCLK_0 cannot be placed in 7 series devices. You can access it using the
##STARTUPE2 primitive.
#set_property PACKAGE_PIN D18 [get_ports {QspiDB[0]}]
    #set_property IOSTANDARD LVCMOS33 [get_ports {QspiDB[0]}]
#set_property PACKAGE_PIN D19 [get_ports {QspiDB[1]}]
    #set_property IOSTANDARD LVCMOS33 [get_ports {QspiDB[1]}]
#set_property PACKAGE_PIN G18 [get_ports {QspiDB[2]}]
    #set_property IOSTANDARD LVCMOS33 [get_ports {QspiDB[2]}]
#set_property PACKAGE_PIN F18 [get_ports {QspiDB[3]}]
    #set_property IOSTANDARD LVCMOS33 [get_ports {QspiDB[3]}]
#set_property PACKAGE_PIN K19 [get_ports QspiCSn]
    #set_property IOSTANDARD LVCMOS33 [get_ports QspiCSn]


## Configuration options, can be used for all designs
set_property CONFIG_VOLTAGE 3.3 [current_design]
set_property CFGBVS VCCO [current_design]
