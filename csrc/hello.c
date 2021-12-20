struct screen {
	unsigned char row, col;
};


void copy_line(int prev, int cur) {
	int *prev_vram_start = ((int *) (prev * 80 + 1024));
	int *cur_vram_start = ((int *) (cur * 80 + 1024));
	for (int i = 0; i < 20; ++i) {
		prev_vram_start[i] = cur_vram_start[i];
	}
}

void write_char(int row, int col, unsigned char ch) {
	unsigned char *vram = (unsigned char *) 1024;
	vram[row * 80 + col] = ch;
}

void move_to(struct screen *scrp, int row, int col) {
	scrp->row = row;
	scrp->col = col;
}

void new_line(struct screen *scrp) {
	scrp->col = 0;
	if (scrp->row == 29) {
		for (int i = 0; i < 29; ++i) {
			copy_line(i, i + 1);
		}
	} else {
		++scrp->row;
	}
}

void putch(struct screen *scrp, unsigned char ch) {
	if (ch == '\n') {
		new_line(scrp);
	} else if (ch == '\r') { 
		scrp->col = 0;
	} else {
		if (scrp->col == 79) {
			new_line(scrp);
		}
		write_char(scrp->row, scrp->col, ch);
		++scrp->col;
	}
}

void clear_screen(struct screen *scrp) {
	scrp->row = 0;
	scrp->col = 0;
	int *vram = ((int *) 1024);
	for (int i = 0; i < 600; ++i) vram[i] = 0x20202020;
}

void print_hex(struct screen *p, unsigned int counter) {
	putch(p, '0'); putch(p, 'x');
	for (int i = 7; i >= 0; --i) {
		unsigned int num = (counter >> (i * 4)) & 0xF;
		if (num < 10) {
			putch(p, '0' + num);
		} else {
			putch(p, 'A' + num - 10);
		}
	}
}

void print_regs() {
	unsigned int regs[32];
}

int main() {
	struct screen scr;
	struct screen *p = &scr;
	clear_screen(p);

	putch(p, 137);
	putch(p, '2'); putch(p, '0'); putch(p, '2'); putch(p, '1'); putch(p, ' ');
	putch(p, 'H'); putch(p, 'o'); putch(p, 'w'); putch(p, 'a'); putch(p, 'r'); putch(p, 'd'); putch(p, ' '); 
	putch(p, 'L'); putch(p, 'a'); putch(p, 'u'); putch(p, '\n'); 
	putch(p, 'H'); putch(p, 'e'); putch(p, 'l'); putch(p, 'l'); putch(p, 'o'); putch(p, ','); 
	putch(p, ' '); putch(p, 'w'); putch(p, 'o'); putch(p, 'r'); putch(p, 'l'); putch(p, 'd'); putch(p, '!');
	putch(p, '\n');
	
	*((int *) 0x4) = 0xDEADBEEF;
	unsigned int counter = 0;
	for (;;) {
		for (register int i = 0; i < 10000000; ++i);
		++counter;
		move_to(p, 2, 0);
		print_hex(p, counter);
	}
}

