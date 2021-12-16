int main() {
	for (int row = 0; row < 30; ++row) {
		char *vram = ((char *) 1024) + row * 80;
		(*(vram++)) = 'H';
		(*(vram++)) = 'e';
		(*(vram++)) = 'l';
		(*(vram++)) = 'l';
		(*(vram++)) = 'o';
		(*(vram++)) = ',';
		(*(vram++)) = ' ';
		(*(vram++)) = 'w';
		(*(vram++)) = 'o';
		(*(vram++)) = 'r';
		(*(vram++)) = 'l';
		(*(vram++)) = 'd';
		(*(vram++)) = '!';
	}
	*((int *) 0xD) = 0xDEADDEAD;
}

