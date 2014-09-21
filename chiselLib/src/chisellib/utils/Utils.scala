package chisellib.utils

import Chisel._
object in {
  def apply(that: Data): Data = {
    that.asInput
    that
  }
}
object out {
  def apply(that: Data): Data = {
    that.asOutput
    that
  }
}
//Dont restart in same time than in flow packet
class FlowDeserializer[T <: Data](packetWidth: Integer, dataType: T) extends Module {
  val io = new Bundle {
    val restart = Bool(INPUT)
    val in = Flow(UInt(width = packetWidth)).asSlave()
    val out = Flow(dataType).asMaster()
  }
  val inWidth = io.in.bits.getWidth
  val outWidth = io.out.bits.getWidth

  val buffer = if (outWidth > inWidth) Reg(UInt(width = outWidth - inWidth)) else null
  val counter = if (outWidth > inWidth) Reg(UInt(width = log2Up((buffer.getWidth - 1) / inWidth)), init = UInt(0)) else null
  val flush = RegInit(Bool(false))

  flush := Bool(false)

  when(io.restart) {
    if (counter != null)
      counter := UInt(0)
  }
  when(io.in.valid) {
    if (buffer == null) {
      flush := Bool(true)
    } else {
      if (buffer.getWidth > inWidth)
        buffer := Cat(buffer(buffer.getWidth - 1 - inWidth, 0), io.in.bits)
      else
        buffer := io.in.bits

      counter := counter + UInt(1)
      when(counter === UInt((buffer.getWidth - 1) / inWidth)) {
        flush := Bool(true)
        counter := UInt(0)
      }
    }
  }

  val data = dataType.clone
  data := data.fromNode(if (buffer == null) io.in.bits else Cat(buffer, io.in.bits))

  io.out.valid := flush
  io.out.bits := data
}