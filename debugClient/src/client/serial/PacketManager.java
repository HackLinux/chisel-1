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
	// public static final int ackSymbole = 0x03;

	public static final int identityHeader = 0xFF;

	public PacketManager(SerialPortHandler serialPort) {
		this.serialPort = serialPort;
		serialPort.setObserver(this);
	}

	public void txPacket(LinkedList<Integer> packet) {
		serialPort.tx(specialSymbole);
		serialPort.tx(startSymbole);
		for (Integer i : packet) {
			serialPort.tx(i);
			if (i == specialSymbole) {
				serialPort.tx(xSpSymbole);
			}
		}
		serialPort.tx(specialSymbole);
		serialPort.tx(endSymbole);
		serialPort.tx(1);
		serialPort.tx(2);
	}

	boolean rxLastIsSpecial = false;
	LinkedList<Integer> rxBuffer;

	static public enum RxState {
		idle, data, crc1, crc2
	}

	RxState rxState = RxState.idle;
	int rxCrc;
	
	public void rxByte(int b) {
		if (b == specialSymbole) {
			rxLastIsSpecial = true;
			return;
		}
		if (rxLastIsSpecial) {
			rxLastIsSpecial = false;
			switch (b) {
			case startSymbole:
				rxBuffer = new LinkedList<Integer>();
				rxState = RxState.data;
				return;
			case endSymbole:
				if (rxState == RxState.data)
					rxState = RxState.crc1;
				else
					rxState = RxState.idle;
				return;
			case xSpSymbole:
				b = specialSymbole;
				break;
			}
		}

		switch (rxState) {
		case data:
			rxBuffer.add(b);
			break;
		case crc1:
			rxCrc = b;
		case crc2:
			rxCrc /= b <<8;
			rxPacket(rxBuffer);
			rxState = RxState.idle;
			break;
		default:
			rxState = RxState.idle;
			break;
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
