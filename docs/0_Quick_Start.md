# Quick_Start

## 开发环境

本项目主要搭建在 Linux 环境或 WSL （Windows Subsystem for Linux）环境中。~~Windows 环境和 MacOS 理论上也可以，但需要读者自行摸索。~~

这里假设你使用的 Linux 或 WSL 系统是 Debian 11。对于使用其他 Linux 系统的同学，操作是类似的，相信你有足够的能力参考下面的指令搭建环境。

### 必要工具

```bash
sudo apt install git  # 用于将本项目 clone 下来
sudo apt install gcc-riscv64-unknown-elf # 用于生成 risc-v 的测试指令
sudo apt install make # build-essential
sudo apt install curl gnupg2 # 下载工具
sudo apt install scala # 本项目的语言
```

### 包管理器 SBT

[sbt](https://packages.debian.org/sid/sbt)目前仍然在 debian 的[不稳定](https://www.debian.org/releases/sid/)版本中。对于不希望使用 sid 系统的同学，可以按照[文档](https://www.scala-sbt.org/1.x/docs/zh-cn/Installing-sbt-on-Linux.html#Ubuntu%E5%92%8C%E5%85%B6%E4%BB%96%E5%9F%BA%E4%BA%8EDebian%E7%9A%84%E5%8F%91%E8%A1%8C%E7%89%88)的指示下载安装。

```bash
echo "deb https://repo.scala-sbt.org/scalasbt/debian all main" | sudo tee /etc/apt/sources.list.d/sbt.list
echo "deb https://repo.scala-sbt.org/scalasbt/debian /" | sudo tee /etc/apt/sources.list.d/sbt_old.list
curl -sL "https://keyserver.ubuntu.com/pks/lookup?op=get&search=0x2EE0EA64E40A89B84B2DF73499E82A75642AC823" | sudo apt-key add
sudo apt update
sudo apt install sbt
```

### Vivado

中山大学组成原理实验课使用的 [Basys3](https://www.stepfpga.com/doc/_media/basys3_ss.pdf) 实验板型号为 XC7A35T，只需要安装 `Vivado HL WebPACK` 即可使用。中山大学校园网内下载 Vivado 安装包，可以使用我们提供的的镜像。

```bash
sudo apt install libtinfo5 # 需要一个这个，否则启动报错
curl -O https://mirrors.matrix.moe/software/Xilinx_Unified_2020.1_0602_1208.tar.gz
# 也可以在 Xilinx 的官网下载
# <https://china.xilinx.com/support/download/index.html/content/xilinx/zh/downloadNav/vivado-design-tools/archive.html>
tar -zxf Xilinx_Unified_2020.1_0602_1208.tar.gz
cd Xilinx_Unified_2020.1_0602_1208
./xsetup -b ConfigGen
```

先输入 `2` 再输入 `1`，这时会选中 `Vivado HL WebPACK`。同时会产生一个默认配置文件 `~/.Xilinx/install_config.txt`。

随后再次执行下述指令，安装`Vivado HL WebPACK`。

```bash
./xsetup -a XilinxEULA,3rdPartyEULA,WebTalkTerms -b Install -c ~/.Xilinx/install_config.txt -l ~/Xilinx
```

其中`~/Xilinx`是安装目录，可以自行定义。

## 测试

执行下述指令可以模拟运行本项目的测试，首次运行时需要联网自动下载必要的组件。

```bash
# git clone git@github.com:howardlau1999/chisel-riscv.git
# cd chisel-riscv
sbt test
```

如果成功执行，你会看到类似这样的输出。

```bash
[success] Total time: 385 s (06:25), completed Dec 15, 2021, 8:45:25 PM
```

输入如下指令，可以编译生成 verilog 代码 `verilog/Top.v`。

```bash
sbt run
```

## 验证

### 生成二进制文件

假设你的 Vivado 安装目录是 `~/Xilinx`（其他目录自行修改），执行下述指令，可以根据 `verilog/Top.v` 生成二进制文件 `vivado/riscv-basys3/riscv-basys3.runs/impl_1/Top.bit`。

```bash
cd vivado
~/Xilinx/Vivado/2020.1/bin/vivado -mode batch -source ./generate_bitstream.tcl
```

### 烧板

由于 WSL 下暂时不可以使用 USB 设备，因此不可以直接烧板。解决方案有两个。

#### 方案一：使用 USB 方式烧板（WIP）

需要一个 Fat32 格式的 U 盘。

#### 方案二：在 Windows 下烧板

由于 WSL 下可以调用 powershell，我们可以回到 Windows 下烧板。Windows 下可以只装 Vivado_Lab，和完整版的 Vivado 相比只有烧板功能，精简很多。

```bash
# 假设你在 vivado 目录下，同时 Windows 下 Vivado_Lab 安装在 `C:\Xilinx`
powershell.exe 'C:\Xilinx\Vivado_Lab\2020.1\bin\vivado_lab.bat -mode batch -source .\program_device.tcl'
```

中山大学校园网内下载 Vivado_Lab 安装包，也可以使用我们提供的的镜像。
