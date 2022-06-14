## This file is a general .xdc for the PYNQ-Z1 board Rev. C
## To use it in a project:
## - uncomment the lines corresponding to used pins
## - rename the used ports (in each line, after get_ports) according to the top level signal names in the project

## Clock signal 125 MHz

set_property -dict {PACKAGE_PIN H16 IOSTANDARD LVCMOS33} [get_ports clock]
create_clock -period 8.000 -name sys_clk_pin -waveform {0.000 4.000} -add [get_ports clock]

##Switches

set_property -dict { PACKAGE_PIN M20   IOSTANDARD LVCMOS33 } [get_ports { io_debug_step }]; #IO_L7N_T1_AD2N_35 Sch=sw[0]
#set_property -dict { PACKAGE_PIN M19   IOSTANDARD LVCMOS33 } [get_ports { sw[1] }]; #IO_L7P_T1_AD2P_35 Sch=sw[1]

##RGB LEDs

#set_property -dict { PACKAGE_PIN L15   IOSTANDARD LVCMOS33 } [get_ports { led4_b }]; #IO_L22N_T3_AD7N_35 Sch=led4_b
#set_property -dict { PACKAGE_PIN G17   IOSTANDARD LVCMOS33 } [get_ports { led4_g }]; #IO_L16P_T2_35 Sch=led4_g
#set_property -dict { PACKAGE_PIN N15   IOSTANDARD LVCMOS33 } [get_ports { led4_r }]; #IO_L21P_T3_DQS_AD14P_35 Sch=led4_r
#set_property -dict { PACKAGE_PIN G14   IOSTANDARD LVCMOS33 } [get_ports { led5_b }]; #IO_0_35 Sch=led5_b
#set_property -dict { PACKAGE_PIN L14   IOSTANDARD LVCMOS33 } [get_ports { led5_g }]; #IO_L22P_T3_AD7P_35 Sch=led5_g
#set_property -dict { PACKAGE_PIN M15   IOSTANDARD LVCMOS33 } [get_ports { led5_r }]; #IO_L23N_T3_35 Sch=led5_r

##LEDs

set_property -dict {PACKAGE_PIN R14 IOSTANDARD LVCMOS33} [get_ports {io_led[0]}]
set_property -dict {PACKAGE_PIN P14 IOSTANDARD LVCMOS33} [get_ports {io_led[1]}]
set_property -dict {PACKAGE_PIN N16 IOSTANDARD LVCMOS33} [get_ports {io_led[2]}]
set_property -dict {PACKAGE_PIN M14 IOSTANDARD LVCMOS33} [get_ports {io_led[3]}]

##Buttons

set_property -dict {PACKAGE_PIN D19 IOSTANDARD LVCMOS33} [get_ports reset]
set_property -dict { PACKAGE_PIN D20   IOSTANDARD LVCMOS33 } [get_ports { io_debug_clk }]; #IO_L4N_T0_35 Sch=btn[1]
#set_property -dict { PACKAGE_PIN L20   IOSTANDARD LVCMOS33 } [get_ports { btn[2] }]; #IO_L9N_T1_DQS_AD3N_35 Sch=btn[2]
#set_property -dict { PACKAGE_PIN L19   IOSTANDARD LVCMOS33 } [get_ports { btn[3] }]; #IO_L9P_T1_DQS_AD3P_35 Sch=btn[3]

##Pmod Header JA

#set_property -dict { PACKAGE_PIN Y18   IOSTANDARD LVCMOS33 } [get_ports { io_rx }]; #IO_L17P_T2_34 Sch=ja_p[1]
#set_property -dict { PACKAGE_PIN Y19   IOSTANDARD LVCMOS33 } [get_ports { io_tx }]; #IO_L17N_T2_34 Sch=ja_n[1]
# set_property -dict {PACKAGE_PIN Y16 IOSTANDARD LVCMOS33} [get_ports io_rx]
# set_property -dict {PACKAGE_PIN Y17 IOSTANDARD LVCMOS33} [get_ports io_tx]
#set_property -dict { PACKAGE_PIN U18   IOSTANDARD LVCMOS33 } [get_ports { ja[4] }]; #IO_L12P_T1_MRCC_34 Sch=ja_p[3]
#set_property -dict { PACKAGE_PIN U19   IOSTANDARD LVCMOS33 } [get_ports { ja[5] }]; #IO_L12N_T1_MRCC_34 Sch=ja_n[3]
#set_property -dict { PACKAGE_PIN W18   IOSTANDARD LVCMOS33 } [get_ports { ja[6] }]; #IO_L22P_T3_34 Sch=ja_p[4]
#set_property -dict { PACKAGE_PIN W19   IOSTANDARD LVCMOS33 } [get_ports { ja[7] }]; #IO_L22N_T3_34 Sch=ja_n[4]

##Pmod Header JB

#set_property -dict { PACKAGE_PIN W14   IOSTANDARD LVCMOS33 } [get_ports { jb[0] }]; #IO_L8P_T1_34 Sch=jb_p[1]
#set_property -dict { PACKAGE_PIN Y14   IOSTANDARD LVCMOS33 } [get_ports { jb[1] }]; #IO_L8N_T1_34 Sch=jb_n[1]
set_property -dict {PACKAGE_PIN T11 IOSTANDARD LVCMOS33} [get_ports io_rx]
set_property -dict {PACKAGE_PIN T10 IOSTANDARD LVCMOS33} [get_ports io_tx]
#set_property -dict { PACKAGE_PIN V16   IOSTANDARD LVCMOS33 } [get_ports { jb[4] }]; #IO_L18P_T2_34 Sch=jb_p[3]
#set_property -dict { PACKAGE_PIN W16   IOSTANDARD LVCMOS33 } [get_ports { jb[5] }]; #IO_L18N_T2_34 Sch=jb_n[3]
#set_property -dict { PACKAGE_PIN V12   IOSTANDARD LVCMOS33 } [get_ports { jb[6] }]; #IO_L4P_T0_34 Sch=jb_p[4]
#set_property -dict { PACKAGE_PIN W13   IOSTANDARD LVCMOS33 } [get_ports { jb[7] }]; #IO_L4N_T0_34 Sch=jb_n[4]

##Audio Out

#set_property -dict { PACKAGE_PIN R18   IOSTANDARD LVCMOS33 } [get_ports { aud_pwm }]; #IO_L20N_T3_34 Sch=aud_pwm
#set_property -dict { PACKAGE_PIN T17   IOSTANDARD LVCMOS33 } [get_ports { aud_sd }]; #IO_L20P_T3_34 Sch=aud_sd

##Mic input

#set_property -dict { PACKAGE_PIN F17   IOSTANDARD LVCMOS33 } [get_ports { m_clk }]; #IO_L6N_T0_VREF_35 Sch=m_clk
#set_property -dict { PACKAGE_PIN G18   IOSTANDARD LVCMOS33 } [get_ports { m_data }]; #IO_L16N_T2_35 Sch=m_data

##ChipKit Single Ended Analog Inputs
##NOTE: The ck_an_p pins can be used as single ended analog inputs with voltages from 0-3.3V (Chipkit Analog pins A0-A5).
##      These signals should only be connected to the XADC core. When using these pins as digital I/O, use pins ck_io[14-19].

#set_property -dict { PACKAGE_PIN D18   IOSTANDARD LVCMOS33 } [get_ports { ck_an_n[0] }]; #IO_L3N_T0_DQS_AD1N_35 Sch=ck_an_n[0]
#set_property -dict { PACKAGE_PIN E17   IOSTANDARD LVCMOS33 } [get_ports { ck_an_p[0] }]; #IO_L3P_T0_DQS_AD1P_35 Sch=ck_an_p[0]
#set_property -dict { PACKAGE_PIN E19   IOSTANDARD LVCMOS33 } [get_ports { ck_an_n[1] }]; #IO_L5N_T0_AD9N_35 Sch=ck_an_n[1]
#set_property -dict { PACKAGE_PIN E18   IOSTANDARD LVCMOS33 } [get_ports { ck_an_p[1] }]; #IO_L5P_T0_AD9P_35 Sch=ck_an_p[1]
#set_property -dict { PACKAGE_PIN J14   IOSTANDARD LVCMOS33 } [get_ports { ck_an_n[2] }]; #IO_L20N_T3_AD6N_35 Sch=ck_an_n[2]
#set_property -dict { PACKAGE_PIN K14   IOSTANDARD LVCMOS33 } [get_ports { ck_an_p[2] }]; #IO_L20P_T3_AD6P_35 Sch=ck_an_p[2]
#set_property -dict { PACKAGE_PIN J16   IOSTANDARD LVCMOS33 } [get_ports { ck_an_n[3] }]; #IO_L24N_T3_AD15N_35 Sch=ck_an_n[3]
#set_property -dict { PACKAGE_PIN K16   IOSTANDARD LVCMOS33 } [get_ports { ck_an_p[3] }]; #IO_L24P_T3_AD15P_35 Sch=ck_an_p[3]
#set_property -dict { PACKAGE_PIN H20   IOSTANDARD LVCMOS33 } [get_ports { ck_an_n[4] }]; #IO_L17N_T2_AD5N_35 Sch=ck_an_n[4]
#set_property -dict { PACKAGE_PIN J20   IOSTANDARD LVCMOS33 } [get_ports { ck_an_p[4] }]; #IO_L17P_T2_AD5P_35 Sch=ck_an_p[4]
#set_property -dict { PACKAGE_PIN G20   IOSTANDARD LVCMOS33 } [get_ports { ck_an_n[5] }]; #IO_L18N_T2_AD13N_35 Sch=ck_an_n[5]
#set_property -dict { PACKAGE_PIN G19   IOSTANDARD LVCMOS33 } [get_ports { ck_an_p[5] }]; #IO_L18P_T2_AD13P_35 Sch=ck_an_p[5]

##ChipKit Digital I/O Low

#set_property -dict { PACKAGE_PIN T14   IOSTANDARD LVCMOS33 } [get_ports { ck_io[0] }]; #IO_L5P_T0_34 Sch=ck_io[0]
#set_property -dict { PACKAGE_PIN U12   IOSTANDARD LVCMOS33 } [get_ports { ck_io[1] }]; #IO_L2N_T0_34 Sch=ck_io[1]
#set_property -dict { PACKAGE_PIN U13   IOSTANDARD LVCMOS33 } [get_ports { ck_io[2] }]; #IO_L3P_T0_DQS_PUDC_B_34 Sch=ck_io[2]
#set_property -dict { PACKAGE_PIN V13   IOSTANDARD LVCMOS33 } [get_ports { ck_io[3] }]; #IO_L3N_T0_DQS_34 Sch=ck_io[3]
#set_property -dict { PACKAGE_PIN V15   IOSTANDARD LVCMOS33 } [get_ports { ck_io[4] }]; #IO_L10P_T1_34 Sch=ck_io[4]
#set_property -dict { PACKAGE_PIN T15   IOSTANDARD LVCMOS33 } [get_ports { ck_io[5] }]; #IO_L5N_T0_34 Sch=ck_io[5]
#set_property -dict { PACKAGE_PIN R16   IOSTANDARD LVCMOS33 } [get_ports { ck_io[6] }]; #IO_L19P_T3_34 Sch=ck_io[6]
#set_property -dict { PACKAGE_PIN U17   IOSTANDARD LVCMOS33 } [get_ports { ck_io[7] }]; #IO_L9N_T1_DQS_34 Sch=ck_io[7]
#set_property -dict { PACKAGE_PIN V17   IOSTANDARD LVCMOS33 } [get_ports { ck_io[8] }]; #IO_L21P_T3_DQS_34 Sch=ck_io[8]
#set_property -dict { PACKAGE_PIN V18   IOSTANDARD LVCMOS33 } [get_ports { ck_io[9] }]; #IO_L21N_T3_DQS_34 Sch=ck_io[9]
#set_property -dict { PACKAGE_PIN T16   IOSTANDARD LVCMOS33 } [get_ports { ck_io[10] }]; #IO_L9P_T1_DQS_34 Sch=ck_io[10]
#set_property -dict { PACKAGE_PIN R17   IOSTANDARD LVCMOS33 } [get_ports { ck_io[11] }]; #IO_L19N_T3_VREF_34 Sch=ck_io[11]
#set_property -dict { PACKAGE_PIN P18   IOSTANDARD LVCMOS33 } [get_ports { ck_io[12] }]; #IO_L23N_T3_34 Sch=ck_io[12]
#set_property -dict { PACKAGE_PIN N17   IOSTANDARD LVCMOS33 } [get_ports { ck_io[13] }]; #IO_L23P_T3_34 Sch=ck_io[13]

##ChipKit Digital I/O On Outer Analog Header
##NOTE: These pins should be used when using the analog header signals A0-A5 as digital I/O (Chipkit digital pins 14-19)

#set_property -dict { PACKAGE_PIN Y11   IOSTANDARD LVCMOS33 } [get_ports { ck_io[14] }]; #IO_L18N_T2_13 Sch=ck_a[0]
#set_property -dict { PACKAGE_PIN Y12   IOSTANDARD LVCMOS33 } [get_ports { ck_io[15] }]; #IO_L20P_T3_13 Sch=ck_a[1]
#set_property -dict { PACKAGE_PIN W11   IOSTANDARD LVCMOS33 } [get_ports { ck_io[16] }]; #IO_L18P_T2_13 Sch=ck_a[2]
#set_property -dict { PACKAGE_PIN V11   IOSTANDARD LVCMOS33 } [get_ports { ck_io[17] }]; #IO_L21P_T3_DQS_13 Sch=ck_a[3]
#set_property -dict { PACKAGE_PIN T5    IOSTANDARD LVCMOS33 } [get_ports { ck_io[18] }]; #IO_L19P_T3_13 Sch=ck_a[4]
#set_property -dict { PACKAGE_PIN U10   IOSTANDARD LVCMOS33 } [get_ports { ck_io[19] }]; #IO_L12N_T1_MRCC_13 Sch=ck_a[5]

##ChipKit Digital I/O On Inner Analog Header
##NOTE: These pins will need to be connected to the XADC core when used as differential analog inputs (Chipkit analog pins A6-A11)

#set_property -dict { PACKAGE_PIN B20   IOSTANDARD LVCMOS33 } [get_ports { ck_io[20] }]; #IO_L1N_T0_AD0N_35 Sch=ad_n[0]
#set_property -dict { PACKAGE_PIN C20   IOSTANDARD LVCMOS33 } [get_ports { ck_io[21] }]; #IO_L1P_T0_AD0P_35 Sch=ad_p[0]
#set_property -dict { PACKAGE_PIN F20   IOSTANDARD LVCMOS33 } [get_ports { ck_io[22] }]; #IO_L15N_T2_DQS_AD12N_35 Sch=ad_n[12]
#set_property -dict { PACKAGE_PIN F19   IOSTANDARD LVCMOS33 } [get_ports { ck_io[23] }]; #IO_L15P_T2_DQS_AD12P_35 Sch=ad_p[12]
#set_property -dict { PACKAGE_PIN A20   IOSTANDARD LVCMOS33 } [get_ports { ck_io[24] }]; #IO_L2N_T0_AD8N_35 Sch=ad_n[8]
#set_property -dict { PACKAGE_PIN B19   IOSTANDARD LVCMOS33 } [get_ports { ck_io[25] }]; #IO_L2P_T0_AD8P_35 Sch=ad_p[8]

##ChipKit Digital I/O High

#set_property -dict { PACKAGE_PIN U5    IOSTANDARD LVCMOS33 } [get_ports { ck_io[26] }]; #IO_L19N_T3_VREF_13 Sch=ck_io[26]
#set_property -dict { PACKAGE_PIN V5    IOSTANDARD LVCMOS33 } [get_ports { ck_io[27] }]; #IO_L6N_T0_VREF_13 Sch=ck_io[27]
#set_property -dict { PACKAGE_PIN V6    IOSTANDARD LVCMOS33 } [get_ports { ck_io[28] }]; #IO_L22P_T3_13 Sch=ck_io[28]
#set_property -dict { PACKAGE_PIN U7    IOSTANDARD LVCMOS33 } [get_ports { ck_io[29] }]; #IO_L11P_T1_SRCC_13 Sch=ck_io[29]
#set_property -dict { PACKAGE_PIN V7    IOSTANDARD LVCMOS33 } [get_ports { ck_io[30] }]; #IO_L11N_T1_SRCC_13 Sch=ck_io[30]
#set_property -dict { PACKAGE_PIN U8    IOSTANDARD LVCMOS33 } [get_ports { ck_io[31] }]; #IO_L17N_T2_13 Sch=ck_io[31]
#set_property -dict { PACKAGE_PIN V8    IOSTANDARD LVCMOS33 } [get_ports { ck_io[32] }]; #IO_L15P_T2_DQS_13 Sch=ck_io[32]
#set_property -dict { PACKAGE_PIN V10   IOSTANDARD LVCMOS33 } [get_ports { ck_io[33] }]; #IO_L21N_T3_DQS_13 Sch=ck_io[33]
#set_property -dict { PACKAGE_PIN W10   IOSTANDARD LVCMOS33 } [get_ports { ck_io[34] }]; #IO_L16P_T2_13 Sch=ck_io[34]
#set_property -dict { PACKAGE_PIN W6    IOSTANDARD LVCMOS33 } [get_ports { ck_io[35] }]; #IO_L22N_T3_13 Sch=ck_io[35]
#set_property -dict { PACKAGE_PIN Y6    IOSTANDARD LVCMOS33 } [get_ports { ck_io[36] }]; #IO_L13N_T2_MRCC_13 Sch=ck_io[36]
#set_property -dict { PACKAGE_PIN Y7    IOSTANDARD LVCMOS33 } [get_ports { ck_io[37] }]; #IO_L13P_T2_MRCC_13 Sch=ck_io[37]
#set_property -dict { PACKAGE_PIN W8    IOSTANDARD LVCMOS33 } [get_ports { ck_io[38] }]; #IO_L15N_T2_DQS_13 Sch=ck_io[38]
#set_property -dict { PACKAGE_PIN Y8    IOSTANDARD LVCMOS33 } [get_ports { ck_io[39] }]; #IO_L14N_T2_SRCC_13 Sch=ck_io[39]
#set_property -dict { PACKAGE_PIN W9    IOSTANDARD LVCMOS33 } [get_ports { ck_io[40] }]; #IO_L16N_T2_13 Sch=ck_io[40]
#set_property -dict { PACKAGE_PIN Y9    IOSTANDARD LVCMOS33 } [get_ports { ck_io[41] }]; #IO_L14P_T2_SRCC_13 Sch=ck_io[41]
#set_property -dict { PACKAGE_PIN Y13   IOSTANDARD LVCMOS33 } [get_ports { ck_io[42] }]; #IO_L20N_T3_13 Sch=ck_ioa

## ChipKit SPI

#set_property -dict { PACKAGE_PIN W15   IOSTANDARD LVCMOS33 } [get_ports { ck_miso }]; #IO_L10N_T1_34 Sch=ck_miso
#set_property -dict { PACKAGE_PIN T12   IOSTANDARD LVCMOS33 } [get_ports { ck_mosi }]; #IO_L2P_T0_34 Sch=ck_mosi
#set_property -dict { PACKAGE_PIN H15   IOSTANDARD LVCMOS33 } [get_ports { ck_sck }]; #IO_L19P_T3_35 Sch=ck_sck
#set_property -dict { PACKAGE_PIN F16   IOSTANDARD LVCMOS33 } [get_ports { ck_ss }]; #IO_L6P_T0_35 Sch=ck_ss

## ChipKit I2C

#set_property -dict { PACKAGE_PIN P16   IOSTANDARD LVCMOS33 } [get_ports { ck_scl }]; #IO_L24N_T3_34 Sch=ck_scl
#set_property -dict { PACKAGE_PIN P15   IOSTANDARD LVCMOS33 } [get_ports { ck_sda }]; #IO_L24P_T3_34 Sch=ck_sda

##HDMI Rx

#set_property -dict { PACKAGE_PIN H17   IOSTANDARD LVCMOS33 } [get_ports { hdmi_rx_cec }]; #IO_L13N_T2_MRCC_35 Sch=hdmi_rx_cec
set_property -dict { PACKAGE_PIN P19   IOSTANDARD TMDS_33  } [get_ports { io_debug_hdmi_clk_n }]; #IO_L13N_T2_MRCC_34 Sch=hdmi_rx_clk_n
set_property -dict { PACKAGE_PIN N18   IOSTANDARD TMDS_33  } [get_ports { io_debug_hdmi_clk_p }]; #IO_L13P_T2_MRCC_34 Sch=hdmi_rx_clk_p
set_property -dict { PACKAGE_PIN W20   IOSTANDARD TMDS_33  } [get_ports { io_debug_hdmi_data_n[0] }]; #IO_L16N_T2_34 Sch=hdmi_rx_d_n[0]
set_property -dict { PACKAGE_PIN V20   IOSTANDARD TMDS_33  } [get_ports { io_debug_hdmi_data_p[0] }]; #IO_L16P_T2_34 Sch=hdmi_rx_d_p[0]
set_property -dict { PACKAGE_PIN U20   IOSTANDARD TMDS_33  } [get_ports { io_debug_hdmi_data_n[1] }]; #IO_L15N_T2_DQS_34 Sch=hdmi_rx_d_n[1]
set_property -dict { PACKAGE_PIN T20   IOSTANDARD TMDS_33  } [get_ports { io_debug_hdmi_data_p[1] }]; #IO_L15P_T2_DQS_34 Sch=hdmi_rx_d_p[1]
set_property -dict { PACKAGE_PIN P20   IOSTANDARD TMDS_33  } [get_ports { io_debug_hdmi_data_n[2] }]; #IO_L14N_T2_SRCC_34 Sch=hdmi_rx_d_n[2]
set_property -dict { PACKAGE_PIN N20   IOSTANDARD TMDS_33  } [get_ports { io_debug_hdmi_data_p[2] }]; #IO_L14P_T2_SRCC_34 Sch=hdmi_rx_d_p[2]
set_property -dict { PACKAGE_PIN T19   IOSTANDARD LVCMOS33 } [get_ports { io_debug_hdmi_hpdn }]; #IO_25_34 Sch=hdmi_rx_hpd
#set_property -dict { PACKAGE_PIN U14   IOSTANDARD LVCMOS33 } [get_ports { hdmi_rx_scl }]; #IO_L11P_T1_SRCC_34 Sch=hdmi_rx_scl
#set_property -dict { PACKAGE_PIN U15   IOSTANDARD LVCMOS33 } [get_ports { hdmi_rx_sda }]; #IO_L11N_T1_SRCC_34 Sch=hdmi_rx_sda

##HDMI Tx

#set_property -dict { PACKAGE_PIN G15   IOSTANDARD LVCMOS33 } [get_ports { hdmi_tx_cec }]; #IO_L19N_T3_VREF_35 Sch=hdmi_tx_cec
set_property -dict {PACKAGE_PIN L17 IOSTANDARD TMDS_33} [get_ports io_hdmi_clk_n]
set_property -dict {PACKAGE_PIN L16 IOSTANDARD TMDS_33} [get_ports io_hdmi_clk_p]
set_property -dict {PACKAGE_PIN K18 IOSTANDARD TMDS_33} [get_ports {io_hdmi_data_n[0]}]
set_property -dict {PACKAGE_PIN K17 IOSTANDARD TMDS_33} [get_ports {io_hdmi_data_p[0]}]
set_property -dict {PACKAGE_PIN J19 IOSTANDARD TMDS_33} [get_ports {io_hdmi_data_n[1]}]
set_property -dict {PACKAGE_PIN K19 IOSTANDARD TMDS_33} [get_ports {io_hdmi_data_p[1]}]
set_property -dict {PACKAGE_PIN H18 IOSTANDARD TMDS_33} [get_ports {io_hdmi_data_n[2]}]
set_property -dict {PACKAGE_PIN J18 IOSTANDARD TMDS_33} [get_ports {io_hdmi_data_p[2]}]
set_property -dict {PACKAGE_PIN R19 IOSTANDARD LVCMOS33} [get_ports io_hdmi_hpdn]
#set_property -dict { PACKAGE_PIN M17   IOSTANDARD LVCMOS33 } [get_ports { hdmi_tx_scl }]; #IO_L8P_T1_AD10P_35 Sch=hdmi_tx_scl
#set_property -dict { PACKAGE_PIN M18   IOSTANDARD LVCMOS33 } [get_ports { hdmi_tx_sda }]; #IO_L8N_T1_AD10N_35 Sch=hdmi_tx_sda

##Crypto SDA

#set_property -dict { PACKAGE_PIN J15   IOSTANDARD LVCMOS33 } [get_ports { crypto_sda }]; #IO_25_35 Sch=crypto_sda



connect_debug_port u_ila_0/probe0 [get_nets [list {cpu/cpu/clint/io_id_interrupt_handler_address[0]} {cpu/cpu/clint/io_id_interrupt_handler_address[1]} {cpu/cpu/clint/io_id_interrupt_handler_address[2]} {cpu/cpu/clint/io_id_interrupt_handler_address[3]} {cpu/cpu/clint/io_id_interrupt_handler_address[4]} {cpu/cpu/clint/io_id_interrupt_handler_address[5]} {cpu/cpu/clint/io_id_interrupt_handler_address[6]} {cpu/cpu/clint/io_id_interrupt_handler_address[7]} {cpu/cpu/clint/io_id_interrupt_handler_address[8]} {cpu/cpu/clint/io_id_interrupt_handler_address[9]} {cpu/cpu/clint/io_id_interrupt_handler_address[10]} {cpu/cpu/clint/io_id_interrupt_handler_address[11]} {cpu/cpu/clint/io_id_interrupt_handler_address[12]} {cpu/cpu/clint/io_id_interrupt_handler_address[13]} {cpu/cpu/clint/io_id_interrupt_handler_address[14]} {cpu/cpu/clint/io_id_interrupt_handler_address[15]} {cpu/cpu/clint/io_id_interrupt_handler_address[16]} {cpu/cpu/clint/io_id_interrupt_handler_address[17]} {cpu/cpu/clint/io_id_interrupt_handler_address[18]} {cpu/cpu/clint/io_id_interrupt_handler_address[19]} {cpu/cpu/clint/io_id_interrupt_handler_address[20]} {cpu/cpu/clint/io_id_interrupt_handler_address[21]} {cpu/cpu/clint/io_id_interrupt_handler_address[22]} {cpu/cpu/clint/io_id_interrupt_handler_address[23]} {cpu/cpu/clint/io_id_interrupt_handler_address[24]} {cpu/cpu/clint/io_id_interrupt_handler_address[25]} {cpu/cpu/clint/io_id_interrupt_handler_address[26]} {cpu/cpu/clint/io_id_interrupt_handler_address[27]} {cpu/cpu/clint/io_id_interrupt_handler_address[28]} {cpu/cpu/clint/io_id_interrupt_handler_address[29]} {cpu/cpu/clint/io_id_interrupt_handler_address[30]} {cpu/cpu/clint/io_id_interrupt_handler_address[31]}]]
connect_debug_port u_ila_0/probe1 [get_nets [list {cpu/cpu/csr_regs/io_clint_csr_mepc[0]} {cpu/cpu/csr_regs/io_clint_csr_mepc[1]} {cpu/cpu/csr_regs/io_clint_csr_mepc[2]} {cpu/cpu/csr_regs/io_clint_csr_mepc[3]} {cpu/cpu/csr_regs/io_clint_csr_mepc[4]} {cpu/cpu/csr_regs/io_clint_csr_mepc[5]} {cpu/cpu/csr_regs/io_clint_csr_mepc[6]} {cpu/cpu/csr_regs/io_clint_csr_mepc[7]} {cpu/cpu/csr_regs/io_clint_csr_mepc[8]} {cpu/cpu/csr_regs/io_clint_csr_mepc[9]} {cpu/cpu/csr_regs/io_clint_csr_mepc[10]} {cpu/cpu/csr_regs/io_clint_csr_mepc[11]} {cpu/cpu/csr_regs/io_clint_csr_mepc[12]} {cpu/cpu/csr_regs/io_clint_csr_mepc[13]} {cpu/cpu/csr_regs/io_clint_csr_mepc[14]} {cpu/cpu/csr_regs/io_clint_csr_mepc[15]} {cpu/cpu/csr_regs/io_clint_csr_mepc[16]} {cpu/cpu/csr_regs/io_clint_csr_mepc[17]} {cpu/cpu/csr_regs/io_clint_csr_mepc[18]} {cpu/cpu/csr_regs/io_clint_csr_mepc[19]} {cpu/cpu/csr_regs/io_clint_csr_mepc[20]} {cpu/cpu/csr_regs/io_clint_csr_mepc[21]} {cpu/cpu/csr_regs/io_clint_csr_mepc[22]} {cpu/cpu/csr_regs/io_clint_csr_mepc[23]} {cpu/cpu/csr_regs/io_clint_csr_mepc[24]} {cpu/cpu/csr_regs/io_clint_csr_mepc[25]} {cpu/cpu/csr_regs/io_clint_csr_mepc[26]} {cpu/cpu/csr_regs/io_clint_csr_mepc[27]} {cpu/cpu/csr_regs/io_clint_csr_mepc[28]} {cpu/cpu/csr_regs/io_clint_csr_mepc[29]} {cpu/cpu/csr_regs/io_clint_csr_mepc[30]} {cpu/cpu/csr_regs/io_clint_csr_mepc[31]}]]
connect_debug_port u_ila_0/probe2 [get_nets [list {cpu/cpu/csr_regs/io_clint_csr_mstatus[0]} {cpu/cpu/csr_regs/io_clint_csr_mstatus[1]} {cpu/cpu/csr_regs/io_clint_csr_mstatus[2]} {cpu/cpu/csr_regs/io_clint_csr_mstatus[3]} {cpu/cpu/csr_regs/io_clint_csr_mstatus[4]} {cpu/cpu/csr_regs/io_clint_csr_mstatus[5]} {cpu/cpu/csr_regs/io_clint_csr_mstatus[6]} {cpu/cpu/csr_regs/io_clint_csr_mstatus[7]} {cpu/cpu/csr_regs/io_clint_csr_mstatus[8]} {cpu/cpu/csr_regs/io_clint_csr_mstatus[9]} {cpu/cpu/csr_regs/io_clint_csr_mstatus[10]} {cpu/cpu/csr_regs/io_clint_csr_mstatus[11]} {cpu/cpu/csr_regs/io_clint_csr_mstatus[12]} {cpu/cpu/csr_regs/io_clint_csr_mstatus[13]} {cpu/cpu/csr_regs/io_clint_csr_mstatus[14]} {cpu/cpu/csr_regs/io_clint_csr_mstatus[15]} {cpu/cpu/csr_regs/io_clint_csr_mstatus[16]} {cpu/cpu/csr_regs/io_clint_csr_mstatus[17]} {cpu/cpu/csr_regs/io_clint_csr_mstatus[18]} {cpu/cpu/csr_regs/io_clint_csr_mstatus[19]} {cpu/cpu/csr_regs/io_clint_csr_mstatus[20]} {cpu/cpu/csr_regs/io_clint_csr_mstatus[21]} {cpu/cpu/csr_regs/io_clint_csr_mstatus[22]} {cpu/cpu/csr_regs/io_clint_csr_mstatus[23]} {cpu/cpu/csr_regs/io_clint_csr_mstatus[24]} {cpu/cpu/csr_regs/io_clint_csr_mstatus[25]} {cpu/cpu/csr_regs/io_clint_csr_mstatus[26]} {cpu/cpu/csr_regs/io_clint_csr_mstatus[27]} {cpu/cpu/csr_regs/io_clint_csr_mstatus[28]} {cpu/cpu/csr_regs/io_clint_csr_mstatus[29]} {cpu/cpu/csr_regs/io_clint_csr_mstatus[30]} {cpu/cpu/csr_regs/io_clint_csr_mstatus[31]}]]
connect_debug_port u_ila_0/probe3 [get_nets [list {cpu/cpu/csr_regs/io_clint_csr_mtvec[0]} {cpu/cpu/csr_regs/io_clint_csr_mtvec[1]} {cpu/cpu/csr_regs/io_clint_csr_mtvec[2]} {cpu/cpu/csr_regs/io_clint_csr_mtvec[3]} {cpu/cpu/csr_regs/io_clint_csr_mtvec[4]} {cpu/cpu/csr_regs/io_clint_csr_mtvec[5]} {cpu/cpu/csr_regs/io_clint_csr_mtvec[6]} {cpu/cpu/csr_regs/io_clint_csr_mtvec[7]} {cpu/cpu/csr_regs/io_clint_csr_mtvec[8]} {cpu/cpu/csr_regs/io_clint_csr_mtvec[9]} {cpu/cpu/csr_regs/io_clint_csr_mtvec[10]} {cpu/cpu/csr_regs/io_clint_csr_mtvec[11]} {cpu/cpu/csr_regs/io_clint_csr_mtvec[12]} {cpu/cpu/csr_regs/io_clint_csr_mtvec[13]} {cpu/cpu/csr_regs/io_clint_csr_mtvec[14]} {cpu/cpu/csr_regs/io_clint_csr_mtvec[15]} {cpu/cpu/csr_regs/io_clint_csr_mtvec[16]} {cpu/cpu/csr_regs/io_clint_csr_mtvec[17]} {cpu/cpu/csr_regs/io_clint_csr_mtvec[18]} {cpu/cpu/csr_regs/io_clint_csr_mtvec[19]} {cpu/cpu/csr_regs/io_clint_csr_mtvec[20]} {cpu/cpu/csr_regs/io_clint_csr_mtvec[21]} {cpu/cpu/csr_regs/io_clint_csr_mtvec[22]} {cpu/cpu/csr_regs/io_clint_csr_mtvec[23]} {cpu/cpu/csr_regs/io_clint_csr_mtvec[24]} {cpu/cpu/csr_regs/io_clint_csr_mtvec[25]} {cpu/cpu/csr_regs/io_clint_csr_mtvec[26]} {cpu/cpu/csr_regs/io_clint_csr_mtvec[27]} {cpu/cpu/csr_regs/io_clint_csr_mtvec[28]} {cpu/cpu/csr_regs/io_clint_csr_mtvec[29]} {cpu/cpu/csr_regs/io_clint_csr_mtvec[30]} {cpu/cpu/csr_regs/io_clint_csr_mtvec[31]}]]
connect_debug_port u_ila_0/probe4 [get_nets [list {cpu/cpu/inst_fetch/io_id_instruction_address[0]} {cpu/cpu/inst_fetch/io_id_instruction_address[1]} {cpu/cpu/inst_fetch/io_id_instruction_address[2]} {cpu/cpu/inst_fetch/io_id_instruction_address[3]} {cpu/cpu/inst_fetch/io_id_instruction_address[4]} {cpu/cpu/inst_fetch/io_id_instruction_address[5]} {cpu/cpu/inst_fetch/io_id_instruction_address[6]} {cpu/cpu/inst_fetch/io_id_instruction_address[7]} {cpu/cpu/inst_fetch/io_id_instruction_address[8]} {cpu/cpu/inst_fetch/io_id_instruction_address[9]} {cpu/cpu/inst_fetch/io_id_instruction_address[10]} {cpu/cpu/inst_fetch/io_id_instruction_address[11]} {cpu/cpu/inst_fetch/io_id_instruction_address[12]} {cpu/cpu/inst_fetch/io_id_instruction_address[13]} {cpu/cpu/inst_fetch/io_id_instruction_address[14]} {cpu/cpu/inst_fetch/io_id_instruction_address[15]} {cpu/cpu/inst_fetch/io_id_instruction_address[16]} {cpu/cpu/inst_fetch/io_id_instruction_address[17]} {cpu/cpu/inst_fetch/io_id_instruction_address[18]} {cpu/cpu/inst_fetch/io_id_instruction_address[19]} {cpu/cpu/inst_fetch/io_id_instruction_address[20]} {cpu/cpu/inst_fetch/io_id_instruction_address[21]} {cpu/cpu/inst_fetch/io_id_instruction_address[22]} {cpu/cpu/inst_fetch/io_id_instruction_address[23]} {cpu/cpu/inst_fetch/io_id_instruction_address[24]} {cpu/cpu/inst_fetch/io_id_instruction_address[25]} {cpu/cpu/inst_fetch/io_id_instruction_address[26]} {cpu/cpu/inst_fetch/io_id_instruction_address[27]} {cpu/cpu/inst_fetch/io_id_instruction_address[28]} {cpu/cpu/inst_fetch/io_id_instruction_address[29]} {cpu/cpu/inst_fetch/io_id_instruction_address[30]} {cpu/cpu/inst_fetch/io_id_instruction_address[31]}]]
connect_debug_port u_ila_0/probe5 [get_nets [list {cpu_io_interrupt_flag[0]} {cpu_io_interrupt_flag[1]} {cpu_io_interrupt_flag[2]} {cpu_io_interrupt_flag[3]} {cpu_io_interrupt_flag[4]} {cpu_io_interrupt_flag[5]} {cpu_io_interrupt_flag[6]} {cpu_io_interrupt_flag[7]} {cpu_io_interrupt_flag[8]} {cpu_io_interrupt_flag[9]} {cpu_io_interrupt_flag[10]} {cpu_io_interrupt_flag[11]} {cpu_io_interrupt_flag[12]} {cpu_io_interrupt_flag[13]} {cpu_io_interrupt_flag[14]} {cpu_io_interrupt_flag[15]} {cpu_io_interrupt_flag[16]} {cpu_io_interrupt_flag[17]} {cpu_io_interrupt_flag[18]} {cpu_io_interrupt_flag[19]} {cpu_io_interrupt_flag[20]} {cpu_io_interrupt_flag[21]} {cpu_io_interrupt_flag[22]} {cpu_io_interrupt_flag[23]} {cpu_io_interrupt_flag[24]} {cpu_io_interrupt_flag[25]} {cpu_io_interrupt_flag[26]} {cpu_io_interrupt_flag[27]} {cpu_io_interrupt_flag[28]} {cpu_io_interrupt_flag[29]} {cpu_io_interrupt_flag[30]} {cpu_io_interrupt_flag[31]}]]
connect_debug_port u_ila_0/probe6 [get_nets [list cpu/cpu/clint/io_exception_signal]]
connect_debug_port u_ila_0/probe7 [get_nets [list cpu/cpu/clint/io_id_interrupt_assert]]
connect_debug_port u_ila_0/probe8 [get_nets [list uart_io_signal_interrupt]]

