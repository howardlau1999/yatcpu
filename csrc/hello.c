void write_char(int row, int col, char ch) {
	char *vram = (char *) 1024;
	vram[row * 80 + col] = ch;
}

void clear_screen() {
	int *vram = ((int *) 1024);
	for (int i = 0; i < 600; ++i) vram[i] = 0x20202020;
}

int main() {
	clear_screen();
	for (int row = 0; row < 30; ++row) {
		int col = 0;
		write_char(row, col++, 'H');
		write_char(row, col++, 'e');
		write_char(row, col++, 'l');
		write_char(row, col++, 'l');
		write_char(row, col++, 'o');
		write_char(row, col++, ',');
		write_char(row, col++, ' ');
		write_char(row, col++, 'w');
		write_char(row, col++, 'o');
		write_char(row, col++, 'r');
		write_char(row, col++, 'l');
		write_char(row, col++, 'd');
		write_char(row, col++, '!');
	}
	*((int *) 0x4) = 0xDEADBEEF;
}

