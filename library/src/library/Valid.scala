package library

import Chisel.ValidIO
import Chisel._

class Valid[T <: Data](gen: T) extends ValidIO(gen) {
  def asMaster(dummy: Int = 0): Valid.this.type = { Valid.this }
  def asSlave(dummy: Int = 0): Valid.this.type = { flip; Valid.this }

  def >>(next: ValidIO[T]) {
    next.valid := valid
    next.bits := bits
  }

  def >->(next: ValidIO[T]) {
    val rValid = Reg(init = Bool(false))
    val rBits = Reg(gen)

    next.valid := rValid
    next.bits := rBits
  }

  override def clone: Valid.this.type =
    try {
      super.clone()
    } catch {
      case e: java.lang.Exception => {
        (new Valid(gen)).asInstanceOf[Valid.this.type]
      }
    }
}

class ValidReg[T <: Data](gen: T) extends Bundle {

  val valid = Reg(init = Bool(false))
  val bits = Reg(gen)

  def >>(next: ValidIO[T]) {
    next.valid := valid
    next.bits := bits
  }

  def >>(next: ValidReg[T]) {
    next.valid := valid
    next.bits := bits
  }

  override def clone: this.type = { new StreamReg(gen).asInstanceOf[this.type]; }
}
