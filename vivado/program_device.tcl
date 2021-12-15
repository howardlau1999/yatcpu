open_hw_manager
connect_hw_server -allow_non_jtag
refresh_hw_server [current_hw_server]
open_hw_target [lindex [get_hw_targets] 0]
set_property PROGRAM.FILE {riscv-basys3/riscv-basys3.runs/impl_1/Top.bit} [get_hw_devices xc7a35t_0]
program_hw_devices [get_hw_devices xc7a35t_0]
close_hw_target
