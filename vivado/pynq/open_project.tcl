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
set project_dir riscv-pynq
set project_name riscv-pynq
set part xc7z020clg400-1

# open the project. will create one if it doesn't exist
if {[file exist $project_dir]} {
    # check that it's a directory
    if {! [file isdirectory $project_dir]} {
        puts "$project_dir exists, but it's a file"
    }
    open_project $project_dir/$project_name.xpr -part $part
} else {
    source riscv-pynq.tcl
}