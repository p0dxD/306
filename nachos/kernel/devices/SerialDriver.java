// SerialDriver.java
//
//	Test driver that operates serial ports in PIO mode.
//	Students will replace this with a bona fide interrupt-driven version.
//
// Copyright (c) 2010 State University of New York at Stony Brook.
// All rights reserved.  See the COPYRIGHT file for copyright notice and 
// limitation of liability and disclaimer of warranty provisions.

package nachos.kernel.devices;

import nachos.Debug;
import nachos.machine.SerialPort;
import nachos.machine.InterruptHandler;
import nachos.kernel.Nachos;
import java.net.SocketAddress;

/**
 * Driver for serial port devices.
 *
 * @author Eugene W. Stark
 * @version 20100720
 */
public class SerialDriver {

    /** SerialPort device units. */
    private SerialPort[] units = new SerialPort[SerialPort.NUM_UNITS];

    /**
     * Initialize the driver.
     */
    public SerialDriver() {
	// Nothing for now.  The units are initialized individually
	// on demand via openPort().
    }

    /**
     * Shut down the driver and the underlying devices.
     */
    public void stop() {
	for(int i = 0; i < units.length; i++)
	    closePort(i);
    }

    /**
     * Open a specified serial port and ready it for use.
     * By default, the port is in loopback mode, in which the
     * transmit and receive sides are "wired together".
     *
     * @param i The unit number of the port to open.
     */
    public void openPort(int i) {
	SerialPort unit = SerialPort.getUnit(i);
	units[i] = unit;
	unit.writeMCR(SerialPort.MCR_DTR | SerialPort.MCR_RTS);
	Debug.print('p', "Serial port " + i + " open at address "
		    + unit.getLocalAddress() + "\n");
    }

    /**
     * Close a previously opened port.
     *
     * @param i The unit number of the port to close.
     */
    public void closePort(int i) {
	SerialPort unit = units[i];
	if(unit != null) {
	    unit.writeMCR(0);
	    unit.stop();
	    units[i] = null;
	}
    }

    /**
     * Connect the wire attached to a specified unit number to a remote
     * port located at a specified address.
     * 
     * @param i The unit number of the port to be connected.
     * @param addr  The address of the remote port to connect to,
     * or null to set the specified unit into loopback mode.
     */
    public void connectPort(int i, SocketAddress addr) {
	units[i].connectTo(addr);
    }

    /**
     * Send a byte of data over a serial port.
     * The calling thread spins in a polling loop until the port is ready
     * to accept the data to be transmitted.
     *
     * @param i The unit number of the port to use.
     * @param data The data byte to be transmitted.
     */
    public void putByte(int i, byte data) {
	SerialPort unit = units[i];
	while((unit.readLSR() & SerialPort.LSR_TRDY) == 0)
	    Nachos.scheduler.yieldThread();
	unit.writeTDR(data);
    }

    /**
     * Wait for a byte of data to be received over the serial port
     * and return it.  The calling thread spins in a polling loop until
     * a byte of data is available.
     *
     * @param i The unit number of the port to use.
     * @return  The byte of data received.
     */
    public byte getByte(int i) {
	SerialPort unit = units[i];
	while((unit.readLSR() & SerialPort.LSR_RRDY) == 0)
	    Nachos.scheduler.yieldThread();
	byte data = unit.readRDR();
	return data;
    }

    /**
     * SerialDriver interrupt handler class (sample: not currently used).
     */
    private class SerialIntHandler implements InterruptHandler {

	private int unit;

	public SerialIntHandler(int unit) {
	    this.unit = unit;
	}

	public void handleInterrupt() {
	    Debug.println('s', "Serial interrupt: unit #" + unit);
	    units[unit].showState();
	}
    }
}
