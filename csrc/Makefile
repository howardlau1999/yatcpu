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

RISCVCC ?= riscv64-unknown-elf-gcc
RISCVOBJCOPY ?= riscv64-unknown-elf-objcopy
ARCHFLAGS = -march=rv32i -mabi=ilp32
EXEFLAGS = -nostdlib -nostartfiles -static -T lnk.ld
OBJCOPYFLAGS = -O binary -j .text -j .rodata

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


