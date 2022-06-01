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

#define VRAM_BASE 0x20000000
#define VRAM ((volatile unsigned char *) VRAM_BASE)
#define TIMER_BASE 0x80000000
#define TIMER_LIMIT ((volatile unsigned int *) (TIMER_BASE + 4))
#define TIMER_ENABLED ((volatile unsigned int *) (TIMER_BASE + 8))
#define UART_BASE 0x40000000
#define UART_BAUDRATE ((volatile unsigned int *) (UART_BASE + 4))
#define UART_RECV ((volatile unsigned int *) (UART_BASE + 12))
#define UART_SEND ((volatile unsigned int *) (UART_BASE + 16))
//remap the mmio to reduce memory usage of page table
#define VA_VRAM_BASE 0x00100000
#define VA_VRAM ((volatile unsigned char *) VA_VRAM_BASE)
#define VA_TIMER_BASE 0x00200000
#define VA_TIMER_LIMIT ((volatile unsigned int *) (VA_TIMER_BASE + 4))
#define VA_TIMER_ENABLED ((volatile unsigned int *) (VA_TIMER_BASE + 8))
#define VA_UART_BASE 0x00300000
#define VA_UART_BAUDRATE ((volatile unsigned int *) (VA_UART_BASE + 4))
#define VA_UART_RECV ((volatile unsigned int *) (VA_UART_BASE + 12))
#define VA_UART_SEND ((volatile unsigned int *) (VA_UART_BASE + 16))