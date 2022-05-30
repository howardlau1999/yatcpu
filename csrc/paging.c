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

typedef unsigned int uint32;
typedef uint32 pte_t;
typedef uint32* pagetable_t;

extern void enable_paging();
extern void enable_interrupt();

uint32 map(pagetable_t pgtbl,uint32 va,uint32 pa,int perm){
    uint32 vpn0 = PX(0,va);
    pagetable_t pgtbl0 = (void *)0xC000;
    ((uint32*)0xC000)[vpn0] = PA2PTE(pa) | perm;
    return ((uint32*)0xC000)[vpn0];//return val will be put in a0, will help debug
}

void kvminit(){
    pagetable_t pgtbl = (void*)PAGEDIR_BASE;
    //create pte mmap for text and 

    pgtbl[PX(1,0xC000)]=PA2PTE(0xC000) | PTE_V; //忽略高2位，次10位为0的页表映射到
    map(pgtbl,0x0000,0x0000,PTE_X | PTE_W | PTE_R | PTE_V);
    map(pgtbl,0x1000,0x1000,PTE_X | PTE_W | PTE_R | PTE_V);
    map(pgtbl,0x2000,0x2000,PTE_W | PTE_R | PTE_V);
    map(pgtbl,0x3000,0x3000,PTE_W | PTE_R | PTE_V);
    map(pgtbl,0x4000,0x8000,PTE_W | PTE_R | PTE_V);
}



void trap_handler(void *epc, unsigned int cause) {
	if (cause == 10 || cause == 15 || cause == 13) {
        while(1);
	}
}


void vminit(){
    kvminit();
    enable_paging();
}

int main(){
    enable_interrupt();
    vminit();
    int temp = 0;
    *(uint32*)(0x4013) = 1; //Write 0x1c013
    temp = *(uint32*)(0x4013); //Read 0x1c013
    temp = *(uint32*)(0x8123); //invalid read
    return 0;
}