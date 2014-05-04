package client;

import client.gui.DiscoveryGui;
import client.gui.IdentityListener;
import client.serial.PacketManager;
import client.serial.SerialPortHandler;

public class Main {

	public static void main(String[] args) {
		SerialPortHandler serialHandler = new SerialPortHandler();
	    PacketManager packetManager = new PacketManager(serialHandler);;
		
	    DiscoveryGui discoveryGui = new DiscoveryGui(serialHandler,packetManager);
	    IdentityListener identityListener = new IdentityListener(discoveryGui,packetManager);


	}

}
