// SerialTest.java
//
//	Test program to demonstrate the operation of serial ports.
//
// Copyright (c) 2010 State University of New York at Stony Brook.
// All rights reserved.  See the COPYRIGHT file for copyright notice and 
// limitation of liability and disclaimer of warranty provisions.

package nachos.kernel.devices.test;

import nachos.Debug;
import nachos.machine.Machine;
import nachos.machine.NachosThread;
import nachos.kernel.Nachos;
import nachos.kernel.threads.Scheduler;
import nachos.kernel.devices.SerialDriver;

/**
 * Loopback test for serial port.
 * Creates a sender thread and a receiver thread.
 * Sender sends several bytes of data, receiver receives them.
 *
 * @author Eugene W. Stark
 * @version 20100720
 */
public class SerialTest {
    
    /** Reference to serial port driver. */
    private static SerialDriver driver;

    /** Array of bytes to be transmitted. */
    private static byte[] data = new byte[] { 'H', 'E', 'L', 'L', 'O', 0x0 };

    /**
     * Entry point for the serial test.
     */
    public static void start() {
	Debug.println('p', "Entering SerialTest");
	driver = Nachos.serialDriver;
	driver.openPort(0);

	NachosThread sender =
	    new NachosThread
	    ("Sender thread",
	     new Runnable() {
		    public void run() {
			Debug.print('p', "Sender starting\n");
			for(int i = 0; i < data.length; i++) {
			    byte b = data[i];
			    driver.putByte(0, b);
			    Debug.printf('p', "Sender: sent 0x%x ('%c')\n",
					 b, b);
			}
			Debug.print('p', "Sender terminating\n");
			Nachos.scheduler.finishThread();
		    }
		});

	NachosThread receiver =
	    new NachosThread
	    ("Receiver thread",
	     new Runnable() {
		    public void run() {
			Debug.print('p', "Receiver starting\n");
			byte b = 0;
			do {
			    b = driver.getByte(0);
			    Debug.printf('p', "Receiver: got 0x%x ('%c')\n",
					 b, b);
			} while(b != 0);
			driver.closePort(0);
			Debug.printf('p', "Receiver terminating\n", b);
			Nachos.scheduler.finishThread();
		    }
		});

	Nachos.scheduler.readyToRun(sender);
	Nachos.scheduler.readyToRun(receiver);
    }
}
