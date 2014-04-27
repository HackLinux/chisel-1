package client;

public class Main {

	public static void main(String[] args) {
		SerialPortHandler serialHandler = new SerialPortHandler();
	    PacketManager packetManager = new PacketManager(serialHandler);;
		
		new DiscoveryGui(serialHandler,packetManager);
	}

}
