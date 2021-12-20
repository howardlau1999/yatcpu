# Quick_Start

本项目主要搭建在 Linux 环境或 WSL （Windows Subsystem for Linux）环境中。~~Windows 环境和 MacOS 理论上也可以，但需要读者自行摸索。~~

这里假设你使用的 Linux 或 WSL 系统是 Debian 11。对于使用其他 Linux 系统的同学，操作是类似的，相信你有足够的能力参考下面的指令搭建环境。

## 安装 WSL

对于使用 Windows 系统且特别萌新的同学，可以参照[这篇博客](https://wu-kan.cn/2018/12/14/Windows-Subsystem-for-Linux/)，配置一个 WSL 环境。其他同学可以跳过这一节。

## 安装必要依赖

```bash
sudo apt install git  # 用于将本项目 clone 下来
sudo apt install gcc-riscv64-unknown-elf # 用于生成 risc-v 的测试指令
sudo apt install make # build-essential
sudo aot install curl # 下载工具
sudo apt install scala # 本项目的语言
```

## 安装包管理器 SBT

[sbt](https://packages.debian.org/sid/sbt)目前仍然在 debian 的[不稳定](https://www.debian.org/releases/sid/)版本中。对于不希望使用 sid 系统的同学，可以按照[文档](https://www.scala-sbt.org/1.x/docs/zh-cn/Installing-sbt-on-Linux.html#Ubuntu%E5%92%8C%E5%85%B6%E4%BB%96%E5%9F%BA%E4%BA%8EDebian%E7%9A%84%E5%8F%91%E8%A1%8C%E7%89%88)的指示下载安装。

```bash
echo "deb https://repo.scala-sbt.org/scalasbt/debian all main" | sudo tee /etc/apt/sources.list.d/sbt.list
echo "deb https://repo.scala-sbt.org/scalasbt/debian /" | sudo tee /etc/apt/sources.list.d/sbt_old.list
curl -sL "https://keyserver.ubuntu.com/pks/lookup?op=get&search=0x2EE0EA64E40A89B84B2DF73499E82A75642AC823" | sudo apt-key add
sudo apt update
sudo apt install sbt
```

## 获取本项目

```bash
git clone git@github.com:howardlau1999/chisel-riscv.git
cd chisel-riscv
```

## 进行一个测试

完成上述过程后，你已经配置完成本地开发必要的环境了。执行下述指令可以模拟运行本项目的测试。

```bash
sbt test
```

首次运行时需要联网，该指令会自动下载必要的组件。如果成功执行，你会看到类似这样的输出。

```bash
[success] Total time: 385 s (06:25), completed Dec 15, 2021, 8:45:25 PM
```

恭喜你，已经配置完毕！

## 烧板（WIP）

- [参考文档](https://china.xilinx.com/support/documentation/sw_manuals/xilinx2020_1/c_ug973-vivado-release-notes-install-license.pdf)

我们在中山大学校园网内提供了 vivado 的镜像。

```bash
sudo apt install libtinfo5 # 需要一个这个，否则启动报错
curl -O https://mirrors.matrix.moe/software/Xilinx_Unified_2020.1_0602_1208.tar.gz
# 也可以在 Xilinx 的官网下载
# <https://china.xilinx.com/support/download/index.html/content/xilinx/zh/downloadNav/vivado-design-tools/archive.html>
tar -zxf Xilinx_Unified_2020.1_0602_1208.tar.gz
cd Xilinx_Unified_2020.1_0602_1208
./xsetup -b ConfigGen
```

根据[手册](https://www.stepfpga.com/doc/_media/basys3_ss.pdf)可得知，学校提供的 Basys3 实验板型号为 XC7A35T，只需要安装 `Vivado HL WebPACK` 即可使用。因此先输入 `2` 再输入 `1`，会产生一个默认配置文件 `~/.Xilinx/install_config.txt`。

```bash
./xsetup -a XilinxEULA,3rdPartyEULA,WebTalkTerms -b Install -c ~/.Xilinx/install_config.txt -l ~/Xilinx
```

```bash
cd ~/Xilinx/Vivado/2020.1/data/xicom/cable_drivers/lin64/install_script/install_drivers
sudo ./install_drivers
source ~/Xilinx/Vivado/2020.1/settings64.sh
```
