package client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.TooManyListenersException;
import java.util.Vector;

import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;

public class SerialPortHandler implements SerialPortEventListener {
	private SerialPort serialPort;
	private OutputStream outStream;
	private InputStream inStream;

	public void connect(String portName) throws IOException {
		try {
			// Obtain a CommPortIdentifier object for the port you want to open
			CommPortIdentifier portId = CommPortIdentifier
					.getPortIdentifier(portName);

			// Get the port's ownership
			serialPort = (SerialPort) portId.open("Demo application", 5000);

			// Set the parameters of the connection.
			setSerialPortParameters();

			// Open the input and output streams for the connection. If they
			// won't
			// open, close the port before throwing an exception.
			outStream = serialPort.getOutputStream();
			inStream = serialPort.getInputStream();
		} catch (NoSuchPortException e) {
			throw new IOException(e.getMessage());
		} catch (PortInUseException e) {
			throw new IOException(e.getMessage());
		} catch (IOException e) {
			serialPort.close();
			throw e;
		}
	}

	/**
	 * Get the serial port input stream
	 * 
	 * @return The serial port input stream
	 */
	public InputStream getSerialInputStream() {
		return inStream;
	}

	/**
	 * Get the serial port output stream
	 * 
	 * @return The serial port output stream
	 */
	public OutputStream getSerialOutputStream() {
		return outStream;
	}

	/**
	 * Sets the serial port parameters
	 */
	private void setSerialPortParameters() throws IOException {
		int baudRate = 57600; // 57600bps

		try {
			// Set serial port to 57600bps-8N1..my favourite
			serialPort.setInputBufferSize(1024 * 1024);
			serialPort.setOutputBufferSize(1024 * 1024);
			serialPort.setSerialPortParams(baudRate, SerialPort.DATABITS_8,
					SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);

			serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);

			serialPort.addEventListener(this);
			serialPort.notifyOnDataAvailable(true);
		} catch (UnsupportedCommOperationException ex) {
			throw new IOException("Unsupported serial port parameter");
		} catch (TooManyListenersException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void disconnect() {
		try {
			serialPort.removeEventListener();
			serialPort.close();
			inStream.close();
			outStream.close();
		} catch (Exception e) {

		}
	}

	ISerialPortObserver observer;
	void setObserver(ISerialPortObserver observer){
		this.observer = observer;
	}
	
	public static final int specialSymbole = 0x60;
	public static final int endSymbole = 0x61;
	public static final int xSpSymbole = 0x62;

	public void tx(int b) {
		try {
			outStream.write(b);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void serialEvent(SerialPortEvent e) {
		try {
			int b;
			while ((b = inStream.read()) != -1) {
				if(observer != null) observer.rxByte(b);
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

}