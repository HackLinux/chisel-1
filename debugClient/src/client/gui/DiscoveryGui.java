package client.gui;

import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Vector;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.WindowConstants;

import client.serial.PacketManager;
import client.serial.RxTxHelper;
import client.serial.SerialPortHandler;

public class DiscoveryGui implements ActionListener {

	JFrame frame;
	SerialPortHandler serialHandler;
	PacketManager packetManager;

	JComboBox<String> cbCom;
	String[] parityList = { "none", "odd", "even" };
	JComboBox<String> cbParity;
	JComboBox<Integer> cbStop;
	JComboBox<Integer> cbSpeed;

	public JButton btScan;
	public JList<String> nodeList;
	public DefaultListModel<String> nodes;

	public DiscoveryGui(SerialPortHandler serialHandler, PacketManager packetManager) {
		this.serialHandler = serialHandler;
		this.packetManager = packetManager;

		frame = new JFrame("Client");
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

		GridLayout lConfig = new GridLayout(0, 2);
		lConfig.setHgap(10);
		lConfig.setVgap(5);
		JPanel pConfig = new JPanel(lConfig);

		{
			pConfig.add(new JLabel(" Com "));
			String[] comList = {};
			cbCom = new JComboBox<String>(comList);
			cbCom.addFocusListener(new ComListener());
			pConfig.add(cbCom);

			pConfig.add(new JLabel(" Baudrate "));
			Integer[] speedList = { 1200, 2400, 4800, 9600, 19200, 38400, 57600, 115200 };
			cbSpeed = new JComboBox<Integer>(speedList);
			cbSpeed.setSelectedIndex(7);
			pConfig.add(cbSpeed);

			pConfig.add(new JLabel(" Parity "));
			String[] parityList = { "none", "odd", "even" };
			cbParity = new JComboBox<String>(parityList);
			cbParity.setSelectedIndex(0);
			pConfig.add(cbParity);

			pConfig.add(new JLabel(" Stop "));
			Integer[] stopList = { 1, 2 };
			cbStop = new JComboBox<Integer>(stopList);
			cbStop.setSelectedIndex(0);
			pConfig.add(cbStop);

			JButton btConnect = new JButton("Connect");
			btConnect.addActionListener(new ConnectListener());
			pConfig.add(btConnect);

			btScan = new JButton("Scan");
			refreshComComboBox();
			pConfig.add(btScan);
		}

		nodes = new DefaultListModel<String>();
		nodeList = new JList<String>(nodes);
		nodeList.setLayoutOrientation(JList.VERTICAL);
		JScrollPane listScroller = new JScrollPane(nodeList);
		listScroller.setPreferredSize(new Dimension(250, 20));
		nodes.addElement("un");
		nodes.addElement("deux");

		JPanel top = new JPanel();
		top.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		top.setLayout(new BoxLayout(top, BoxLayout.LINE_AXIS));
		top.add(pConfig);
		top.add(listScroller);

		frame.getContentPane().add(top);
		frame.pack();
		frame.setResizable(false);
		frame.setVisible(true);

	}

	public void refreshComComboBox() {
		cbCom.removeAllItems();
		for (String i : RxTxHelper.getPortMap().keySet()) {
			cbCom.addItem(i);
		}
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("Setup/serial")) {

		}
	}

	class ConnectListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			try {
				int parity = 0;
				if (cbParity.getSelectedItem().equals("none"))
					parity = SerialPort.PARITY_NONE;
				if (cbParity.getSelectedItem().equals("even"))
					parity = SerialPort.PARITY_EVEN;
				if (cbParity.getSelectedItem().equals("odd"))
					parity = SerialPort.PARITY_ODD;
				serialHandler.connect((String) cbCom.getSelectedItem(), (Integer) cbSpeed.getSelectedItem(), parity, (Integer) cbStop.getSelectedItem());

			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}

	class ComListener implements FocusListener {

		public void focusGained(FocusEvent e) {
			refreshComComboBox();
		}

		public void focusLost(FocusEvent e) {

		}
	}
}

/*
 * JMenuBar mb = new JMenuBar();
 * 
 * JMenu fileMenu = new JMenu("Setup"); JMenuItem serialSetup =
 * fileMenu.add("serial"); serialSetup.addActionListener(this);
 * serialSetup.setActionCommand("Setup/serial"); mb.add(fileMenu);
 * 
 * frame.setJMenuBar(mb);
 */