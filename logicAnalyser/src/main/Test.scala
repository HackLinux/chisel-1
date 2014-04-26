package main

import Chisel._
import logicanalyser.LogicAnalyser
import packet.Fragment

class BlockAA extends Module {
  val io = new Bundle {
    val in0 = UInt(INPUT, 16)
    val out0 = UInt(OUTPUT, 16)
  }
  val s0 = UInt(width = 16)
  val s1 = UInt(width = 16)
  val s2 = UInt(width = 16)
  s0 := io.in0
  s1 := s0 + UInt(1)
  s2 := s1 + UInt(1)
  this.io.out0 := s2
}

class BlockA extends Module {
  val io = new Bundle {
    val in0 = UInt(INPUT, 16)
    val out0 = UInt(OUTPUT, 16)
  }
  val blockAA = Module(new BlockAA())
  blockAA.io.in0 := io.in0
  this.io.out0 := blockAA.io.out0
}

class Test extends Module {
  val io = new Bundle {
    val in0 = UInt(INPUT, 16)
    val out0 = UInt(OUTPUT, 16)
    
    val packetSlave = Decoupled(Fragment(Bits(width = 8))).flip
    val packetMaster = Decoupled(Fragment(Bits(width = 8)))
  }
  val counter = RegInit(UInt(0,16))
  counter := counter + UInt(1)
  
  val blockA = Module(new BlockA())
  blockA.io.in0 := counter//io.in0
  this.io.out0 := blockA.io.out0
  
  val p = new LogicAnalyser.Parameter
  p.addData(blockA.blockAA.s0)
  p.addData(blockA.blockAA.s1)
  p.addData(blockA.blockAA.s2)
  
  val logicAnalyser = Module(new LogicAnalyser(p))
  logicAnalyser.io.packetSlave <> io.packetSlave
  logicAnalyser.io.packetMaster <> io.packetMaster

  
  logicAnalyser.connect()
}