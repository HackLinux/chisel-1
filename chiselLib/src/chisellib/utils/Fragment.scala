package chisellib.utils

import Chisel._
import scala.math.BigInt.long2bigInt

object Fragment {
  def apply[T <: Data](gen: T): Fragment[T] = new Fragment(gen)
  
  def isFirst[T <: Data](flow : Flow[Fragment[T]]) : Bool = {
    val first = RegInit(Bool(true))
    
    when(flow.valid){
      first := flow.bits.last
    }
    
    return first
  }
}

class Fragment[T <: Data](gen: T) extends Bundle {
  val last = Bool(OUTPUT)
  val fragment = gen.clone.asOutput

  override def clone: this.type = { new Fragment(gen).asInstanceOf[this.type]; }
}

class StreamFragmentWidthAdapter(inWidth: Int, outWidth: Int, asynchronous: Boolean = false, packed: Boolean = false) extends Module {
  require(packed == false)
  require(asynchronous == false)
  require(inWidth > outWidth)

  val io = new Bundle {
    val in = Stream(Fragment(UInt(width = inWidth))).asSlave()
    val out = Stream(Fragment(UInt(width = outWidth))).asMaster()
  }
  var shiftCount = inWidth / outWidth
  if (inWidth % outWidth != 0) shiftCount = shiftCount + 1
  val shiftRegisterValid = RegInit(Bool(false))
  val shiftRegisterReady = Bool()
  val shiftRegister = Reg(UInt(width = inWidth))
  val shiftRegisterCounter = Reg(UInt(width = log2Up(shiftCount)))
  val shiftRegisterLast = RegInit(Bool(false))

  io.out.valid := shiftRegisterValid
  io.out.bits.fragment := shiftRegister
  io.out.bits.last := shiftRegisterLast && shiftRegisterCounter === UInt(0)

  when(io.out.ready) {
    shiftRegister := shiftRegister(inWidth - 1, outWidth)
    shiftRegisterCounter := shiftRegisterCounter - UInt(1)
  }

  shiftRegisterReady := !shiftRegisterValid || (io.out.ready && shiftRegisterCounter === UInt(0))

  when(shiftRegisterReady) {
    shiftRegisterValid := io.in.valid
    shiftRegister := io.in.bits.fragment
    shiftRegisterLast := io.in.bits.last
    shiftRegisterCounter := UInt(shiftCount - 1)
  }

  io.in.ready := shiftRegisterReady
}/*
class StreamFragmentHeaderAdderUInt(headerWidth: Int, fragmentWidth: Int, asynchronous: Boolean = false) extends Module {
  require(asynchronous == false)

  val io = new Bundle {
    val header = UInt(INPUT, headerWidth)
    val in = Stream(Fragment(UInt(width = fragmentWidth))).flip
    val out = Stream(Fragment(UInt(width = fragmentWidth)))
  }

  var fragmentCount = headerWidth / fragmentWidth
  //if (headerWidth % fragmentWidth != 0) fragmentCount = fragmentCount + 1
  val counter = RegInit(UInt(0, log2Up(fragmentCount)))
  val done = RegInit(Bool(false))
  when(!done) {
    io.out.valid := io.in.valid
    io.out.bits.last := Bool(false)
    // io.out.bits.fragment := io.header((counter + UInt(1)) * UInt(fragmentWidth - 1), counter * UInt(fragmentWidth))
    io.out.bits.fragment := io.header >> (counter * UInt(fragmentWidth))
    io.in.ready := Bool(false)
  } otherwise {
    io.out.valid := io.in.valid
    io.out.bits := io.in.bits
    io.in.ready := io.out.ready
  }

  when(io.out.fire()) {
    counter := counter + UInt(1)
    when(counter === UInt(fragmentCount - 1)) {
      done := Bool(true)
      counter := UInt(0)
    }
    when(io.in.bits.last) {
      done := Bool(false)
    }
  }

}*/


object StreamFragmentHeaderAdder {
  def apply(header: UInt): StreamFragmentHeaderAdder = {
    val vec = Vec.fill(1) { header }
    new StreamFragmentHeaderAdder(vec)
  }
}

class StreamFragmentHeaderAdder(header: Vec[UInt]) extends Module {
  def fragmentCount = header.size
  def fragmentWidth = header(0).getWidth

  val io = new Bundle {
    val in = Stream(Fragment(UInt(width = fragmentWidth))).asSlave()
    val out = Stream(Fragment(UInt(width = fragmentWidth))).asMaster()
  }

  val headerGenerator = Module(new StreamFragmentGenerator(header))
  headerGenerator.io.packetData := header
  headerGenerator.io.generate.valid := Bool(true)

  val headerAdder = Module(new StreamFragmentHeaderJoin(fragmentWidth))
  headerAdder.io.header << headerGenerator.io.out
  headerAdder.io.in << io.in
  headerAdder.io.out >> io.out

}

class StreamFragmentHeaderJoin(fragmentWidth: Int) extends Module {

  val io = new Bundle {
    val header = Stream(Fragment(UInt(width = fragmentWidth))).asSlave()
    val in = Stream(Fragment(UInt(width = fragmentWidth))).asSlave()
    val out = Stream(Fragment(UInt(width = fragmentWidth))).asMaster()
  }

  val done = RegInit(Bool(false))

  when(!done) {
    io.header >> io.out
    when(!io.in.valid) {
      io.out.valid := Bool(false)
      io.header.ready := Bool(false)
    }
    io.out.bits.last := Bool(false)
    io.in.ready := Bool(false)
  } otherwise {
    io.in >> io.out
    io.header.ready := Bool(false)
  }

  when(io.header.fire() && io.header.bits.last) {
    done := Bool(true)
  }

  when(io.in.fire() && io.in.bits.last) {
    done := Bool(false)
  }

}

class StreamFragmentGenerator(vec: Vec[UInt]) extends Module {
  def fragmentCount = vec.size
  def fragmentWidth = vec(0).getWidth

  val io = new Bundle {
    val packetData = Vec.fill(fragmentCount) { UInt(width = fragmentWidth) }.asInput
    val generate = Stream(UInt(width = 1)).asSlave()
    val out = Stream(Fragment(UInt(width = fragmentWidth))).asMaster()
  }

  val counter = RegInit(UInt(0, log2Up(fragmentCount)))

  io.generate.ready := Bool(false)
  io.out.bits.last := Bool(false)

  io.out.valid := io.generate.valid
  io.out.bits.fragment := io.packetData(counter)
  when(io.out.fire()) {
    counter := counter + UInt(1);
  }

  when(counter === UInt(fragmentCount - 1)) {
    io.out.bits.last := Bool(true)
    when(io.out.fire()) {
      counter := UInt(0)
      io.generate.ready := Bool(true)
    }
  }

}

class StreamFragmentEventRx(fragmentWidth: Int, fragmentCount: Int) extends Module {
  val io = new Bundle {
    val header = Vec.fill(fragmentCount) { UInt(width = fragmentWidth) }.asInput
    val in = Stream(Fragment(UInt(width = fragmentWidth))).asSlave()
    val events = Stream(UInt(width = 1)).asMaster()
  }

  val event = StreamReg(UInt(width = 1))
  val counter = RegInit(UInt(0, log2Up(fragmentCount)))
  val headerMatch = RegInit(Bool(true))
  val headerMatchCalculated = (Bool())
  event.bits := UInt(0)

  headerMatch := headerMatchCalculated
  headerMatchCalculated := headerMatch

  io.in.ready := !(counter === UInt(fragmentCount - 1) && headerMatchCalculated && !io.events.isFree())
  when(io.in.valid) {
    when(io.header(counter) != io.in.bits.fragment) {
      headerMatchCalculated := Bool(false)
    }
    when(headerMatchCalculated && counter === UInt(fragmentCount - 1)) {
      event.valid := Bool(true)
      headerMatch := Bool(false)
    }
  }

  when(io.in.fire()) {
    counter := counter + UInt(1)
    when(io.in.bits.last) {
      counter := UInt(0)
      headerMatch := Bool(true)
    }
  }

  when(!io.in.ready) {
    headerMatch := headerMatch
  }

  event >> io.events

}
object FlowFragmentEventRx {
  def apply(header: UInt): FlowFragmentEventRx = {
    val vec = Vec.fill(1) { header }
    new FlowFragmentEventRx(vec)
  }
}

class FlowFragmentEventRx(header: Vec[UInt]) extends Module {
  def fragmentCount = header.size
  def fragmentWidth = header(0).getWidth

  val io = new Bundle {
    val in = Flow(Fragment(UInt(width = fragmentWidth))).asSlave()
    val events = Stream(UInt(width = 1)).asMaster()
  }

  val event = StreamReg(UInt(width = 1))
  val counter = RegInit(UInt(0, log2Up(fragmentCount)))
  val headerMatch = RegInit(Bool(true))
  val headerMatchCalculated = (Bool())
  event.bits := UInt(0)

  headerMatch := headerMatchCalculated
  headerMatchCalculated := headerMatch

  when(io.in.valid) {
    when(header(counter) != io.in.bits.fragment) {
      headerMatchCalculated := Bool(false)
    }
    when(headerMatchCalculated && counter === UInt(fragmentCount - 1)) {
      event.valid := Bool(true)
      headerMatch := Bool(false)
    }

    counter := counter + UInt(1)
    when(io.in.bits.last) {
      counter := UInt(0)
      headerMatch := Bool(true)
    }
  }

  event >> io.events

}

object FlowFragmentFilter {
  def apply(header: UInt): FlowFragmentFilter = {
    val vec = Vec.fill(1) { header }
    new FlowFragmentFilter(vec)
  }
}

class FlowFragmentFilter(header: Vec[UInt]) extends Module {
  def fragmentCount = header.size
  def fragmentWidth = header(0).getWidth

  val io = new Bundle {
    val in = Flow(Fragment(UInt(width = fragmentWidth))).asSlave()
    val out = Flow(Fragment(UInt(width = fragmentWidth))).asMaster()
  }

  val counter = RegInit(UInt(0, log2Up(fragmentCount)))
  val headerFail = RegInit(Bool(false))
  val headerDone = RegInit(Bool(false))

  when(io.in.valid) {
    when(!headerDone && header(counter) != io.in.bits.fragment) {
      headerFail := Bool(true)
    }
    when(counter === UInt(fragmentCount - 1)) {
      headerDone := Bool(true)
    }

    counter := counter + UInt(1)
    when(io.in.bits.last) {
      counter := UInt(0)
      headerFail := Bool(false)
      headerDone := Bool(false)
    }
  }

  io.out << (io.in & (headerDone && !headerFail))

}

class FlowFragmentAddressFilter(address : UInt) extends Module {
  def fragmentWidth = address.getWidth

  val io = new Bundle {
    val in = Flow(Fragment(UInt(width = fragmentWidth))).asSlave()
    val out = Flow(Fragment(UInt(width = fragmentWidth))).asMaster()
  }
  val first = Fragment.isFirst(io.in)
  val headerMatch = RegInit(Bool(false))

  when(io.in.valid) {
    when(first && (address === io.in.bits.fragment || UInt((1l<<fragmentWidth)-1) === io.in.bits.fragment)) {
      headerMatch := Bool(true)
    }

    when(io.in.bits.last) {
       headerMatch := Bool(false)
    }
  }

  io.out << (io.in & headerMatch)
}

class StreamFragmentArbiter(fragmentWidth: Int, n: Int) extends Module {
  val io = new Bundle {
    val in = Vec.fill(n) { Stream(Fragment(UInt(width = fragmentWidth))).asSlave() }
    val out = Stream(Fragment(UInt(width = fragmentWidth))).asMaster()
  }

  val locked = RegInit(Bool(false))
  val lockIdx = RegInit(UInt(0, log2Up(n)))
  val chosen = UInt(width = log2Up(n))

  var choose = UInt(n - 1)
  for (i <- n - 2 to 0 by -1) {
    choose = Mux(io.in(i).valid, UInt(i), choose)
  }
  chosen := Mux(locked, lockIdx, choose)

  when(!locked) {
    lockIdx := chosen
  }

  io.in.map(_.ready := Bool(false))
  io.in(chosen) >> io.out

  when(io.in(chosen).fire()) {
    locked := !io.in(chosen).bits.last
  }
}


