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

verilog:
	sbt run

test:
	sbt test

bitstream:
	pushd && cd vivado && vivado -mode batch -source generate_bitstream.tcl && popd

board:
	pushd && cd vivado && vivado -mode batch -source program_device.tcl && popd

simulation:
	pushd && cd vivado && vivado -mode batch -source run_simulation.tcl && popd

.PHONY: verilog test bitstream board simulation
