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

source open_project.tcl

launch_simulation
restart
open_vcd
#log_wave -recursive [get_object /test/top/hdmi_display/*]
#log_vcd [get_object /test/top/hdmi_display/*]
#log_wave -recursive [get_object /test/top/*]
#log_vcd [get_object /test/top/*]
log_wave -recursive [get_object /test/top/hdmi_display/serdesBlue/osr10/*]
log_vcd [get_object /test/top/hdmi_display/serdesBlue/osr10/*]
run 1000000ns
close_vcd
close_sim
