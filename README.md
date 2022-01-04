# YatCPU

YatCPU(**Y**et **a**nother **t**oy CPU) is a simple three-stage pipeline RISC-V implementation written in Chisel 3 HDL with support for AXI4-Lite, featuring its painless hands-on experience and VGA output for fun.

You can play Tetris on your own CPU!

![tetris-demo](https://liuhaohua.com/service-computing-blog/assets/images/tetris.gif)

Heavily inspired by [tinyriscv](https://gitee.com/liangkangnan/tinyriscv), this project is still in its very early stage and under active development. We are looking forward to your
feedback and contributions!

## Development Plans

- [ ] L1 I-cache and D-cache
- [ ] Branch predictor
- [ ] Run [Yat-sen OS RISC-V](https://github.com/NelsonCheung-cn/yatsenos-riscv)
- [ ] RV32M
- [ ] Pass RISC-V compliance test
- [ ] Run CoreMark benchmarks

## Prerequisites

### You

- Basic knowledge of digital logic and design principles
- Basic knowledge of C/C++
- (Optional) A FPGA development board

### Your computer

- Windows 10 or higher / Linux (Debian, Ubuntu, WSL1/2 or other distros)
    - We have tested on Windows 10 and WSL Debian 11
    - macOS is **NOT** supported
- Java 8 or higher
- Scala 2.12 (not 3)
- sbt
- (Optional) [Latest Verilator](https://veripool.org/guide/latest/install.html)
- (Optional) Vivado 2020.1 or higher

## Getting Started

Please refer to [YatCPU-docs](https://github.com/sysu-scc/YatCPU-docs) for more documentation.
