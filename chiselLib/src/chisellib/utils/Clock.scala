package chisellib.utils

import Chisel._

object CrossClockStream_HandShake {
  def apply[T <: Data](in: Flow[T], clockIn: Clock, clockOut: Clock): Stream[T] = {

    val inToogle = Reg(init = Bool(false), clock = clockIn)
    val inBits = Reg(in.bits, null.asInstanceOf[T], null.asInstanceOf[T], clockIn)
    when(in.valid) {
      inToogle := !inToogle
      inBits := in.bits
    }

    val outTarget1 = Reg(init = Bool(false), next = inToogle, clock = clockOut);
    val outTarget = Reg(init = Bool(false), next = outTarget1, clock = clockOut);
    val outHit = Reg(init = Bool(false), clock = clockOut);

    val outStream = StreamReg(in.bits)
    when(outTarget != outHit) {
      outHit := !outHit
      outStream.valid := Bool(true)
      outStream.bits := inBits
    }
    //outStream.bits := in.bits
    return outStream.toStream()
  }
}

object CrossClockEvent_HandShake {
  def apply(in: Bool, clockIn: Clock, clockOut: Clock): Bool = {
    val inToogle = Reg(init = Bool(false), clock = clockIn)
    when(in) {
      inToogle := !inToogle

    }

    val outTarget1 = Reg(init = Bool(false), next = inToogle, clock = clockOut);
    val outTarget = Reg(init = Bool(false), next = outTarget1, clock = clockOut);
    val outHit = Reg(init = Bool(false), clock = clockOut);

    when(outTarget != outHit) {
      outHit := !outHit
    }

    return outTarget != outHit
  }

}