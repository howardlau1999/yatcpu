import sbt.Keys.libraryDependencies
// See README.md for license details.

ThisBuild / scalaVersion     := "2.13.8"
ThisBuild / version          := "0.1.0"
ThisBuild / organization     := "io.github.howardlau1999"

val chiselVersion = "3.5.1"

resolvers ++= Seq(
  MavenRepository(
      "sonatype-s01-snapshots",
      "https://s01.oss.sonatype.org/content/repositories/snapshots" 
  )
)

lazy val root = (project in file("."))
  .settings(
    name := "chisel-riscv",
    libraryDependencies ++= Seq(
      "edu.berkeley.cs" %% "chisel3" % chiselVersion,
      "io.github.howardlau1999" %% "chiseltest" % "0.6-SNAPSHOT" % "test" changing(),
    ),
    scalacOptions ++= Seq(
      "-language:reflectiveCalls",
      "-deprecation",
      "-feature",
      "-Xcheckinit",
      "-P:chiselplugin:genBundleElements",
    ),
    addCompilerPlugin("edu.berkeley.cs" % "chisel3-plugin" % chiselVersion cross CrossVersion.full),
  )
