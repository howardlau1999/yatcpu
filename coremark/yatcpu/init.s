.section .text.init
.globl _start
_start:
    li sp, 4092
    call main
    li x1, 0xBABECAFE
write_tohost:
    sw x1, tohost, x0
loop:
    j loop

.pushsection .tohost,"aw",@progbits
    .align 4
    .global tohost
    tohost: .word 0
.popsection