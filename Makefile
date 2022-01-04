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

test:
	sbt test

verilator:
	sbt "runMain board.verilator.VerilogGenerator"
	cd verilog/verilator && verilator --trace --exe --cc sim_main.cpp Top.v && make -C obj_dir -f VTop.mk

verilator-sim: verilator
	cd verilog/verilator && obj_dir/VTop $(SIM_TIME)

basys3:
	sbt "runMain board.basys3.VerilogGenerator"

bitstream: basys3
	cd vivado && vivado -mode batch -source generate_bitstream.tcl

program: bitstream
	cd vivado && vivado -mode batch -source program_device.tcl

vivado-sim: basys3
	cd vivado && vivado -mode batch -source run_simulation.tcl

.PHONY: basys3 verilator test bitstream program verilator-sim vivado-sim
