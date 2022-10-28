# YatCPU

简体中文 | [English](README.md)

YatCPU(**Y**et **a**nother **t**oy CPU) 是一个使用 Chisel 3 硬件设计语言开发的简单的三级流水线 RISC-V CPU，支持 AXI4-Lite。上手简单，具有 VGA 输出功能（目前支持 Basys 3 开发板）。

你可以在自己开发的 CPU 上运行自己开发的俄罗斯方块！

![tetris-demo](https://howardlau.me/wp-content/uploads/2022/06/tetris.gif)

你还可以在自己开发的 CPU 上运行红白机模拟器！

![litenes-demo](https://howardlau.me/wp-content/uploads/2022/06/litenes-scaled.jpg)

本项目深受 [tinyriscv](https://gitee.com/liangkangnan/tinyriscv) 项目的启发, 目前还处于非常初期的阶段。我们正在努力改进，并且希望得到您的反馈和贡献！

## 开发计划

- [ ] L1 指令缓存和数据缓存
- [ ] 分支预测器
- [ ] 运行 [逸仙 OS RISC-V](https://github.com/NelsonCheung-cn/yatsenos-riscv)
- [ ] 支持 RV32M 扩展
- [x] 通过 RISC-V 合规性测试 
- [x] 运行 CoreMark 性能测试 

## 前置要求

### 你需要……

- 掌握基本的数字电路与逻辑的设计与分析方法
- 掌握基本的 C 或 C++ 编程
- （可选）一个 FPGA 开发板

### 你的电脑需要……

- Windows 10 及以上 / Linux (Debian, Ubuntu, WSL1/2 或其他系统) / macOS
    - 我们已经在 Windows 10 和 WSL Debian 11 上测试过
    - macOS 可以运行软件仿真，但无法烧写 FPGA 开发板
- Java 8 及以上
- Scala 2.12 (不是 3)
- sbt
- （可选） [最新版 Verilator](https://veripool.org/guide/latest/install.html)
- （可选） Vivado 2020.1 及以上

## 快速入门

请参考 [YatCPU 文档](https://yatcpu.sysu.tech)。
