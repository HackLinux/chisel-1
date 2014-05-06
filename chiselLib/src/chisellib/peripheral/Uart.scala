package chisellib.peripheral

import Chisel._
import chisellib.utils._

//Enums
object UartConfig {
  //Enums
  val eData7bit :: eData8bit :: eData9bit :: Nil = Enum(UInt(), 3)
  val eStop1bit :: eStop2bit :: Nil = Enum(UInt(), 2)
  val eParityNone :: eParityEven :: eParityOdd :: Nil = Enum(UInt(), 3)

  def DataType = UInt(width = eData7bit.getWidth)
  def StopType = UInt(width = eStop1bit.getWidth)
  def ParityType = UInt(width = eParityNone.getWidth)

  def dataTypeToBitCount(dataType: UInt): UInt = {
    val ret = UInt(width = 4)
    ret := UInt(8)
    switch(dataType) {
      is(eData7bit) {
        ret := UInt(7)
      }
      is(eData8bit) {
        ret := UInt(8)
      }
      is(eData9bit) {
        ret := UInt(9)
      }
    }
    return ret
  }

  def stopTypeToBitCount(dataType: UInt) : UInt = {
    val ret = UInt(width = 12)
    ret := UInt(1)
    switch(dataType) {
      is(eStop1bit) {
        ret := UInt(1)
      }
      is(eStop2bit) {
        ret := UInt(2)
      }
    }
    return ret;
  }
}

class UartConfig extends Bundle {
  val dataType = UartConfig.DataType;
  val stopType = UartConfig.StopType;
  val parityType = UartConfig.ParityType;
  val clockDivider = UInt(width = 20);
}

class UartRxData extends Bundle {
  val data = UInt(width = 8);
  val error = Bool();
}

class UartRx extends Module {
  val io = new Bundle {
    val config = new UartConfig().asInput(); //Config
    val rx = Bool(INPUT);
    val data = Flow(new UartRxData).asMaster();
  }

  //Logic
  //====================================

  //Input sync
  val sRxBuffer = RegInit(Bool(true));
  val sRx = RegInit(Bool(true));
  sRx := sRxBuffer;
  sRxBuffer := io.rx;

  //Uart sampler
  val sCounter = Reg(io.config.clockDivider.clone()); //Same width than periodCounterPrecharge //Signal
  val sEvStart = Bool() //Combinatoire !
  val sEvNewBit = Bool() //..

  sCounter := sCounter - UInt(1);

  sEvStart := Bool(false)
  when(sEvStart) {
    sCounter := (io.config.clockDivider) >> UInt(1);
  }

  sEvNewBit := Bool(false)
  when(sCounter === UInt(0)) {
    sEvNewBit := Bool(true);
    sCounter := io.config.clockDivider;
  }

  //Uart SM
  val stIdle :: stStartBit :: stDataBit :: stParity :: stStopBit :: Nil = Enum(UInt(), 5) //SM states
  val sState = RegInit(stIdle);
  val sBitIdx = Reg(UInt(width = 4));
  val sSum = Reg(UInt(width = 1));

  val soData = Reg(io.data.bits.data.clone());
  io.data.valid := Bool(false);
  io.data.bits.data := soData;
  io.data.bits.error := Bool(false);

  when(sEvNewBit) {
    sBitIdx := sBitIdx + UInt(1);
  }

  soData := soData; //Because Chisel is a fucking language

  switch(sState) {
    is(stIdle) {
      when(sRx === Bool(false)) { // :==== when(~sRx)
        sState := stStartBit;
        sEvStart := Bool(true);
      }
    }
    is(stStartBit) {
      when(sEvNewBit === Bool(true)) { // when(sEvNewHalfBit)
        when(sRx === Bool(false)) {
          sState := stDataBit;
          sBitIdx := UInt(0); //LSB first
        } otherwise {
          sState := stIdle;
        }
        sSum := (io.config.parityType === UartConfig.eParityOdd).toUInt();
      }
    }
    is(stDataBit) {
      when(sEvNewBit) {
        soData := soData.bitSet(sBitIdx, sRx);
        sSum := sSum ^ sRx;
        when(sBitIdx === UInt(7)) {
          when(io.config.parityType === UartConfig.eParityNone) {
            sState := stStopBit;
            sBitIdx := UInt(0);
          } otherwise {
            sState := stParity;
          }
        }
      }
    }
    is(stParity) {
      when(sEvNewBit) {
        sSum := sSum ^ sRx;
        sState := stStopBit;
        sBitIdx := UInt(0);
      }
    }
    is(stStopBit) {
      when(io.config.parityType != UartConfig.eParityNone) {
        io.data.bits.error := (sSum === (UInt(0)));
      }
      when(sEvNewBit) {
        when(sRx === Bool(false)) {
          io.data.valid := Bool(true)
          io.data.bits.error := Bool(true);
          sState := stIdle;
        }
        when(((sBitIdx === UInt(0)) && (io.config.stopType === UartConfig.eStop1bit)) ||
          ((sBitIdx === UInt(1)) && (io.config.stopType === UartConfig.eStop2bit))) {
          io.data.valid := Bool(true)
          sState := stIdle;
        }
      }
    }
  }

}

class UartTx extends Module {
  val io = new Bundle {
    val config = new UartConfig().asInput(); //Config
    val data = Stream(UInt(width = 9)).asSlave();
    val tx = Bool(OUTPUT);
  }

  io.data.ready := Bool(false);
  val tx = RegInit(Bool(true));
  io.tx := tx

  //Uart sampler
  val sCounter = Reg(io.config.clockDivider.clone()); //Same width than periodCounterPrecharge //Signal
  val sEvStart = Bool() //Combinatoire !
  val sEvNewBit = Bool() //..

  sCounter := sCounter - UInt(1);

  sEvStart := Bool(false)
  when(sEvStart) {
    sCounter := io.config.clockDivider;
  }

  sEvNewBit := Bool(false)
  when(sCounter === UInt(0)) {
    sEvNewBit := Bool(true);
    sCounter := io.config.clockDivider;
  }

  val stIdle :: stStartBit :: stDataBit :: stParity :: stStopBit :: Nil = Enum(UInt(), 5) //SM states
  val sState = RegInit(stIdle);
  val lockingForJob = Bool()
  val sBitIdx = Reg(UInt(width = 4));
  val sSum = Reg(UInt(width = 1));
  val dataBitCount = UartConfig.dataTypeToBitCount(io.config.dataType)
  val stopBitCount = UartConfig.stopTypeToBitCount(io.config.stopType)

  lockingForJob := Bool(false)

  when(sEvNewBit === Bool(true)) {
    switch(sState) {
      is(stStartBit) {
        sState := stDataBit
        sBitIdx := UInt(0)
        sSum := io.config.parityType === UartConfig.eParityOdd
      }
      is(stDataBit) {
        sSum := sSum ^ io.data.bits(sBitIdx);
        when(sBitIdx != dataBitCount - UInt(1)) {
          sBitIdx := sBitIdx + UInt(1)
        } otherwise {
          sBitIdx := UInt(0);
          when(io.config.parityType === UartConfig.eParityNone) {
            sState := stStopBit
          } otherwise {
            sState := stParity
          }
        }
      }
      is(stParity) {
        sState := stStopBit
      }
      is(stStopBit) {
        when(sBitIdx != stopBitCount - UInt(1)) {
          sBitIdx := sBitIdx + UInt(1)
        } otherwise {
          sState := stIdle
          io.data.deq()
          lockingForJob := Bool(true)
        }
      }
    }
  }
  
  when(sState === stIdle) {
    lockingForJob := Bool(true)
  }

  when(lockingForJob) {
    when(io.data.valid) {
      sState := stStartBit
      sEvStart := Bool(true);
    }
  }

  //IO
  switch(sState) {
    is(stIdle) {
      tx := Bool(true)
    }
    is(stStartBit) {
      tx := Bool(false)
    }
    is(stDataBit) {
      tx := io.data.bits(sBitIdx)
    }
    is(stParity) {
      tx := sSum
    }
    is(stStopBit) {
      tx := Bool(true)
    }
  }
}

object Uart {
  def main(args: Array[String]) {

    chiselMain(Array[String]("--backend", "v" /*,"--genHarness"*/ ),
      () => Module(new UartTx()))

  }

}
