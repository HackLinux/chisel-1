package client.serial;

import gnu.io.SerialPortEvent;

import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedList;

public class PacketManager implements ISerialPortObserver {

	SerialPortHandler serialPort;
	
	IPacketListener identityObserver;

	public static final int specialSymbole = 0xAA;
	public static final int xSpSymbole = 0x00;
	public static final int startSymbole = 0x01;
	public static final int endSymbole = 0x02;
	public static final int ackSymbole = 0x03;

	public static final int identityHeader = 0xFF;

	
	public PacketManager(SerialPortHandler serialPort) {
		this.serialPort = serialPort;
		serialPort.setObserver(this);
	}

	public void txPacket(LinkedList<Integer> packet) {
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
	LinkedList<Integer> rxBuffer = new LinkedList<Integer>();

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

	void rxPacket(LinkedList<Integer> packet) {
		if (packet.size() == 0)
			return;
		switch (packet.pop()) {
		case identityHeader:
			identityObserver.rxPacket(packet);
			break;
		default:
			break;
		}
	}
}
