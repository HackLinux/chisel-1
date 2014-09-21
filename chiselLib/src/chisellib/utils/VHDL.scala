package chisellib.utils
import java.io._
import Chisel._

object VHDL {


  def createIpFile(module: Module) {
    val file = new File("ip.vhd")
    val p = new java.io.PrintWriter(file)
    p.println(component(module))
    p.println(signals(module))
    p.println(instance(module))
    p.flush()
  }

  def instance(module: Module, nameInit: String = null): String = {
    var vhdlComponent: String = ""
    var name = nameInit;
    if (name == null) name = module.getClass().getName().split('.').last + "_inst"
    vhdlComponent += s"  $name : " + module.getClass().getName().split('.').last + "\n"
    vhdlComponent += "    port map(\n"
    vhdlComponent += instanceClockReset(module)
    val ioStr = instanceIo(module.io)
    vhdlComponent += ioStr.substring(0, ioStr.size - 2) + "\n"
    vhdlComponent += "    );\n"

    vhdlComponent
  }

  def component(module: Module): String = {
    var vhdlComponent: String = ""
    vhdlComponent += "  component " + module.getClass().getName().split('.').last + "\n"
    vhdlComponent += "    port (\n"
    vhdlComponent += componentClockReset(module)
    val ioStr = componentIo(module.io)
    vhdlComponent += ioStr.substring(0, ioStr.size - 2) + "\n"
    vhdlComponent += "    );\n"
    vhdlComponent += "  end component;\n"

    vhdlComponent
  }

  def componentIo(data: Data, offset: String = "io", stackInit: String = ""): String = {
    var stack: String = stackInit

    for (e <- data.flatten) {
      e._2 match {
        case b: Bool => {
          var dir = "";
          if (b.dir == INPUT) dir = "in" else dir = "out"
          stack += s"      ${e._1} : $dir std_logic;\n"
        }
        case b: UInt => {
          var dir = ""
          if (b.dir == INPUT) dir = "in" else dir = "out"
          stack += s"      ${e._1} : $dir std_logic_vector(${b.getWidth - 1} downto 0);\n"
        }
        case _ => stack += "error \n"
      }
    }

    stack
  }
  def componentClockReset(module: Module): String = {
    var stack: String = ""
    /*stack += "      clk : in std_logic;\n"
    stack += "      reset : in std_logic;\n"*/
    println(module.clocks)
    if (module.clocks.length > 0 || module.resets.size > 0)
      stack += (module.clocks ++ module.resets.values.toList).map(x => "      " + x.name + " : in std_logic;\n" ).reduceLeft(_ + ", " + _)
    stack
  }
  def signals(module: Module): String = {
    return signalsIo(module.io)
  }

  def signalsIo(data: Data): String = {
    var stack: String = ""
    for (e <- data.flatten) {
      e._2 match {
        case b: Bool => {
          stack += s"  signal ${e._1} : std_logic;\n"
        }
        case b: UInt => {
          var dir = ""
          if (b.dir == INPUT) dir = "in" else dir = "out"
          stack += s"  signal ${e._1} : std_logic_vector(${b.getWidth - 1} downto 0);\n"
        }
        case _ => stack += "error \n"
      }
    }
    stack
  }

  def instanceIo(data: Data, offset: String = "io", stackInit: String = ""): String = {
    var stack: String = stackInit
    for (e <- data.flatten) {
      e._2 match {
        case b: Bool => {
          stack += s"      ${e._1} => ${e._1},\n"
        }
        case b: UInt => {
          stack += s"      ${e._1} => ${e._1},\n"
        }
        case _ => stack += "error \n"
      }
    }

    stack
  }
  def instanceClockReset(module: Module): String = {
    var stack: String = ""
  /*  stack += "      clk => clk,\n"
    stack += "      reset => reset,\n"*/
    if (module.clocks.length > 0 || module.resets.size > 0)
      stack += (module.clocks ++ module.resets.values.toList).map(x => "       " + x.name + " => " + x.name + ",\n").reduceLeft(_ + ", " + _)
    stack
  }

}