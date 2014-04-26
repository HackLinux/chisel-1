package chiselext
import java.io._

import Chisel._
import scala.collection.mutable.ArrayBuffer

object VHDL {

  /*def emitWidth(node: Node): String =
    if (node.width == 1) "" else "[" + (node.width - 1) + ":0]"

  def emitComponent(c: Module): String = {
    if (c.isInstanceOf[BlackBox])
      return ""

    val res = new StringBuilder()
    var first = true;
    var nl = "";
    if (c.clocks.length > 0 || c.resets.size > 0)
      res.append((c.clocks ++ c.resets.values.toList).map(x => "input " + x.name).reduceLeft(_ + ", " + _))
    val ports = new ArrayBuffer[StringBuilder]
    for ((n, w) <- c.wires) {
      // if(first && !hasReg) {first = false; nl = "\n"} else nl = ",\n";
      w match {
        case io: Bits => {
          val prune = if (io.prune && c != Module.topComponent) "//" else ""
          if (io.dir == INPUT) {
            ports += new StringBuilder(nl + "    " + prune + "input " +
              emitWidth(io) + " " + io.name);
          } else if (io.dir == OUTPUT) {
            ports += new StringBuilder(nl + "    " + prune + "output" +
              emitWidth(io) + " " + io.name);
          }
        }
      };
    }
    val uncommentedPorts = ports.filter(!_.result.contains("//"))
    uncommentedPorts.slice(0, uncommentedPorts.length - 1).map(_.append(","))
    if (c.clocks.length > 0 || c.resets.size > 0) res.append(",\n") else res.append("\n")
    res.append(ports.map(_.result).reduceLeft(_ + "\n" + _))
    res.append("\n);\n\n");
    // TODO: NOT SURE EXACTLY WHY I NEED TO PRECOMPUTE TMPS HERE

    res.append("endmodule\n\n");
    res.result();
  }*/
  
  
  def createIpFile(module : Module){
	  val file = new File("ip.vhd")
	  val p = new java.io.PrintWriter(file)
	  p.println(component(module))
	  p.println(signals(module))
	  p.println(instance(module))
	  p.flush()
  }

   def instance(module: Module, nameInit : String = null): String = {
    var vhdlComponent: String = ""
    var name = nameInit;
    if(name == null) name = module.getClass().getName().split('.').last + "_inst"
    vhdlComponent += s"  $name : " + module.getClass().getName().split('.').last + "\n"
    vhdlComponent += "    port map(\n"
    vhdlComponent += instanceClockReset(module)
    val ioStr = instanceIo(module.io)
    vhdlComponent += ioStr.substring(0, ioStr.size-2) + "\n"
    vhdlComponent += "    );\n"

    vhdlComponent
  }
 
  def component(module: Module): String = {
    var vhdlComponent: String = ""
    vhdlComponent += "  component " + module.getClass().getName().split('.').last + "\n"
    vhdlComponent += "    port (\n"
    vhdlComponent += componentClockReset(module)
    val ioStr = componentIo(module.io)
    vhdlComponent += ioStr.substring(0, ioStr.size-2) + "\n"
    vhdlComponent += "    );\n"
    vhdlComponent += "  end component;\n"

    vhdlComponent
  }

  def componentIo(data: Data, offset: String = "io", stackInit: String = ""): String = {
    var stack: String = stackInit
    data match {
      case b: Bundle => {
        for (e <- b.elements) {
          stack += componentIo(e._2, offset + "_" + e._1)
        }
      }
      case b: Bool => {
        var dir = "";
        if(b.dir == INPUT) dir = "in" else dir = "out"
        stack += s"      $offset : $dir std_logic;\n"
      }
      case b: UInt => {
        var dir = ""
        if(b.dir == INPUT) dir = "in" else dir = "out"
        stack += s"      $offset : $dir std_logic_vector(${b.getWidth - 1} downto 0);\n"
      }
      case _ => stack += "error \n"
    }
    stack
  }
  def componentClockReset(module: Module): String = {
    var stack: String = ""
    stack += "      clk : in std_logic;\n"
    stack += "      reset : in std_logic;\n"
      println(module.clocks)
    if (module.clocks.length > 0 || module.resets.size > 0)
      stack += (module.clocks ++ module.resets.values.toList).map(x => "input " + x.name).reduceLeft(_ + ", " + _)
    stack
  }
  def signals(module: Module): String = {
    return signalsIo(module.io)
  }
  
  
  def signalsIo(data: Data, offset: String = "io", stackInit: String = ""): String = {
    var stack: String = stackInit
    data match {
      case b: Bundle => {
        for (e <- b.elements) {
          stack += signalsIo(e._2, offset + "_" + e._1)
        }
      }
      case b: Bool => {
        stack += s"  signal $offset : std_logic;\n"
      }
      case b: UInt => {
        var dir = ""
        if(b.dir == INPUT) dir = "in" else dir = "out"
        stack += s"  signal $offset : std_logic_vector(${b.getWidth - 1} downto 0);\n"
      }
      case _ => stack += "error \n"
    }
    stack
  }
  
  def instanceIo(data: Data, offset: String = "io", stackInit: String = ""): String = {
    var stack: String = stackInit
    data match {
      case b: Bundle => {
        for (e <- b.elements) {
          stack += instanceIo(e._2, offset + "_" + e._1)
        }
      }
      case b: Bool => {
        stack += s"      $offset => $offset,\n"
      }
      case b: UInt => {
        stack += s"      $offset => $offset,\n"
      }
      case _ => stack += "error \n"
    }
    stack
  }
  def instanceClockReset(module: Module): String = {
    var stack: String = ""
    stack += "      clk => clk,\n"
    stack += "      reset => reset,\n"
    if (module.clocks.length > 0 || module.resets.size > 0)
      stack += (module.clocks ++ module.resets.values.toList).map(x => "input " + x.name).reduceLeft(_ + ", " + _)
    stack
  }
  

}