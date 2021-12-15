# Only tested on Vivado 2020.1 on Windows 10
create_project -force riscv-basys3 riscv-basys3 -part xc7a35tcpg236-1
add_files -norecurse {../verilog/BCD2Segments.v ../verilog/OnboardDigitDisplay.v ../verilog/Top.v ../verilog/SegmentMux.v}
update_compile_order -fileset sources_1
add_files -fileset constrs_1 -norecurse ./basys3.xdc
launch_runs impl_1 -to_step write_bitstream -jobs 4
wait_on_run [current_run]
