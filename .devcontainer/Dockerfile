FROM sbtscala/scala-sbt:eclipse-temurin-jammy-19.0.1_10_1.8.2_2.13.10
ARG CLANG_VERSION=15
RUN apt-get update && apt-get install -y device-tree-compiler curl python3-pip lsb-release wget software-properties-common gnupg clang-$CLANG_VERSION lldb-$CLANG_VERSION lld-$CLANG_VERSION clangd-$CLANG_VERSION clang-tidy-$CLANG_VERSION clang-format-$CLANG_VERSION clang-tools-$CLANG_VERSION llvm-$CLANG_VERSION-dev lld-$CLANG_VERSION lldb-$CLANG_VERSION llvm-$CLANG_VERSION-tools libomp-$CLANG_VERSION-dev libc++-$CLANG_VERSION-dev libc++abi-$CLANG_VERSION-dev libclang-common-$CLANG_VERSION-dev libclang-$CLANG_VERSION-dev libclang-cpp$CLANG_VERSION-dev libunwind-$CLANG_VERSION-dev
RUN curl -fsSL https://gist.githubusercontent.com/howardlau1999/7ea2cffedc0491c46fa14e1f2355a9a8/raw/7886c0344e8f010a9d515a3de20983e604ae5a0a/update-alternatives-clang.sh | bash -s -- $CLANG_VERSION 100
RUN apt-get install -y verilator make cmake g++
RUN pip3 install git+https://github.com/riscv/riscof.git
RUN git clone --depth 1 https://github.com/riscv-software-src/riscv-isa-sim.git && cd riscv-isa-sim && mkdir build && cd build && ../configure --prefix=/usr && make -j$(nproc) && make install
RUN rm -rf riscv-isa-sim