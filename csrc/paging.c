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

#include "memlayout.h"
#include "mmio.h"

#define PGSIZE 4096 // bytes per page
#define PGSHIFT 12  // bits of offset within a page

#define PGROUNDUP(sz)  (((sz)+PGSIZE-1) & ~(PGSIZE-1))
#define PGROUNDDOWN(a) (((a)) & ~(PGSIZE-1))

#define PTE_V (1L << 0) // valid
#define PTE_R (1L << 1)
#define PTE_W (1L << 2)
#define PTE_X (1L << 3)
#define PTE_U (1L << 4) // 1 -> user can access

// shift a physical address to the right place for a PTE.
#define PA2PTE(pa) ((((uint32)pa) >> 12) << 10)

#define PTE2PA(pte) (((pte) >> 10) << 12)

#define PTE_FLAGS(pte) ((pte) & 0x3FF)

// extract the two 10-bit page table indices from a virtual address.
#define PXMASK          0x3FF // 10 bits
#define PXSHIFT(level)  (PGSHIFT+(10*(level)))
#define PX(level, va) ((((uint32) (va)) >> PXSHIFT(level)) & PXMASK)

#define MAXVA (1L << (10 + 10 + 12))

#define SCREEN_COLS 80
#define SCREEN_ROWS 30
#define INT_TIMER_LIMIT 50000000

typedef unsigned int uint32;
typedef uint32 pte_t;
typedef uint32* pagetable_t;

extern void enable_paging();
extern void enable_interrupt();

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

char info[][16] = {
    "page fault      ",
    "start paging    ",
    "timer interrupt ",
    "out of mem      ",
};

void putch_at(int x, int y, unsigned char ch) {
	VRAM[wk_mul(y,SCREEN_COLS) + x] = ch;
}

void vputch_at(int x, int y, unsigned char ch) {
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
            pgtbl[PX(1,va)] = PA2PTE(t<<12) | PTE_V;
        }else{
            for(int i=0;i<16;i++){
                putch_at(50+i,0,info[3][i]);
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

void trap_handler(void *epc, unsigned int cause) {
    char* t = 0;
	if (cause == 10 || cause == 15 || cause == 13) {
        t=info[0];
        for(int i=0;i<16;i++){
            vputch_at(50+i,timercount,t[i]);
        }
        while(1);
	} else if(cause == 0x80000007){
        t=info[2];
        for(int i=0;i<16;i++){
            vputch_at(i,timercount,t[i]);
        }
        timercount += 1;
    }
    
}

void clear_screen() {
	int *vram = ((int *) VRAM_BASE);
	for (int i = 0; i < 600; ++i) vram[i] = 0x20202020;
}

int main(){
    for(int i=0;i<8;i++){
        pm[i] = 0;
    }
    timercount = 0;
    // clear_screen();
    for(int i=0;i<24;i++){
        putch_at(20+i,0,"printout before paging"[i]);
    }
    kvminit();
    enable_paging();
    // int *vram = ((int *) VA_VRAM_BASE);
	// for (int i = 0; i < 600; ++i) vram[i] = 0x31313131;
    enable_interrupt();
    *VA_TIMER_ENABLED = 1;
    *VA_TIMER_LIMIT = INT_TIMER_LIMIT;
    // *VA_TIMER_LIMIT = 0x10000; //缩小时钟周期，方便debug
    for(;;);
}