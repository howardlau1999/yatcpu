// Copyright 2022 Howard Lau
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

package riscv

import chiseltest.{VerilatorBackendAnnotation, WriteVcdAnnotation}
import firrtl.AnnotationSeq

import java.nio.file.{Files, Paths}

object VerilatorEnabler {
  val annos: AnnotationSeq = if (sys.env.contains("Path")) {
    if (sys.env.getOrElse("Path", "").split(";").exists(path => {
      Files.exists(Paths.get(path, "verilator"))
    })) {
      Seq(VerilatorBackendAnnotation)
    } else {
      Seq()
    }
  } else {
    if (sys.env.getOrElse("PATH", "").split(":").exists(path => {
      Files.exists(Paths.get(path, "verilator"))
    })) {
      Seq(VerilatorBackendAnnotation)
    } else {
      Seq()
    }
  }
}

object WriteVcdEnabler {
  val annos: AnnotationSeq = if (sys.env.contains("WRITE_VCD")) {
    Seq(WriteVcdAnnotation)
  } else {
    Seq()
  }
}

object TestAnnotations {
  val annos = VerilatorEnabler.annos ++ WriteVcdEnabler.annos
}
