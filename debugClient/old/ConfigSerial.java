package client;

import gnu.io.CommPortIdentifier;

import java.awt.BorderLayout;
import java.awt.Container;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

public class ConfigSerial {
	/*JFrame frame;
	public ConfigSerial() {
		frame = new JFrame("Serial config");
		// frame.setIconImage((new
		// ImageIcon("org/sump/analyzer/icons/la.png")).getImage());
		Container contentPane = frame.getContentPane();
		contentPane.setLayout(new BorderLayout());

		JMenuBar mb = new JMenuBar();

		JMenu fileMenu = new JMenu("Setup");
		JMenuItem serialSetup = fileMenu.add("serial");
		serialSetup.addActionListener(this);
		serialSetup.setActionCommand("Setup/serial");
		mb.add(fileMenu);

		frame.setJMenuBar(mb);
		frame.setSize(400, 300);
		frame.setVisible(true);

		HashMap<String, CommPortIdentifier> portMap = RxTxHelper.getPortMap();

		for (Entry<String, CommPortIdentifier> e : portMap.entrySet()) {
			System.out.println(e.getKey());
		}
	}*/
}
