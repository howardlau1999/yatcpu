// # Copyright 2022 hrpccs
// #
// # Licensed under the Apache License, Version 2.0 (the "License");
// # you may not use this file except in compliance with the License.
// # You may obtain a copy of the License at
// #
// #     http://www.apache.org/licenses/LICENSE-2.0
// #
// # Unless required by applicable law or agreed to in writing, software
// # distributed under the License is distributed on an "AS IS" BASIS,
// # WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// # See the License for the specific language governing permissions and
// # limitations under the License.

#include "mm.h"
#include "mmio.h"



#define SCREEN_COLS 80
#define SCREEN_ROWS 30
#define INT_TIMER_LIMIT 50000000
#define PAGEDIR_BASE 0x5000

typedef unsigned int uint32;
typedef uint32 pte_t;
typedef uint32* pagetable_t;

extern void enable_paging();
extern void enable_interrupt();
extern int get_mtval();

int wk_mul(int a, int b) {
  int r = 0;
  for (; b; a <<= 1, b >>= 1)
    if (b & 1)
      r += a;
  return r;
}

void memoryset(unsigned char *dest,unsigned char num, unsigned int size){
    for(unsigned int i=0;i<size;i++){
        dest[i]=num;
    }
}

uint32 pm[8]; //板子上有32kb内存，八个页
uint32 timercount=0;
void (*putch_at)(int,int,unsigned char);

void __putch_at(int x, int y, unsigned char ch) {
	VRAM[wk_mul(y,SCREEN_COLS) + x] = ch;
}

void __vputch_at(int x, int y, unsigned char ch) {
	VA_VRAM[wk_mul(y,SCREEN_COLS) + x] = ch;
}

int alloc(){
    int index = 0;
    int max = 8;
    while(index < max){
        if(pm[index] == 0){
            pm[index] = 1;
            break;
        }
        index++;
    }
    return index == max ? -1 : index;
}

void map(pagetable_t pgtbl,uint32 va,uint32 pa,int perm){
    int t;
    if((pgtbl[PX(1,va)] & PTE_V )!= PTE_V){ //前缀不存在
        t=alloc(); //申请一个页给前缀页表
        if(t>=0){
            int* taddr = (int*)(t<<12);
            for(int i=0;i<PGSIZE>>2;i++){
                taddr[i]=0;
            }
            pgtbl[PX(1,va)] = PA2PTE(t<<12) | PTE_V;
        }else{
            while(1);
            for(int i=0;i<14;i++){
                putch_at(i,22,"out of memory!"[i]);
            }
            while(1);
        }
    }
    pagetable_t n = (void*)PTE2PA(pgtbl[PX(1,va)]);
    n[PX(0,va)] = PA2PTE(pa) | perm | PTE_V;
}


void kvminit(){
    pagetable_t pgtbl = (void*)PAGEDIR_BASE;
    // memoryset(pgtbl,0,PGSIZE); //后面需要读这个内存，所以先初始化
    for(int i=0;i<PGSIZE>>2;i++){
        pgtbl[i]=0;
    }

    pm[PAGEDIR_BASE >> 12] = 1;
    pm[0]=1;
    pm[1]=1;
    pm[2]=1;
    pm[3]=1;
    pm[4]=1;
    //create pte mmap for text
    pgtbl[1023] =  PA2PTE(PAGEDIR_BASE) | PTE_R | PTE_W;
    map(pgtbl,PAGEDIR_BASE,PAGEDIR_BASE,PTE_R | PTE_W);
    map(pgtbl,0x0,0x0, PTE_W | PTE_R ); //kernel stack
    map(pgtbl,0x1000,0x1000, PTE_W | PTE_R | PTE_X ); //
    map(pgtbl,0x2000,0x2000, PTE_W | PTE_R | PTE_X ); //
    map(pgtbl,0x3000,0x3000, PTE_W | PTE_R | PTE_X ); //
    map(pgtbl,0x4000,0x4000, PTE_W | PTE_R | PTE_X ); //
    map(pgtbl,VA_VRAM_BASE,VRAM_BASE, PTE_W | PTE_R ); //
    map(pgtbl,VA_VRAM_BASE + PGSIZE,VRAM_BASE + PGSIZE, PTE_W | PTE_R);
    map(pgtbl,VA_UART_BASE,UART_BASE, PTE_W | PTE_R ); //
    map(pgtbl,VA_TIMER_BASE,TIMER_BASE, PTE_W | PTE_R ); //
}

void clear_screen() {
	int *vram = ((int *) VRAM_BASE);
	for (int i = 0; i < 600; ++i) vram[i] = 0x20202020;
}

void trap_handler(void *epc, unsigned int cause) {
	if (cause == 10 || cause == 15 || cause == 13) {
        for(int i=0;i<39;i++){
            putch_at(i,4,"A not handled page fault caused by : 0x"[i]);
        }
        int mtval = get_mtval();
        char ch;
        for (int i = 0; i < 8; ++i) {
            unsigned int mask = 0xF << (i * 4);
            unsigned int num = (mtval & mask) >> (i * 4);
            if(num >= 10){
                ch = num - 10 + 'A';
            }else{
                ch = num + '0';
            }
		    putch_at(39+7-i,4,ch);
	    }
        while(1); //目前还没有处理page-fault，因为还没有建立完整的映射。
	} else if(cause == 0x80000007){
        for(int i=0;i<16;i++){
            putch_at(i,3,"timer count : 0x"[i]);
        }
        char ch;
        for (int i = 0; i < 8; ++i) {
            unsigned int mask = 0xF << (i * 4);
            unsigned int num = (timercount & mask) >> (i * 4);
            if(num >= 10){
                ch = num - 10 + 'A';
            }else{
                ch = num + '0';
            }
		    putch_at(16+7-i,3,ch);
	    }
        timercount += 1;

    }
}

int main(){
    putch_at = __putch_at;
    for(int i=0;i<8;i++){
        pm[i] = 0;
    }
    timercount = 0;
    clear_screen();
    for(int i=0;i<19;i++){
        putch_at(i,0,"print before paging"[i]);
    }
    kvminit();
    putch_at = __vputch_at;
    enable_paging();
    for(int i=0;i<18;i++){
        putch_at(i,1,"print after paging"[i]);
    }
    enable_interrupt();
    *VA_TIMER_ENABLED = 1;
    *VA_TIMER_LIMIT = INT_TIMER_LIMIT;

    while(1){
        if(timercount == 0x20){
            *(int*)0x11415 = 1130;
        }
    }
    for(;;);
}