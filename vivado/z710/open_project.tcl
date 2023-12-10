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

# Only tested on Vivado 2020.1 on Windows 10

# set variables
set project_dir riscv-z710
set project_name riscv-z710
set part xc7z010clg400-1
set sources {../../verilog/z710/Top.v ../../verilog/z710/clock_control.v ../../verilog/z710/pass_through.v ../../verilog/z710/Top_reset.v ../../verilog/z710/uart_control.v}
set test_sources {../../verilog/z710/test.v ../../verilog/z710/top_test.v}

# open the project. will create one if it doesn't exist 
if {[file exist $project_dir]} {
    # check that it's a directory
    if {! [file isdirectory $project_dir]} {
        puts "$project_dir exists, but it's a file"
    }
    open_project $project_dir/$project_name.xpr -part $part 
} else {
    create_project $project_name $project_dir -part $part    
}

add_files -norecurse $sources
update_compile_order -fileset sources_1
add_files -fileset constrs_1 -norecurse z710.xdc
add_files -fileset sim_1 -norecurse $test_sources
update_compile_order -fileset sim_1
