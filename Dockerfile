FROM sbtscala/scala-sbt:17.0.2_1.7.1_2.12.16
RUN apt-get update && apt-get install -y curl lsb-release wget software-properties-common gnupg
RUN curl -fsSL https://apt.llvm.org/llvm.sh | bash -s -- 15 all
RUN apt-get install -y verilator make cmake g++
RUN apt-get clean autoclean && apt-get autoremove --yes && rm -rf /var/lib/{apt,dpkg,cache,log}/
ADD . /root/yatcpu
WORKDIR /root/yatcpu
RUN sbt test
