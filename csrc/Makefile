RISCVCC ?= riscv64-unknown-elf-gcc
RISCVOBJCOPY ?= riscv64-unknown-elf-objcopy
ARCHFLAGS = -march=rv32i -mabi=ilp32
EXEFLAGS = -nostdlib -nostartfiles -static  -T lnk.ld
OBJCOPYFLAGS = -O binary -j .text

DESTDIR ?= ../src/main/resources

%: %.o init.o
	$(RISCVCC) $(ARCHFLAGS) $(EXEFLAGS) -o $@.out init.o $@.o
	$(RISCVOBJCOPY) $(OBJCOPYFLAGS) $@.out $(DESTDIR)/$@.asmbin

init.o: init.S
	$(RISCVCC) $(ARCHFLAGS) -c -o init.o init.S

%.o: %.c
	$(RISCVCC) $(ARCHFLAGS) -c -o $@ $^
