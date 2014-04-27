package client;

import gnu.io.SerialPortEvent;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Vector;

public class PacketManager implements ISerialPortObserver {

	SerialPortHandler serialPort;

	public static final int specialSymbole = 0x60;
	public static final int endSymbole = 0x61;
	public static final int xSpSymbole = 0x62;
	
	
	public PacketManager(SerialPortHandler serialPort) {
		this.serialPort = serialPort;
		serialPort.setObserver(this);
	}

	public void txPacket(Vector<Integer> packet) {
		for (Integer i : packet) {
			serialPort.tx(i);
			if (i == specialSymbole) {
				serialPort.tx(xSpSymbole);
			}
		}
		serialPort.tx(specialSymbole);
		serialPort.tx(endSymbole);
	}

	int rxLast = specialSymbole + 1;
	Vector<Integer> rxBuffer = new Vector<Integer>();

	public void rxByte(int b) {
		int rxLast = this.rxLast;
		this.rxLast = b;
		if (rxLast == specialSymbole) {
			switch (b) {
			case endSymbole:
				rxPacket(rxBuffer);
				break;
			case xSpSymbole:
				rxBuffer.add(specialSymbole);
				break;
			}
		} else if (b != specialSymbole) {
			rxBuffer.add(b);
		}
	}

	void rxPacket(Vector<Integer> packet) {

	}
}
