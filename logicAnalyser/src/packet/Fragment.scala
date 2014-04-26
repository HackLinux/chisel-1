package packet

import Chisel._

object Fragment {
  def apply[T <: Data](gen: T): FragmentIO[T] = new FragmentIO(gen)
}

class FragmentIO[T <: Data](gen: T) extends Bundle {
  val last = Bool(OUTPUT)
  val fragment = gen.clone.asOutput

  override def clone: this.type = { new FragmentIO(gen).asInstanceOf[this.type]; }
}

object DecoupledFragment {
  class WidthAdapter(inWidth: Int, outWidth: Int, asynchronous: Boolean = false, packed: Boolean = false) extends Module {
    require(packed == false)
    require(asynchronous == false)
    require(inWidth > outWidth)

    val io = new Bundle {
      val in = Decoupled(Fragment(Bits(width = inWidth))).flip
      val out = Decoupled(Fragment(Bits(width = outWidth)))
    }
    io.out.bits.fragment := Bits(0)
    var shiftCount = inWidth / outWidth
    if (inWidth % outWidth != 0) shiftCount = shiftCount + 1
    val shiftRegisterValid = RegInit(Bool(false))
    val shiftRegisterReady = Bool()
    val shiftRegister = Reg(Bits(width = inWidth))
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
  class HeaderAdder(headerWidth: Int, fragmentWidth: Int, asynchronous: Boolean = false) extends Module {
    require(asynchronous == false)

    val io = new Bundle {
      val header = Bits(INPUT,headerWidth)
      val in = Decoupled(Fragment(Bits(width = fragmentWidth))).flip
      val out = Decoupled(Fragment(Bits(width = fragmentWidth)))
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
}




