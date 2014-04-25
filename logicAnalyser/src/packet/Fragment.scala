package packet

import Chisel._

object Fragment {
  def apply[T <: Data](gen: T): FragmentIO[T] = new FragmentIO(gen)
}

class FragmentIO[T <: Data](gen: T) extends Bundle {
  val last = Bool(OUTPUT)
  val fragment = gen.clone.asOutput
}

class DecoupledFragmentWidthAdapter[T <: Data, T2 <: Data](inType: T, outType: T2, asynchronous: Boolean = false, packed: Boolean = false) extends Module {
  require(packed == false)
  require(asynchronous == false)
  require(inType.getWidth > outType.getWidth)
  val io = new Bundle {
    val in = Decoupled(Fragment(inType))
    val out = Decoupled(Fragment(outType))
  }

  var shiftCount = inType.getWidth / outType.getWidth - 1
  if ((inType.getWidth % outType.getWidth) != 0) shiftCount = shiftCount + 1
  val shiftRegisterValid = RegInit(Bool(false))
  val shiftRegisterReady = Bool()
  val shiftRegister = Reg(inType.toBits.clone)
  val shiftRegisterCounter = UInt(log2Up(shiftCount))
  val shiftRegisterLast = RegInit(Bool(false))
  
  
  io.out.valid := shiftRegisterValid
  io.out.bits.fragment := shiftRegisterValid
  io.out.bits.last := shiftRegisterCounter === UInt(0)
  
  when(io.out.ready) {
    shiftRegister := shiftRegister(shiftRegister.getWidth - 1, outType.getWidth)
    shiftRegisterCounter := shiftRegisterCounter - UInt(1)
  }
 
  shiftRegisterReady := ~shiftRegisterValid || (io.out.ready && shiftRegisterCounter === UInt(0))

  when(shiftRegisterReady) {
    shiftRegisterValid := io.in.valid
    shiftRegister := io.in.bits.fragment.toBits
    shiftRegisterLast := io.in.bits.last
    shiftRegisterCounter := UInt(shiftCount)
  }

  io.in.ready := shiftRegisterReady
  
  

}