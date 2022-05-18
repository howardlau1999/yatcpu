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

#ifdef DEBUG
#include <stdio.h>
#endif

#include "mmio.h"

#define FALL_TIMER_LIMIT 50000000
#define ROWS 22
#define COLS 10
#define OFFSET_X 28
#define OFFSET_Y 3
#define SCREEN_COLS 80
#define SCREEN_ROWS 30

struct block {
	unsigned int shape[3];
	unsigned int xywh;
};

struct block current;

unsigned int score;

unsigned char *board;

#ifdef DEBUG
unsigned char screen[SCREEN_COLS * SCREEN_ROWS];
#endif

int wk_mul(int a, int b) {
  int r = 0;
  for (; b; a <<= 1, b >>= 1)
    if (b & 1)
      r += a;
  return r;
}

unsigned int make_xywh(unsigned int x, unsigned int y, unsigned int w, unsigned int h) {
	return (x << 12) | (y << 4) | (w << 2) | h;
}

void init_block(struct block *block, int type, int x, int y) {
	int w = 0; int h = 0;
	block->shape[0] = block->shape[1] = block->shape[2] = 0;
	switch(type) {
	case 0: // I
		block->shape[0] = 0xF;
		w = 3; h = 0;
	break;
	case 1: // O
		block->shape[0] = 0x3;
		block->shape[1] = 0x3;
		w = 1; h = 1;
	break;
	case 2: // J
		block->shape[0] = 0x4;
		block->shape[1] = 0x7;
		w = 2; h = 1;
		break;
	case 3: // T
		block->shape[0] = 0x2;
		block->shape[1] = 0x7;
		w = 2; h = 1;
		break;
	case 4: // L
		block->shape[0] = 0x1;
		block->shape[1] = 0x7;
		w = 2; h = 1;
		break;
	case 5: // Z
		block->shape[0] = 0x6;
		block->shape[1] = 0x3;
		w = 2; h = 1;
		break;
	case 6: // S
		block->shape[0] = 0x3;
		block->shape[1] = 0x6;
		w = 2; h = 1;
		break;
	}
	block->xywh = make_xywh(x, y, w, h);
}

unsigned int get_shape(struct block *block, unsigned int r, unsigned int c) {
	return (block->shape[r] & (1 << c)) >> c;
}

unsigned int check_bounds(struct block *block) {
	unsigned int xywh = block->xywh;
	unsigned int h = xywh & 0x3;
	unsigned int w = (xywh & 0xC) >> 2;
	unsigned int y = (xywh & 0xFF0) >> 4;
	unsigned int x = (xywh & 0xF000) >> 12;
	if (x < 0 || x + w >= COLS) return 0;
	if (y < 0 || y + h >= ROWS) return 0;
	return 1;
}

void copy_block(struct block *dst, struct block *src) {
	dst->xywh = src->xywh;
	dst->shape[0] = src->shape[0];
	dst->shape[1] = src->shape[1];
	dst->shape[2] = src->shape[2];
}

unsigned int check_collision(struct block *block) {	
	unsigned int xywh = block->xywh;
	unsigned int h = xywh & 0x3;
	unsigned int w = (xywh & 0xC) >> 2;
	unsigned int y = (xywh & 0xFF0) >> 4;
	unsigned int x = (xywh & 0xF000) >> 12;
	for (int r = 0; r <= h; ++r) {
		for (int c = 0; c <= w; ++c) {
                  if (get_shape(block, r, c) &&
                      board[wk_mul(y + r, COLS) + x + c])
                    return 0;
                }
	}
	return 1;
}

void putch_at(int x, int y, unsigned char ch) {
#ifdef DEBUG
	screen[wk_mul(OFFSET_Y + y, SCREEN_COLS) + x + OFFSET_X] = ch;
#else
	VRAM[wk_mul(OFFSET_Y + y, SCREEN_COLS) + x + OFFSET_X] = ch;
#endif
}

void block_move(struct block *block, int dir) {
	unsigned int xywh = block->xywh;
	unsigned int h = xywh & 0x3;
	unsigned int w = (xywh & 0xC) >> 2;
	unsigned int y = (xywh & 0xFF0) >> 4;
	unsigned int x = (xywh & 0xF000) >> 12;
	switch(dir) {
	case 0: // Left
		x--;
		break;
	case 1: // Right
		x++;
		break;
	case 2: // Down
		y++;
		break;
	default:
		break;
	}
	block->xywh = (x << 12) | (y << 4) | (w << 2) | h;
}

unsigned int move(struct block *block, int dir) {
	struct block moved;
	copy_block(&moved, block);
	block_move(&moved, dir);
	if (check_bounds(&moved) && check_collision(&moved)) {
		copy_block(block, &moved);
		return 1;
	}
	return 0;
}

void block_rotate(struct block *block, unsigned int clock) {
	unsigned int xywh = block->xywh;
	unsigned int h = xywh & 0x3;
	unsigned int w = (xywh & 0xC) >> 2;
	unsigned int y = (xywh & 0xFF0) >> 4;
	unsigned int x = (xywh & 0xF000) >> 12;
	unsigned int xyhw = make_xywh(x, y, h, w);
	unsigned int shape[3] = {0, 0, 0};
	if (clock) {
		for (int r = 0; r <= h; ++r) {
			for (int c = 0; c <= w; ++c) {
				shape[c] = shape[c] | (get_shape(block, r, c) << (h - r));
			}
		}
		
	} else {
		for (int r = 0; r <= h; ++r) {
			for (int c = 0; c <= w; ++c) {
				shape[w - c] = shape[w - c] | (get_shape(block, r, c) << r);
			}
		}
	}
	block->shape[0] = shape[0];
	block->shape[1] = shape[1];
	block->shape[2] = shape[2];
	block->xywh = xyhw;
}


void rotate(struct block *block, int clock) {
	struct block rotated;
	copy_block(&rotated, block);
	block_rotate(&rotated, clock);
	if (check_bounds(&rotated) && check_collision(&rotated)) {
		copy_block(block, &rotated);
		return;
	}
	unsigned int xywh = rotated.xywh;
	unsigned int h = xywh & 0x3;
	unsigned int w = (xywh & 0xc) >> 2;
	unsigned int y = (xywh & 0xff0) >> 4;
	unsigned int x = (xywh & 0xf000) >> 12;
	if (x + w >= COLS) {
		x = COLS - w - 1;
	}
	rotated.xywh = make_xywh(x, y, w, h);
	if (check_bounds(&rotated) && check_collision(&rotated)) {
		copy_block(block, &rotated);
	}
}

void clear_board() {
	for (int i = 0, s = wk_mul(ROWS, COLS); i < s; ++i) {
		board[i] = 0;
	}
}

void fix_block(struct block *block) {
	unsigned int xywh = block->xywh;
	unsigned int h = xywh & 0x3;
	unsigned int w = (xywh & 0xc) >> 2;
	unsigned int y = (xywh & 0xff0) >> 4;
	unsigned int x = (xywh & 0xf000) >> 12;
#ifdef DEBUG
	printf("%d %d %d %d\n", x, y, w, h);
#endif
	for (int r = 0; r <= h; ++r) {
		for (int c = 0; c <= w; ++c) {
			if (get_shape(block, r, c)) {
				board[wk_mul(y + r, COLS) + x + c] = 1;
			}
		}
	}
}

void print_score() {
	int c = 8;
	putch_at(c++,  -2, 'S');
	putch_at(c++, -2, 'C');
	putch_at(c++, -2, 'O');
	putch_at(c++, -2, 'R');
	putch_at(c++, -2, 'E');
	for (int i = 0; i < 5; ++i) {
		unsigned int mask = 0xF << (i * 4);
		unsigned int num = (score & mask) >> (i * 4);
		putch_at(12 - i, -1, num + '0');
	}
}

void draw_board() {
	for (int r = 0; r < ROWS; ++r) {
		for (int c = 0; c < COLS; ++c) {
			if (board[wk_mul(r , COLS) + c] == 1) {
				putch_at((c << 1) + 1, r, '[');
				putch_at((c << 1) + 2, r, ']');
			} else {
				putch_at((c << 1) + 1, r, ' ');
				putch_at((c << 1)+ 2, r, ' ');
			}
		}
	}	
	unsigned int xywh = current.xywh;
	unsigned int h = xywh & 0x3;
	unsigned int w = (xywh & 0xc) >> 2;
	unsigned int y = (xywh & 0xff0) >> 4;
	unsigned int x = (xywh & 0xf000) >> 12;
	for (int r = 0; r <= h; ++r) {
		for (int c = 0; c <= w; ++c) {
			if (get_shape(&current, r, c)) {
				putch_at(((c + x)  << 1) + 1, r + y, '[');
				putch_at(((c + x)  << 1) + 2, r + y, ']');
			}
		}
	}
	print_score();
}


void add_score(unsigned int delta) {
	score += delta;
	for (unsigned int i = 0, carry = 0; i < 32; i += 4) {
		unsigned int mask = 0xF << i;
		unsigned int num = (score & mask) >> i;
		num += carry;
		if (num >= 10) {
			carry = 1;
			num -= 10;
		} else {
			carry = 0;
		}
		score &= ~(mask);
		score |= (num << i);
	}
}

void check_clear() {
	unsigned int y = (current.xywh & 0xff0) >> 4;
	unsigned int h = current.xywh & 0x3;
	for (int r = y + h; r >= y; --r) {
		unsigned int count = 0;
		for (int c = 0; c < COLS; ++c) {
			if (board[wk_mul(r , COLS) + c]) ++count;
		}
		if (count == COLS) {
			add_score(1);
			for (int nr = r - 1; nr > 0; --nr) {
				for (int c = 0; c < COLS; ++c) {
					board[wk_mul(nr + 1, COLS) + c] = board[wk_mul(nr, COLS) + c];
				}
			}
			++r; ++y;
		}
	}
}

unsigned int rand() {
	static unsigned int seed = 990315;
	seed = (wk_mul(1103515245 , seed) + 12345) & 0x7FFFFFFF;
	return seed;
}

unsigned int rand_type() {
	unsigned int type = rand() & 0x7;
	while (type == 7) {
		type = rand() & 0x7;
	}
	return type;
}

void fall() {
	if (move(&current, 2) == 0) {
		fix_block(&current);
		check_clear();
		init_block(&current, rand_type(), 4, 0);
	}
}

#ifdef DEBUG
void print_screen() {
	for (int r = 0; r < SCREEN_ROWS; ++r) {
		for (int c = 0; c < SCREEN_COLS; ++c) {
			printf("%c", screen[wk_mul(r, SCREEN_COLS) + c]);
		}
		printf("\n");
	}
	printf("\n");
}
#endif

void on_input(unsigned int ch) {
	switch (ch) {
	case 's':
		fall();
		break;
	case 'a':
		move(&current, 0);
		break;
	case 'd':
		move(&current, 1);
		break;
	case 'j':
		rotate(&current, 0);
		break;
	case 'k':
		rotate(&current, 1);
		break;
	}
	draw_board();
#ifdef DEBUG
	print_screen();
#endif
}

void on_timer() {
	fall();
	draw_board();
}

void trap_handler(void *epc, unsigned int cause) {
	if (cause == 0x80000007) {
		on_timer();
	} else {
		unsigned int ch = *UART_RECV;
		*UART_SEND = ch;
		on_input(ch);
	}
}

void init() {
	clear_board();
	// Draw border
	for (int r = 0; r < ROWS; ++r) {
		putch_at(0, r, '|');
		putch_at(COLS << 1 | 1, r, '|');
	}
	for (int c = 0; c <= (2 << COLS | 1); ++c) {
		putch_at(c, ROWS, '-');
	}
	int c = 8;
	putch_at(c++, ROWS + 1, 'T');
	putch_at(c++, ROWS + 1, 'E');
	putch_at(c++, ROWS + 1, 'T');
	putch_at(c++, ROWS + 1, 'R');
	putch_at(c++, ROWS + 1, 'I');
	putch_at(c++, ROWS + 1, 'S');
	c = 6;
	putch_at(c++, ROWS + 3, 'H');
	putch_at(c++, ROWS + 3, 'o');
	putch_at(c++, ROWS + 3, 'w');
	putch_at(c++, ROWS + 3, 'a');
	putch_at(c++, ROWS + 3, 'r');
	putch_at(c++, ROWS + 3, 'd');
	c++;
	putch_at(c++, ROWS + 3, 'L');
	putch_at(c++, ROWS + 3, 'a');
	putch_at(c++, ROWS + 3, 'u');
	c = 9;
	putch_at(c++, ROWS + 4, '2');
	putch_at(c++, ROWS + 4, '0');
	putch_at(c++, ROWS + 4, '2');
	putch_at(c++, ROWS + 4, '1');
	init_block(&current, rand_type(), 4, 0);
	score = 0;
	draw_board();
}

void clear_screen() {
	int *vram = ((int *) VRAM_BASE);
	for (int i = 0; i < 600; ++i) vram[i] = 0x20202020;
}

extern void enable_interrupt();

int main() {
#ifdef DEBUG
	unsigned char b[ROWS * COLS] = {0};
	board = b;
	for (int i = 0; i < SCREEN_ROWS * SCREEN_COLS; ++i) screen[i] = '%';
	init();
#else
	board = (unsigned char *) 16384;
	clear_screen();
	init();
	*((unsigned int *) 4) = 0xDEADBEEF;
	enable_interrupt();
	*TIMER_ENABLED = 1;
	*TIMER_LIMIT = FALL_TIMER_LIMIT;
	for (;;);
#endif
#ifdef DEBUG
	on_input('a');
	on_input('a');
	on_input('s');
	on_input('s');
	on_input('s');
	for (int i = 21; i >= 0; --i) {
		on_timer();
	}
	on_input('d');
	on_input('d');
	on_input('d');
	on_input('d');
	for (int i = 21; i >= 0; --i) {
		on_timer();
	}
	on_input('d');
	on_input('d');
	on_input('d');
	on_input('d');
	on_input('d');
	for (int i = 21; i >= 0; --i) {
		on_timer();
		add_score(10);
	}
	print_score();
	print_screen();
	return 0;
#endif
}
