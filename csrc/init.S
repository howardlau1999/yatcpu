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

.section .text.init
.globl _start
_start:
  li sp, 0x10000000 # Initialize stack pointer
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
.globl enable_paging
enable_paging:
  li t0, 0x80000005 # ppn << 12 = 0x005000
  csrw satp, t0
  ret
.globl get_mtval
get_mtval:
  csrr a0, mtval
  ret

.globl get_epc
get_epc:
  csrr a0, mepc
  ret
.weak trap_handler
tran_handler:
  ret
__trap_entry:
  csrw mscratch, sp
  addi sp, sp, -128
  sw ra, 4(sp)

  sw gp, 12(sp)
  sw tp, 16(sp)
  sw t0, 20(sp)
  sw t1, 24(sp)
  sw t2, 28(sp)
  sw tp, 32(sp)
  sw s1, 36(sp)
  sw a0, 40(sp)
  sw a1, 44(sp)
  sw a2, 48(sp)
  sw a3, 52(sp)
  sw a4, 56(sp)
  sw a5, 60(sp)
  sw a6, 64(sp)
  sw a7, 68(sp)
  sw s2, 72(sp)
  sw s3, 76(sp)
  sw s4, 80(sp)
  sw s5, 84(sp)
  sw s6, 88(sp)
  sw s7, 92(sp)
  sw s8, 96(sp)
  sw s9, 100(sp)
  sw s10, 104(sp)
  sw s11, 108(sp)
  sw t3, 112(sp)
  sw t4, 116(sp)
  sw t5, 120(sp)
  sw t6, 124(sp)

  csrr a0, mepc
  csrr a1, mcause
  call trap_handler

  lw ra, 4(sp)

  lw gp, 12(sp)
  lw tp, 16(sp)
  lw t0, 20(sp)
  lw t1, 24(sp)
  lw t2, 28(sp)
  lw tp, 32(sp)
  lw s1, 36(sp)
  lw a0, 40(sp)
  lw a1, 44(sp)
  lw a2, 48(sp)
  lw a3, 52(sp)
  lw a4, 56(sp)
  lw a5, 60(sp)
  lw a6, 64(sp)
  lw a7, 68(sp)
  lw s2, 72(sp)
  lw s3, 76(sp)
  lw s4, 80(sp)
  lw s5, 84(sp)
  lw s6, 88(sp)
  lw s7, 92(sp)
  lw s8, 96(sp)
  lw s9, 100(sp)
  lw s10, 104(sp)
  lw s11, 108(sp)
  lw t3, 112(sp)
  lw t4, 116(sp)
  lw t5, 120(sp)
  lw t6, 124(sp)
  csrr sp, mscratch
  mret
