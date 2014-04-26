package logicanalyser

import Chisel._
import packet.Fragment

object LASerializer {
 /* class Config(p: LogicAnalyser.Parameter) extends Bundle {
    
  }*/
}

class LASerializer(p: LogicAnalyser.Parameter, probesData: Data) extends Module {
  val io = new Bundle {
   // val config = new LALogger.Config(p).asInput

    val log = Decoupled(probesData.clone).flip
    val fragments = Decoupled(Fragment(Bits(width = 8)))
  }

  
}