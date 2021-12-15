package riscv

import chisel3._
import chisel3.util._

class BCD2Segments extends Module {
  val io = IO(new Bundle {
    val bcd = Input(UInt(4.W))
    val segs = Output(UInt(7.W))
  })

  val bcd = io.bcd
  val segs = Wire(UInt(7.W))

  when(bcd === 0.U) {
    segs := 0x01.U
  }.elsewhen(bcd === 1.U) {
    segs := 0x3F.U
  }.elsewhen(bcd === 2.U) {
    segs := 0x12.U
  }.elsewhen(bcd === 3.U) {
    segs := 0x06.U
  }.elsewhen(bcd === 4.U) {
    segs := 0x4C.U
  }.elsewhen(bcd === 5.U) {
    segs := 0x24.U
  }.elsewhen(bcd === 6.U) {
    segs := 0x20.U
  }.elsewhen(bcd === 7.U) {
    segs := 0x0F.U
  }.elsewhen(bcd === 8.U) {
    segs := 0x00.U
  }.elsewhen(bcd === 9.U) {
    segs := 0x04.U
  }.elsewhen(bcd === 10.U) {
    segs := 0x0C.U
  }.elsewhen(bcd === 11.U) {
    segs := 0x60.U
  }.elsewhen(bcd === 12.U) {
    segs := 0x72.U
  }.elsewhen(bcd === 13.U) {
    segs := 0x42.U
  }.elsewhen(bcd === 14.U) {
    segs := 0x30.U
  }.elsewhen(bcd === 15.U) {
    segs := 0x3C.U
  }.otherwise {
    segs := 0xFF.U
  }

  io.segs := segs
}

