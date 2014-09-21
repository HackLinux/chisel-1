package chisellib.bus

import Chisel.Bundle
import Chisel.UInt
import chisellib.utils._

class AxiSlaveConfig(
  val addrWidth: Integer,
  val dataWidth: Integer) {

}
class AxiSlaveWriteCommand(config: AxiSlaveConfig) extends Bundle {
  val addr = UInt(width = config.addrWidth)
  val prot = UInt(width = 3)
  override def clone: this.type = { new AxiSlaveWriteCommand(config).asInstanceOf[this.type]; }
}
class AxiSlaveWriteData(config: AxiSlaveConfig) extends Bundle {
  val data = UInt(width = config.dataWidth)
  val strb = UInt(width = config.dataWidth / 8)
  override def clone: this.type = { new AxiSlaveWriteData(config).asInstanceOf[this.type]; }
}
class AxiSlaveWriteResponse(config: AxiSlaveConfig) extends Bundle {
  val resp = UInt(width = 2)
  override def clone: this.type = { new AxiSlaveWriteResponse(config).asInstanceOf[this.type]; }
}
class AxiSlaveReadCommand(config: AxiSlaveConfig) extends Bundle {
  val addr = UInt(width = config.addrWidth)
  override def clone: this.type = { new AxiSlaveReadCommand(config).asInstanceOf[this.type]; }
}
class AxiSlaveReadResponse(config: AxiSlaveConfig) extends Bundle {
  val data = UInt(width = config.dataWidth)
  val resp = UInt(width = 2)
  override def clone: this.type = { new AxiSlaveReadResponse(config).asInstanceOf[this.type]; }
}
class AxiSlave(config: AxiSlaveConfig) extends Bundle {
  val writeCmd = Stream(new AxiSlaveWriteCommand(config)).asMaster()
  val writeData = Stream(new AxiSlaveWriteData(config)).asMaster()
  val writeRet = Stream(new AxiSlaveWriteResponse(config)).asSlave()
  val readCmd = Stream(new AxiSlaveReadCommand(config)).asMaster()
  val readRet = Stream(new AxiSlaveReadResponse(config)).asSlave()
  override def clone: this.type = { new AxiSlave(config).asInstanceOf[this.type]; }
}