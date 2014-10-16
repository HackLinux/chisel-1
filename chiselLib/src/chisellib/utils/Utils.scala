package chisellib.utils

import scala.collection.mutable.Set
import Chisel._
import scala.collection.mutable.Map

object CounterWidth {
  def apply(t: Double, hz: Double): Int = {
    // println(" counter limit " + (((t - 100e-12) * hz).ceil.toInt))
    return log2Up(((t - 100e-12) * hz).ceil.toInt)
  }
}

object CounterLimit {
  def apply(t: Double, hz: Double): UInt = {
    //  println(" counter limit " + (((t - 100e-12) * hz).ceil.toInt - 1))
    return UInt(((t - 100e-12) * hz).ceil.toInt - 1)
  }
}

object DelayEvent {
  def apply(event: Bool, t: Double, hz: Double): Bool = {
    DelayEvent(event, ((t - 100e-12) * hz).ceil.toInt)
  }

  def apply(event: Bool, cycle: Int): Bool = {
    if (cycle == 0) return event
    val run = RegInit(Bool(false))
    val counter = Reg(UInt(width = log2Up(cycle)))

  //  event.setName("debugEvent");
    counter := counter + UInt(1)

    //  println("!!!! " + cycle)
    val done = counter === UInt(cycle - 1)
    when(done) {
      run := Bool(false)
    }

    when(event) {
      run := Bool(true)
      counter := UInt(0)
    }
   // done.setName("debugDone");
    //run.comp.setName("debugRun");

    return run && done;
  }
}

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
class FlowDeserializer[T <: Data](packetWidth: Integer, dataType: T, clk: Clock = null) extends Module(clk) {
  val io = new Bundle {
    val restart = Bool(INPUT)
    val in = Flow(UInt(OUTPUT, width = packetWidth)).flip
    val out = Flow(dataType).asMaster()
  }
  val inWidth = io.in.bits.getWidth
  val outWidth = io.out.bits.getWidth

  val buffer = if (outWidth > inWidth) Reg(UInt(width = outWidth - inWidth)) else null
  var counterMax = 0;
  if (buffer != null)
    counterMax = (buffer.getWidth - 1) / inWidth + 1
  val counter = if (outWidth > inWidth) RegInit(UInt(0, log2Up(counterMax + 1))) else null
  val flush = Bool()

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
      when(counter === UInt(counterMax)) {
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

object NodeAnalyser {
  //  def getLatency(from: Node, to: Node, max: Integer = 20, topLatency: Integer = 0, walkedNode: Set[Node] = scala.collection.mutable.Set[Node]()): Integer = {
  //    var latency = topLatency;
  //    if (latency == max)
  //      return -1;
  //    if (walkedNode.contains(from)) return -1;
  //    walkedNode += from;
  //
  //    if (from == to) {
  //      return latency;
  //    }
  //
  //    if (from.isInstanceOf[Data]) {
  //      val data = from.asInstanceOf[Data];
  //      for (c <- if (data.comp != null) data.comp.consumers else from.consumers) {
  //        val ret = getLatency(c, to, max, if (c.isInstanceOf[Reg]) latency + 1 else latency, walkedNode);
  //        if (ret != -1)
  //          return ret;
  //      }
  //    } else {
  //      for (c <- from.consumers) {
  //        val ret = getLatency(c, to, max, if (c.isInstanceOf[Reg]) latency + 1 else latency, walkedNode);
  //        if (ret != -1)
  //          return ret;
  //      }
  //    }
  //    return -1;
  //  }

  var trolololCounter = 0
  def getLatency(from: Node, to: Node, max: Integer = 100, topLatency: Integer = 0, walkedNode: Set[Node] = scala.collection.mutable.Set[Node]()): Integer = {
    trolololCounter = 0
    var from_ = from;
    if (from.isInstanceOf[Data]) {
      val data = from.asInstanceOf[Data];
      if (data.comp != null)
        from_ = data.comp;
    }
    val ret = getLatency_(from_, to, max);
    if (ret == -1)
      println("************** get latencyFaild !***********")
    // println("trololol " + trolololCounter)
    ret
  }
  //  def getLatency_(from: Node, to: Node, max: Integer = 20, topLatency: Integer = 0, walkedNode: Set[Node] = scala.collection.mutable.Set[Node]()): Integer = {
  //    var latency = topLatency;
  //    if (walkedNode.contains(to)) return -1;
  //    walkedNode += to;
  //    if (from == to) {
  //      return from match {
  //        case ram: MemWrite => latency - 1
  //        case _: Reg => latency - 1
  //        case _ => latency
  //      }
  //    }
  //
  //    for (c <- from.consumers) {
  //      val ret = getLatency_(c, to, max, if (c.isInstanceOf[Reg] || c.isInstanceOf[MemWrite]) latency + 1 else latency, walkedNode);
  //      if (ret != -1)
  //        return ret;
  //    }
  //    return -1;
  //  }
  //    def getLatency_(from: Node, to: Node, max: Integer = 20, topLatency: Integer = 0, walkedNode: Set[Node] = scala.collection.mutable.Set[Node]()): Integer = {
  //      var latency = topLatency;
  //      if (walkedNode.contains(to)) return -1;
  //      walkedNode += to;
  //      if (from == to) {
  //        return from match {
  //          case ram: MemWrite => latency - 1
  //          case _: Reg => latency - 1
  //          case _ => latency
  //        }
  //      }
  //  
  //      for (c <- to.inputs) {
  //        val ret = getLatency_(from, c, max, if (c.isInstanceOf[Reg] || c.isInstanceOf[MemWrite]) latency + 1 else latency, walkedNode);
  //        if (ret != -1)
  //          return ret;
  //      }
  //      return -1;
  //    }

  def getLatency_(from: Node, to: Node, depthMax: Integer, depth: Integer = 0, walkedView: Map[Node, Int] = Map[Node, Int]()): Integer = {
    //  trolololCounter = trolololCounter + 1
    val pastDepth: Int = walkedView.getOrElse(to, depthMax)
    if (depth >= pastDepth) {
      return -1;
    }
    if (depth >= depthMax)
      return -1;
    walkedView += (to -> depth)

    if (from == to) {
      return 0;
    }

    var best = -1;
    for (c <- to.inputs) {
      val ret = getLatency_(from, c, depthMax, depth + 1, walkedView);
      if (ret != -1)
        if (best == -1)
          best = ret
        else
          best = Math.min(best, ret)
    }

    if (best != -1)
      if (to.isInstanceOf[Reg] || to.isInstanceOf[MemWrite]) best = best + 1

    return best;
  }
}