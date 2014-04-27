package client;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.TooManyListenersException;

public class SerialConnector implements SerialPortEventListener {

	// for containing the ports that will be found
	private Enumeration ports = null;
	// map the port names to CommPortIdentifiers
	private HashMap portMap = new HashMap();

	// this is the object that contains the opened port
	private CommPortIdentifier selectedPortIdentifier = null;
	private SerialPort serialPort = null;

	// input and output streams for sending and receiving data
	private InputStream input = null;
	private OutputStream output = null;

	// just a boolean flag that i use for enabling
	// and disabling buttons depending on whether the program
	// is connected to a serial port or not
	private boolean bConnected = false;

	// the timeout value for connecting with the port
	final static int TIMEOUT = 2000;

	// a string for recording what goes on in the program
	// this string is written to the GUI
	String logText = "";

	public void connect(String selectedPort, int baudrate) {
		selectedPortIdentifier = (CommPortIdentifier) portMap.get(selectedPort);

		CommPort commPort = null;

		try {
			// the method below returns an object of type CommPort
			commPort = selectedPortIdentifier
					.open("TigerControlPanel", TIMEOUT);
			// the CommPort object can be casted to a SerialPort object
			serialPort = (SerialPort) commPort;

			// for controlling GUI elements
			setConnected(true);

			serialPort.setBaudBase(baudrate);

			// Set serial port to 57600bps-8N1..my favourite
			serialPort.setSerialPortParams(baudrate, SerialPort.DATABITS_8,
					SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);

			// logging
			logText = selectedPort + " opened successfully.";

		} catch (PortInUseException e) {
			logText = selectedPort + " is in use. (" + e.toString() + ")";

		} catch (UnsupportedCommOperationException ex) {
			logText = "Bad setup";
			System.err.println(ex.getMessage());
		} catch (Exception e) {
			logText = "Failed to open " + selectedPort + "(" + e.toString()
					+ ")";
		}

	}

	public boolean initIOStream() {
		// return value for whether opening the streams is successful or not
		boolean successful = false;

		try {
			//
			input = serialPort.getInputStream();
			output = serialPort.getOutputStream();
			// writeData(0, 0);

			successful = true;
			return successful;
		} catch (IOException e) {
			logText = "I/O Streams failed to open. (" + e.toString() + ")";

			return successful;
		}
	}

	public void initListener() {
		try {
			serialPort.addEventListener(this);
			serialPort.notifyOnDataAvailable(true);
		} catch (TooManyListenersException e) {
			logText = "Too many listeners. (" + e.toString() + ")";
		}
	}

	public void serialEvent(SerialPortEvent evt) {
		if (evt.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
			try {
				byte singleData = (byte) input.read();

			} catch (Exception e) {
				logText = "Failed to read data. (" + e.toString() + ")";

			}
		}
	}

	public void disconnect() {
		// close the serial port
		try {
			// writeData(0, 0);

			serialPort.removeEventListener();
			serialPort.close();
			input.close();
			output.close();
			setConnected(false);

			logText = "Disconnected.";

		} catch (Exception e) {
			logText = "Failed to close " + serialPort.getName() + "("
					+ e.toString() + ")";

		}
	}

	void setConnected(boolean connected) {

	}
}
