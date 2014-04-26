package chiselext

import Chisel._

class RamWriteBits(addrWidth: Int, dataWidth: Int) extends Bundle { //Top Level component
  val address = UInt(width = addrWidth)
  val data = UInt(width = dataWidth)
 
  override def clone: this.type = { new RamWriteBits(addrWidth,dataWidth).asInstanceOf[this.type]; }  
  
  
}
/*
class RamWriteBits extends Bundle { //Top Level component
  val address = UInt(width = 1)
  val data = UInt(width = 2)
 
 // override def clone: this.type = { new RamWriteBits(addrWidth,dataWidth).asInstanceOf[this.type]; }  
}*/
class RamWriteStream(addrWidth: Int, dataWidth: Int) extends Stream(new RamWriteBits(addrWidth, dataWidth)) { //Top Level component
 /* override def clone: this.type =
    try {
      super.clone()
    } catch {
      case e: java.lang.Exception => {
        new DecoupledIO(gen).asInstanceOf[this.type]
      }
    }*/
}