FROM sbtscala/scala-sbt:17.0.2_1.7.1_2.12.16
ARG CLANG_VERSION=15
RUN apt-get update && apt-get install -y curl lsb-release wget software-properties-common gnupg
RUN curl -fsSL https://apt.llvm.org/llvm.sh | bash -s -- $CLANG_VERSION all
RUN curl -fsSL https://gist.githubusercontent.com/howardlau1999/7ea2cffedc0491c46fa14e1f2355a9a8/raw/7886c0344e8f010a9d515a3de20983e604ae5a0a/update-alternatives-clang.sh | bash -s -- $CLANG_VERSION 100
RUN apt-get install -y verilator make cmake g++
RUN apt-get clean autoclean && apt-get autoremove --yes && rm -rf /var/lib/{apt,dpkg,cache,log}/
ADD . /root/yatcpu
WORKDIR /root/yatcpu
RUN sbt test
