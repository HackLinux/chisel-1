package logicanalyser

import Chisel._
import packet.Fragment

object LALogger {
  class Config(p: LogicAnalyser.Parameter) extends Bundle {
    val samplesLeftAfterTrigger = UInt(width = p.memAddressWidth)
  }
}

class LALogger(p: LogicAnalyser.Parameter, probesBits: Bits) extends Module {
  val io = new Bundle {
    val config = new LALogger.Config(p).asInput

    val trigger = Bool(INPUT)
    val probe = probesBits.clone.asInput

    val log = Decoupled(Fragment(probesBits.clone))
  }

  val mem = Mem(Bits(width = probesBits.getWidth), 1 << p.memAddressWidth,seqRead = true)
  val memWriteAddress = RegInit(UInt(0, p.memAddressWidth))
  val memReadAddress = RegInit(UInt(0, p.memAddressWidth))

  val sWaitTrigger :: sSample :: sPush :: Nil = Enum(UInt(), 3)

  val state = Reg(init = sWaitTrigger)

  val sampleEnable = Bool()
  val sSampleCounter = Reg(UInt(width = p.memAddressWidth))
  val sPushCounter = Reg(UInt(width = p.memAddressWidth))

  io.log.valid := Bool(false)
  sampleEnable := Bool(false)
  io.log.bits.last := Bool(false)

  when(state === sWaitTrigger) {
    sampleEnable := Bool(true)
    sSampleCounter := UInt(io.config.samplesLeftAfterTrigger)
    when(io.trigger) {
      state := sSample
      memReadAddress := memWriteAddress + io.config.samplesLeftAfterTrigger + UInt(2)
    }
  }
  when(state === sSample) {
    sampleEnable := Bool(true)
    sSampleCounter := sSampleCounter - UInt(1)

    when(sSampleCounter === UInt(0)) {
      state := sPush
      sPushCounter := UInt(0)
    }
  }
  when(state === sPush) {
    io.log.valid := Bool(true)
    when(io.log.ready) {
      memReadAddress := memReadAddress + UInt(1)
      sPushCounter := sPushCounter + UInt(1)
    }
    when(sPushCounter === UInt((1 << sPushCounter.getWidth)-1)) {
      io.log.bits.last := Bool(true)
      when(io.log.ready) {
        state := sWaitTrigger
      }
    }
  }

  when(sampleEnable) {
    mem(memWriteAddress) := io.probe
    memWriteAddress := memWriteAddress + UInt(1)
  }
  io.log.bits.fragment := mem(memReadAddress)
}