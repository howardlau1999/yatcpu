.section .text
.globl _start
_start:
  li sp, 1020 # Initialize stack pointer
  call main   # Jump to main function
loop:
  j loop      # Loop forever