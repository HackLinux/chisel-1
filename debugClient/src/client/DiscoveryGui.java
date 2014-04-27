package client;

import gnu.io.CommPortIdentifier;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Vector;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

public class DiscoveryGui implements ActionListener{

	JFrame frame;
	SerialPortHandler serialHandler;
    PacketManager packetManager;
	public DiscoveryGui(SerialPortHandler serialHandler, PacketManager packetManager) {
		this.serialHandler = serialHandler;
		this.packetManager = packetManager;
		
		frame = new JFrame("Client");
		// frame.setIconImage((new
		// ImageIcon("org/sump/analyzer/icons/la.png")).getImage());
		Container contentPane = frame.getContentPane();
		contentPane.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridwidth = 1;
		c.gridheight = 2;
		
		JMenuBar mb = new JMenuBar();

		JMenu fileMenu = new JMenu("Setup");
		JMenuItem serialSetup = fileMenu.add("serial");
		serialSetup.addActionListener(this);
		serialSetup.setActionCommand("Setup/serial");
		mb.add(fileMenu);

		frame.setJMenuBar(mb);
		
		
		JButton btConnect = new JButton("Connect");
		btConnect.addActionListener(new ConnectListener());
		contentPane.add(btConnect);
		
		
		
		frame.setSize(400, 300);
		frame.setVisible(true);

		HashMap<String, CommPortIdentifier> portMap = RxTxHelper.getPortMap();

		for (Entry<String, CommPortIdentifier> e : portMap.entrySet()) {
			System.out.println(e.getKey());
		}
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("Setup/serial")) {
			
		}
	}

	class ConnectListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
		
			serialHandler.disconnect();
			

			try {
				serialHandler.connect("COM3");
				//Thread.sleep(20);				
				//sendIdentityRequest();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}/* catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}*/
		}
	}

	
	
	void sendIdentityRequest() throws IOException{
		Vector<Integer> packet = new Vector<Integer>();
		packet.add(0xFF);
		packet.add(0xFF);
		packetManager.txPacket(packet);
	}

	

}
