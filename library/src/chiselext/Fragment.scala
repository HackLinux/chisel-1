package chiselext

import Chisel._

object Fragment {
  def apply[T <: Data](gen: T): Fragment[T] = new Fragment(gen)
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

  shiftRegisterReady := ~shiftRegisterValid || (io.out.ready && shiftRegisterCounter === UInt(0))

  when(shiftRegisterReady) {
    shiftRegisterValid := io.in.valid
    shiftRegister := io.in.bits.fragment
    shiftRegisterLast := io.in.bits.last
    shiftRegisterCounter := UInt(shiftCount - 1)
  }

  io.in.ready := shiftRegisterReady
}
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
  when(~done) {
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

}

class StreamFragmentHeaderAdder(fragmentWidth: Int, asynchronous: Boolean = false) extends Module {
  require(asynchronous == false)

  val io = new Bundle {
    val header = Stream(Fragment(UInt(width = fragmentWidth))).asSlave()
    val in = Stream(Fragment(UInt(width = fragmentWidth))).asSlave()
    val out = Stream(Fragment(UInt(width = fragmentWidth))).asMaster()
  }

  val done = RegInit(Bool(false))

  when(~done) {
    io.header >> io.out
    when(~io.in.valid) {
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

class StreamFragmentGenerator(fragmentWidth: Int, fragmentCount: Int) extends Module {
  val io = new Bundle {
    val header = Vec.fill(fragmentCount) { UInt(width = fragmentWidth) }.asInput
    val generate = Stream(UInt(width = 1)).asSlave()
    val out = Stream(Fragment(UInt(width = fragmentWidth))).asMaster()
  }

  val counter = RegInit(UInt(0, log2Up(fragmentCount)))

  io.generate.ready := Bool(false)
  io.out.bits.last := Bool(false)

  io.out.valid := io.generate.valid
  io.out.bits.fragment := io.header(counter)
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

class StreamFragmentEventRx(fragmentWidth: Int, fragmentCount: Int, nonBlocking: Boolean = false) extends Module {
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

  io.in.ready := ~(counter === UInt(fragmentCount - 1) && headerMatchCalculated && ~io.events.isFree() && ~Bool(nonBlocking))
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

  when(~io.in.ready){
	headerMatch := headerMatch
  }
  
  event >> io.events

}


