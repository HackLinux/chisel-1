package main

import Chisel._
import logicanalyser.LogicAnalyser

class BlockAA extends Module {
  val io = new Bundle {
    val in0 = UInt(INPUT, 4)
    val out0 = UInt(OUTPUT, 4)
  }
  val s0 = UInt(width = 4)
  val s1 = UInt(width = 4)
  val s2 = UInt(width = 4)
  s0 := io.in0
  s1 := s0
  s2 := s1
  this.io.out0 := s2
}

class BlockA extends Module {
  val io = new Bundle {
    val in0 = UInt(INPUT, 4)
    val out0 = UInt(OUTPUT, 4)
  }
  val blockAA = Module(new BlockAA())
  blockAA.io.in0 := io.in0
  this.io.out0 := blockAA.io.out0
}

class Test extends Module {
  val io = new Bundle {
    val in0 = UInt(INPUT, 4)
    val out0 = UInt(OUTPUT, 4)
  }
  val blockA = Module(new BlockA())
  blockA.io.in0 := io.in0
  this.io.out0 := blockA.io.out0
  
  val p = new LogicAnalyser.Parameter
  p.addData(blockA.blockAA.s0)
  p.addData(blockA.blockAA.s1)
  p.addData(blockA.blockAA.s2)
  
  val logicAnalyser = Module(new LogicAnalyser(p))
  logicAnalyser.connect()
}