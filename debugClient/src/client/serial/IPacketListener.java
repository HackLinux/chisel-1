package client.serial;

import java.util.LinkedList;


public interface IPacketListener {
	void rxPacket(LinkedList<Integer> packet);
}
