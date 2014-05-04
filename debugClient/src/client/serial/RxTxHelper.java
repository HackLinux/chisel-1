package client.serial;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.HashMap;

public class RxTxHelper {

	public static HashMap<String, CommPortIdentifier> getPortMap() {
		HashMap<String, CommPortIdentifier> map = new HashMap<String, CommPortIdentifier>();

		Enumeration ports = CommPortIdentifier.getPortIdentifiers();

		while (ports.hasMoreElements()) {
			CommPortIdentifier curPort = (CommPortIdentifier) ports
					.nextElement();

			// get only serial ports
			if (curPort.getPortType() == CommPortIdentifier.PORT_SERIAL) {
				// 0window.cboxPorts.addItem(curPort.getName());
				map.put(curPort.getName(), curPort);
			}
		}
		return map;
	}

}