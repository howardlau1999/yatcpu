#!/bin/sh
rm -rf build
cmake -DCMAKE_TOOLCHAIN_FILE=toolchain.cmake -B build . && cmake --build build --parallel `nproc`
