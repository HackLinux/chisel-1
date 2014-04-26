package logicanalyser

import Chisel._
import scala.collection.mutable.MutableList
import chiselext._

object LogicAnalyser {
  var UUID = 0
  def getUUID: Int = {
    val temp = UUID
    UUID = UUID + 1
    return temp
  }

  class Parameter {
    var memAddressWidth = 8

    val dataList = new MutableList[Data]
    def addData(data: Data) {
      dataList += data
    }
  }

  class Config(p : Parameter) extends Bundle {
	  val logger = new LALogger.Config(p)
  }

  def extractData(data: Data, to: Data, name: String) {
    val dataptr = extractData(data, to.component, name)
    wirePin(to, dataptr)
  }
  def extractData(data: Data, to: Module, name: String): Data = {
    var modulePtr: Module = null
    var pinPtr: Data = null
    var dataPtr: Data = data

    val riseModules = new MutableList[Module]
    modulePtr = to
    do {
      riseModules += modulePtr
      modulePtr = modulePtr.parent
    } while (modulePtr != null)

    modulePtr = data.component
    while (!riseModules.contains(modulePtr)) {
      pinPtr = data.clone
      addPin(modulePtr, pinPtr.asOutput, name)
      wirePin(pinPtr, dataPtr)
      modulePtr = modulePtr.parent
      dataPtr = pinPtr
    }

    var riseIdx = riseModules.indexOf(modulePtr)
    while (riseIdx != 0) {
      val riseModule = riseModules(riseIdx - 1)
      pinPtr = data.clone
      addPin(riseModule, pinPtr.asInput, name)
      wirePin(pinPtr, dataPtr)
      modulePtr = riseModule
      dataPtr = pinPtr
      riseIdx = riseIdx - 1;
    }

  //  println("EXTRACT " + data)
    return dataPtr
  }

  def wirePin(pin: Data, input: Node) {
    if (pin.inputs.isEmpty) pin.inputs += input
    else pin.inputs(0) = input
  }

  def addPin(m: Module, pin: Data, name: String) {
    // assign component
    pin.component = m
    // a hack to index pins by their name
    pin setName name
    // include in io
    pin.isIo = true
    (m.io) match {
      case io: Bundle => io += pin
    }
    // set its real name
    pin setName ("io_" + name)

    // for complex pins
   /* pin match {
      case dio: StreamIO[_] => {
        dio.ready.component = m
        dio.valid.component = m
        dio.bits.component = m
        dio.ready.isIo = true
        dio.valid.isIo = true
        dio.bits.isIo = true
        dio.ready setName ("io_" + name + "_ready")
        dio.valid setName ("io_" + name + "_valid")
        dio.bits setName ("io_" + name + "_bits")
      }
      case vio: ValidIO[_] => {
        vio.valid.component = m
        vio.bits.component = m
        vio.valid.isIo = true
        vio.bits.isIo = true
        vio.valid setName ("io_" + name + "_valid")
        vio.bits setName ("io_" + name + "_bits")
      }
      case _ =>
    }*/
  }
}

class UartIo extends Bundle {
  val tx = Bool(OUTPUT)
  val rx = Bool(INPUT)
}


 
class LogicAnalyser(p: LogicAnalyser.Parameter) extends Module {
  val io = new Bundle {
    //val config = new LogicAnalyser.Config(p).asInput
    
    val packetSlave = Stream(Fragment(Bits(width = 8))).flip
    val packetMaster = Stream(Fragment(Bits(width = 8)))
  }

  val UUID = LogicAnalyser.getUUID

  val probes = new MutableList[Data]
  var probesWidth = 0
  for (data <- p.dataList) {
    val probe = data.clone
    probes += probe
    probesWidth += probe.getWidth
  }

  def connect(dummy: Int = 0) {
    var probeUUID: Int = 0
    for (i <- Range(0, probes.length)) {
      LogicAnalyser.extractData(p.dataList(i), probes(i), s"zLogicAnalyser${this.UUID}_$probeUUID")
      probeUUID = probeUUID + 1
    }
  }

  var probesBits: UInt = UInt(width = probesWidth)
  probesBits := probes.reduceLeft(Cat(_, _)).toBits.toUInt

  val trigger = Bool()
  val triggerCounter = RegInit(UInt(0,10))
  triggerCounter := triggerCounter + UInt(1)
  trigger := triggerCounter === UInt((1<<triggerCounter.getWidth) - 1)

  val logger = Module(new LALogger(p, probesBits))
  logger.io.trigger := trigger
  logger.io.probe := probesBits
  logger.io.config.samplesLeftAfterTrigger := UInt((1<<p.memAddressWidth)/2)

  val logWidthAdapter = Module(new StreamFragmentWidthAdapter(probesBits.getWidth,8))
  logger.io.log >> logWidthAdapter.io.in
  
  
  val logHeader = Vec.fill(4) { Bits(width = 8) }
  logHeader(0) := UInt(0x11)
  logHeader(1) := UInt(0x22)
  logHeader(2) := UInt(0x33)
  logHeader(3) := UInt(0x44)
  
  val logHeaderGenerator = Module(new StreamFragmentGenerator(8,4))
  logHeaderGenerator.io.header := logHeader
  logHeaderGenerator.io.generate.valid := Bool(true)
  
  val logHeaderAdder = Module(new StreamFragmentHeaderAdder(8))
  logHeaderGenerator.io.out >> logHeaderAdder.io.header
  logWidthAdapter.io.out >> logHeaderAdder.io.in 
  //logHeaderAdder.io.out >> io.packetMaster
  
  
  
 /* val eventRx = Module(new StreamFragmentEventRx(8,4,true))
  eventRx.io.header := logHeader
  logHeaderAdder.io.out >> eventRx.io.in
  eventRx.io.events.ready := Bool(false)*/
  
  val identityRequest = Vec.fill(2) { Bits(width = 8) }
  identityRequest(0) := UInt(0x99)
  identityRequest(1) := UInt(0xAA)
  
  val identityEvent = Module(new StreamFragmentEventRx(8,2,true))
  identityEvent.io.header := identityRequest
  io.packetSlave >> identityEvent.io.in
  
  val identityResponse = Vec.fill(8) { Bits(width = 8) }
  identityResponse(0) := UInt(0x60)
  identityResponse(1) := UInt(0x61)
  identityResponse(2) := UInt(0x62)
  identityResponse(3) := UInt(0x63)
  identityResponse(4) := UInt(0x64)
  identityResponse(5) := UInt(0x65)
  identityResponse(6) := UInt(0x66)
  identityResponse(7) := UInt(0x67)

  val identityGenerator = Module(new StreamFragmentGenerator(8,8))
  identityGenerator.io.header := identityResponse
  identityEvent.io.events >> identityGenerator.io.generate
  identityGenerator.io.out >> io.packetMaster
   
}