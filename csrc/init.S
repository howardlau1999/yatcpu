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

.section .text
.globl _start
_start:
  li sp, 1020 # Initialize stack pointer
  call main   # Jump to main function
loop:
  j loop      # Loop forever
.globl enable_interrupt
enable_interrupt:
  la t0, __trap_entry
  csrrw t1, mtvec, t0 # setup trap vector base
  li t0, 0x1888
  csrrw t1, mstatus, t0 # enable interrupt
  ret
.globl get_epc
get_epc:
  csrr a0, mepc
  ret
__trap_entry:
  li t0, 32600
  sw ra, 4(t0)
  sw sp, 8(t0)
  sw gp, 12(t0)
  sw tp, 16(t0)
  sw t0, 20(t0)
  sw t1, 24(t0)
  sw t2, 28(t0)
  sw tp, 32(t0)
  sw s1, 36(t0)
  sw a0, 40(t0)
  sw a1, 44(t0)
  sw a2, 48(t0)
  sw a3, 52(t0)
  sw a4, 56(t0)
  sw a5, 60(t0)
  sw a6, 64(t0)
  sw a7, 68(t0)
  sw s2, 72(t0)
  sw s3, 76(t0)
  sw s4, 80(t0)
  sw s5, 84(t0)
  sw s6, 88(t0)
  sw s7, 92(t0)
  sw s8, 96(t0)
  sw s9, 100(t0)
  sw s10, 104(t0)
  sw s11, 108(t0)
  sw t3, 112(t0)
  sw t4, 116(t0)
  sw t5, 120(t0)
  sw t6, 124(t0)
  li sp, 32600
  csrr a0, mepc
  csrr a1, mcause
  call trap_handler
  li t0, 32600
  lw ra, 4(t0)
  lw sp, 8(t0)
  lw gp, 12(t0)
  lw tp, 16(t0)
  # t0
  lw t1, 24(t0)
  lw t2, 28(t0)
  lw tp, 32(t0)
  lw s1, 36(t0)
  lw a0, 40(t0)
  lw a1, 44(t0)
  lw a2, 48(t0)
  lw a3, 52(t0)
  lw a4, 56(t0)
  lw a5, 60(t0)
  lw a6, 64(t0)
  lw a7, 68(t0)
  lw s2, 72(t0)
  lw s3, 76(t0)
  lw s4, 80(t0)
  lw s5, 84(t0)
  lw s6, 88(t0)
  lw s7, 92(t0)
  lw s8, 96(t0)
  lw s9, 100(t0)
  lw s10, 104(t0)
  lw s11, 108(t0)
  lw t3, 112(t0)
  lw t4, 116(t0)
  lw t5, 120(t0)
  lw t6, 124(t0)
  mret
