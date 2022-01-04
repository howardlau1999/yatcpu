// Copyright 2021 Howard Lau
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

#include "mmio.h"

#define MUL80(x) (((x) << 6) + ((x) << 4))

struct screen {
	unsigned char row, col;
} scr;

void copy_line(int prev, int cur) {
	int *prev_vram_start = ((int *) (MUL80(prev) + VRAM_BASE));
	int *cur_vram_start = ((int *) (MUL80(cur) + VRAM_BASE));
	for (int i = 0; i < 20; ++i) {
		prev_vram_start[i] = cur_vram_start[i];
	}
}

void write_char(int row, int col, unsigned char ch) {
	VRAM[MUL80(row) + col] = ch;
}

void move_to(int row, int col) {
	scr.row = row;
	scr.col = col;
}

void new_line() {
	scr.col = 0;
	if (scr.row == 29) {
		for (int i = 0; i < 29; ++i) {
			copy_line(i, i + 1);
		}
		int *vram = (int *) (MUL80(29) + VRAM_BASE);
		for (int i = 0; i < 20; ++i) {
			vram[i] = 0x20202020;
		}
	} else {
		++scr.row;
	}
}

void putch(unsigned char ch) {
	if (ch == '\n') {
		new_line();
	} else if (ch == '\r') { 
		scr.col = 0;
	} else {
		if (scr.col == 79) {
			new_line();
		}
		write_char(scr.row, scr.col, ch);
		++scr.col;
	}
}

void clear_screen() {
	scr.row = 0;
	scr.col = 0;
	int *vram = ((int *) VRAM_BASE);
	for (int i = 0; i < 600; ++i) vram[i] = 0x20202020;
}

void print_hex(unsigned int counter) {
	putch('0'); putch('x');
	for (int i = 7; i >= 0; --i) {
		unsigned int num = (counter >> (i << 2)) & 0xF;
		if (num < 10) {
			putch('0' + num);
		} else {
			putch('A' + num - 10);
		}
	}
}

void putstr(const char *s) {
	while (*s) {
		putch(*(s++));
	}
}

int hc = 1;
int fast = 0;

void print_timer() {
	putstr("Hardware timer count limit = ");
	print_hex(*TIMER_LIMIT);
	putstr(", enabled = ");
	print_hex(*TIMER_ENABLED);
	putch('\n');
}

void print_uart() {
	putstr("UART Baud rate = ");
	print_hex(*UART_BAUDRATE);
	putch('\n');
}

void handle_timer() {
	putstr("Timer trigger times = ");
	print_hex(hc++);
	putch('\n');
	int mode = ((hc & 0x10) >> 4);
	if (hc == 0x40) {
		putstr("Disable timer!\n");
		*TIMER_ENABLED = 0;
		print_timer();
		return;
	}
	if (fast ^ mode) {
		putstr("Switch timer frequency\n");
		if (fast == 0) {
			*TIMER_LIMIT = 25000000;
		} else {
		
			*TIMER_LIMIT = 100000000;
		}
		fast = mode;
		print_timer();
	}
}

void handle_uart() {
	unsigned int ch = *UART_RECV;
	*UART_SEND = ch;
	putstr("UART Recv hex = "); print_hex(ch); putstr(", ch = "); putch(ch); putch('\n');
}

void trap_handler(void *epc, unsigned int cause) {
	putstr("Interrupt! EPC = ");
	print_hex((unsigned int) epc);
	putstr(", CAUSE = ");
	print_hex(cause);
	putch('\n');
	switch (cause) {
	case 0x8000000B:
		handle_uart();
		break;
	default:
		handle_timer();
		break;
	}
}
extern void enable_interrupt();
extern unsigned int get_epc();
int main() {
	clear_screen();
	hc = 0;
	*TIMER_ENABLED = 1;
	putstr("YatCPU Demo Program ");
	putch(137);
	putstr("2021 Howard Lau\n");
	putstr("Hello, world!\n");
	putstr("Last EPC = ");
	print_hex(get_epc());
	putch('\n');
	print_timer();
	print_uart();
	*((int *) 0x4) = 0xDEADBEEF;
	unsigned int i = 0;
	enable_interrupt();
	for (;;) ;
}
