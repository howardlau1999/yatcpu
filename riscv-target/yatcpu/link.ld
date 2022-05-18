OUTPUT_ARCH( "riscv" )
ENTRY(rvtest_entry_point)

SECTIONS
{
  . = 0x00001000;
  .text : { *(.text.init) *(.text.startup) *(.text) }
  .data ALIGN(0x1000) : { *(.data*) *(.rodata*) *(.sdata*) }
  .tohost ALIGN(0x1000) : { *(.tohost) }
  .bss : { *(.bss) }
  _end = .;
}
