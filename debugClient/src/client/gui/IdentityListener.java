package client.gui;

import java.awt.List;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.Vector;

import client.serial.IPacketListener;
import client.serial.PacketManager;

public class IdentityListener implements IPacketListener {

	DiscoveryGui gui;
	PacketManager packetManager;
	public IdentityListener(DiscoveryGui gui,PacketManager packetManager) {
		this.gui = gui;
		this.packetManager = packetManager;
		
		gui.btScan.addActionListener(new ScanListener());
	}

	public void refresh() {
		gui.nodes.removeAllElements();
		LinkedList<Integer> packet = new LinkedList<Integer>();
		packet.add(0xFF);
		packet.add(0xFF);
		packetManager.txPacket(packet);
	}

	public void rxPacket(LinkedList<Integer> packet) {
		gui.nodes.addElement("ASD");
	}
	
	class ScanListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			refresh();
		}
	}
}
