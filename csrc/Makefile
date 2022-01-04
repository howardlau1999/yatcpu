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

CC = clang
ARCHFLAGS ?= -march=rv32i -mabi=ilp32
CFLAGS ?= --target=riscv32-unknown-unknown-elf $(ARCHFLAGS)
CPPFLAGS = -O0
LD = ld.lld
LDFLAGS ?= $(ARCHFLAGS) -melf32lriscv -nostdlib -static -T lnk.ld  # -nostartfiles
OBJCOPY ?= llvm-objcopy
OBJCOPYFLAGS ?= -O binary -j .text -j .text.startup -j .rodata -j .sdata
DESTDIR ?= ../src/main/resources

.PHONY: all clean

asmbin: init.o $(PROG).o init.S mmio.h
	$(LD) $(LDFLAGS) -o $(PROG).out init.o $(PROG).o
	mkdir -p $(DESTDIR)
	$(OBJCOPY) $(OBJCOPYFLAGS) $(PROG).out $(DESTDIR)/$(PROG).asmbin

init.o: init.S
	$(CC) -c $(CFLAGS) $(CPPFLAGS) -o $@ $^

%.o: %.c
	$(CC) -c $(CFLAGS) $(CPPFLAGS) -o $@ $^

clean:
	$(RM) *.o *.out
