RISCVCC ?= riscv64-unknown-elf-gcc
RISCVOBJCOPY ?= riscv64-unknown-elf-objcopy
ARCHFLAGS = -march=rv32i -mabi=ilp32
EXEFLAGS = -nostdlib -nostartfiles -static -T lnk.ld
OBJCOPYFLAGS = -O binary -j .text

DESTDIR ?= ../src/main/resources

.PHONY: all

asmbin: init.o $(PROG).o
	$(RISCVCC) $(ARCHFLAGS) $(EXEFLAGS) -o $(PROG).out init.o $(PROG).o
	mkdir -p $(DESTDIR)
	$(RISCVOBJCOPY) $(OBJCOPYFLAGS) $(PROG).out $(DESTDIR)/$(PROG).asmbin

init.o: init.S
	$(RISCVCC) $(ARCHFLAGS) -c -o init.o init.S

%.o: %.c
	$(RISCVCC) $(ARCHFLAGS) -c -o $@ $^


