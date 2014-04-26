package main

import Chisel._

object Main {
  def main(args: Array[String]) {

    chiselMain(Array[String]("--backend", "fpga"),
   // chiselMain(Array[String]("--backend", "v","--genHarness"),
        () => Module(new Test))
    /*  var m = Module(new Top(new Parameter(8, 1280, 1024, 7, 18, 28)))

    VHDL.createIpFile(m)*/

  }

}