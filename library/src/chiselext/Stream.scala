package chiselext

import Chisel._

object Stream {
  def apply[T <: Data](gen: T): Stream[T] = new Stream(gen)
}

class Stream[T <: Data](gen: T) extends DecoupledIO(gen) {
  def isFree(dummy: Int = 0): Bool = (~this.valid) | this.ready

  def asMaster(dummy: Int = 0): this.type = { this }
  def asSlave(dummy: Int = 0): this.type = { flip; this }

   //left to right simple connection
  def >>(next: DecoupledIO[T]) : DecoupledIO[T] = {
    next.valid := this.valid
    this.ready := next.ready
    next.bits := this.bits
    next
  }

  //left to right connection (cut data path, add 1 cycle latency)
  def >->(next: DecoupledIO[T]) : DecoupledIO[T] = {
    val rValid = Reg(init = Bool(false))
    val rBits = Reg(gen)

    this.ready := (~next.valid) | next.ready

    when(this.ready) {
      rValid := this.valid
      rBits := this.bits
    }

    next.valid := rValid
    next.bits := rBits
    next
  }
    
  //left to right connection (cut ready path, no latency added)
  def >/>(next: DecoupledIO[T]) : DecoupledIO[T] = {
    val rValid = Reg(init = Bool(false))
    val rBits = Reg(gen)

    next.valid := this.valid || rValid
    this.ready := ~rValid
    next.bits := Mux(rValid, rBits, this.bits)

    when(next.ready) {
      rValid := Bool(false)
    }

    when(this.ready && (~next.ready)) {
      rValid := this.valid
      rBits := this.bits
    }
    next
  }
 
  //left to right connection (cut ready and data path, add 1 cycle latency)
  def >/->(next: DecoupledIO[T]) : DecoupledIO[T] = {
    val stage = this.clone
    this >/> stage
    stage >-> next
    next
  }  
  
  //Take left DecoupledIO arbitration with right bits and return a new stream with
  //Usefull to translate DecoupledIO to a other type  (inputDecoupledIO ~ newBitsCalculatedFromInput) >> outputStream
  def ~[T2 <: Data](nextBits: T2): Stream[T2] = {
    val next = new Stream(nextBits)
    next.valid := this.valid
    this.ready := next.ready
    next.bits := nextBits
    next
  }

  def <<(previous: DecoupledIO[T]) {
    this.valid := previous.valid
    previous.ready := this.ready
    this.bits := previous.bits
  }


  def &(linkEnable: Bool): Stream[T] = {
    val next = new Stream(this.bits)
    next.valid := this.valid && linkEnable
    this.ready := next.ready && linkEnable
    next.bits := this.bits
    return next
  }

  def >>(next: StreamReg[T]) {
    ready := next.isFree()
    when(ready) {
      next.valid := valid
      next.bits := bits
    }
  }

  

  def >/->(next: Stream[T]) {
    val stage = this.clone
    this >/> stage
    stage >-> next
  }
  def <-/<(previous: Stream[T]) {
    val stage = this.clone
    previous >/> stage
    stage >-> this
  }

  override def clone: this.type = { new Stream(gen).asInstanceOf[this.type]; }
}
/*
object Stream {
  implicit def DecoupledIoToStream[T <: Data](in : DecoupledIO[T]) = {
    val stream = new Stream(in.bits.clone)
    stream.valid := in.valid
    stream.bits := in.bits
    in.ready := stream.ready
    stream
  }
}
*/


object StreamReg {
  def apply[T <: Data](gen: T): StreamReg[T] = new StreamReg(gen)
}

class StreamReg[T <: Data](gen: T) extends Bundle {
  val valid = Reg(init = Bool(false))
  val ready = Bool()
  val bits = Reg(gen)

  when(ready){
    valid := Bool(false)
  }
  
  def >>(next: DecoupledIO[T]) {
    next.valid := valid
    ready := next.ready
    next.bits := bits
  }

  def >->(next: StreamReg[T]) {
    ready := next.isFree()
    when(ready) {
      next.valid := valid
      next.bits := bits
    }
  }

  def isFree(dummy: Int = 0): Bool = (~this.valid) | this.ready
  override def clone: this.type = { new StreamReg(gen).asInstanceOf[this.type]; }
}

class DispatcherReg[T <: Data](gen: T, n: Int) extends Module {
  val io = new Bundle {
    val in = new Stream(gen).asSlave()
    val out = Vec.fill(n) { new Stream(gen) }
  }

  val out = Vec.fill(n) { new Stream(gen) }

  io.in.ready := Bool(false)
  var carry = Bool(true)
  for (i <- (0 to n - 1)) {
    when(out(i).ready) { io.in.ready := Bool(true) }

    out(i).valid := Bool(false)
    out(i).bits := io.in.bits
    when(carry && out(i).ready) {
      out(i).valid := io.in.valid
    }
    out(i) >-> io.out(i)
    carry = (carry & (~out(i).ready))
  }

}

class Fork[T <: Data](gen: T, n: Int) extends Module {
  val io = new Bundle {
    val in = new Stream(gen).asSlave()
    val out = Vec.fill(n) { new Stream(gen) }
  }
  val linkEnable = Vec.fill(n) { RegInit(Bool(true)) }

  io.in.ready := Bool(true)
  for (i <- (0 to n - 1)) {
    when(~io.out(i).ready && linkEnable(i)) {
      io.in.ready := Bool(false)
    }
  }

  for (i <- (0 to n - 1)) {
    io.out(i).valid := io.in.valid && linkEnable(i)
    io.out(i).bits := io.in.bits
    when(io.out(i).fire()) {
      linkEnable(i) := Bool(false)
    }
  }

  when(io.in.ready) {
    linkEnable.map(_ := Bool(true))
  }
}
/*
    val out = Vec.fill(n) { new Stream(gen) }

    io.in.ready := Bool(true)
    for (i <- (0 to n - 1)) {
      when(~out(i).ready) { io.in.ready := Bool(false) }
    }

    for (i <- (0 to n - 1)) {
      out(i).valid := io.in.fire()
      out(i).bits := io.in.bits
      out(i) >-> io.out(i)
    }
 */










/*
  def >>>>[T2 <: Data](next: DecoupledIO[T2], f: (T, T2) => Unit) {
    next.valid := this.valid
    this.ready := next.ready
    f(this.bits, next.bits)
  }

  //def cp[T2 <: Data](src : T) : T2 = {return src.asInstanceOf[T2]}
  def >>[T2 <: Data](next: DecoupledIO[T2], f: (T) => T2) {
    next.valid := this.valid
    this.ready := next.ready
    next.bits := f(this.bits)
  }
  def >>>[T2 <: Data](next: DecoupledIO[T2], nextBits: T2) {
    next.valid := this.valid
    this.ready := next.ready
    next.bits := nextBits
  }
  def >>(next: DecoupledIO[T]) {
    next.valid := this.valid
    this.ready := next.ready
    next.bits := this.bits
  }*/