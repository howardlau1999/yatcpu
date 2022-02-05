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
set_param general.maxThreads 16

while 1 {
    if { [catch {launch_runs synth_1 -jobs 16 } ] } {
        regexp {ERROR: \[Common (\d+-\d+)]} $errorInfo -> code
        if { [string equal $code "12-978"] } {
            puts "Already generated and up-to-date"
            break
        } elseif { [string equal $code "17-69"] } {
            puts "Out of date, reset runs"
            reset_runs synth_1
            continue
        } else {
            puts "UNKNOWN ERROR!!! $errorInfo"
            exit
        }
    }
    break
}

wait_on_run synth_1

while 1 {
    if { [catch {launch_runs impl_1 -jobs 16 -to_step write_bitstream } ] } {
        regexp {ERROR: \[Vivado (\d+-\d+)]} $errorInfo -> code
        if { [string equal $code "12-978"] } {
            puts "Already generated and up-to-date"
            break
        } elseif { [string equal $code "12-1088"] } {
            puts "Out of date, reset runs"
            reset_runs impl_1
            continue
        } else {
            puts "UNKNOWN ERROR!!! $errorInfo"
            exit
        }
    }
    break
}

wait_on_run impl_1
