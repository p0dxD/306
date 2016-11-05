// Copyright (c) 1992-1993 The Regents of the University of California.
// Copyright (c) 1998 Rice University.
// Copyright (c) 2003 State University of New York at Stony Brook.
// All rights reserved.  See the COPYRIGHT file for copyright notice and
// limitation of liability and disclaimer of warranty provisions.

package nachos.kernel.userprog;

import java.util.HashMap;

import nachos.Debug;
import nachos.kernel.Nachos;
import nachos.kernel.filesys.OpenFile;
import nachos.kernel.threads.Condition;
import nachos.kernel.threads.Lock;
import nachos.kernel.threads.Semaphore;
import nachos.kernel.threads.SpinLock;
import nachos.machine.CPU;
import nachos.machine.MIPS;
import nachos.machine.Machine;
import nachos.machine.NachosThread;
import nachos.machine.Simulation;
import nachos.machine.TranslationEntry;

/**
 * Nachos system call interface.  These are Nachos kernel operations
 * that can be invoked from user programs, by trapping to the kernel
 * via the "syscall" instruction.
 *
 * @author Thomas Anderson (UC Berkeley), original C++ version
 * @author Peter Druschel (Rice University), Java translation
 * @author Eugene W. Stark (Stony Brook University)
 */
public class Syscall {

    // System call codes -- used by the stubs to tell the kernel 
    // which system call is being asked for.

    /** Integer code identifying the "Halt" system call. */
    public static final byte SC_Halt = 0;

    /** Integer code identifying the "Exit" system call. */
    public static final byte SC_Exit = 1;

    /** Integer code identifying the "Exec" system call. */
    public static final byte SC_Exec = 2;

    /** Integer code identifying the "Join" system call. */
    public static final byte SC_Join = 3;

    /** Integer code identifying the "Create" system call. */
    public static final byte SC_Create = 4;

    /** Integer code identifying the "Open" system call. */
    public static final byte SC_Open = 5;

    /** Integer code identifying the "Read" system call. */
    public static final byte SC_Read = 6;

    /** Integer code identifying the "Write" system call. */
    public static final byte SC_Write = 7;

    /** Integer code identifying the "Close" system call. */
    public static final byte SC_Close = 8;

    /** Integer code identifying the "Fork" system call. */
    public static final byte SC_Fork = 9;

    /** Integer code identifying the "Yield" system call. */
    public static final byte SC_Yield = 10;

    /** Integer code identifying the "Remove" system call. */
    public static final byte SC_Remove = 11;
    
    /** Integer code identifying the "PredictCPU" system call.*/
    public static final byte SC_PredictCPU = 12;
    
    /**Contains temporary exit status */
    static HashMap<Integer, Integer> exitStatus = new HashMap<>();
    
    /**Contains temporary processes information */
    static HashMap<Integer, ProcessInformation> processes = new HashMap<>();
    
    /**Lock for accessing processes and exit status a */
    static private final SpinLock lockStatus = new SpinLock("Lock on the status");
    
    static private final SpinLock lockProcess = new SpinLock("Lock on the process");

    /**
     * Class to hold information about exec process
     */
     private class ProcessInformation{
	int parentID;

	private Semaphore sem = new Semaphore("Join lock",0);
	public ProcessInformation(int parentID){
	    this.parentID = parentID;

	}
	
	public Semaphore getSemaphore(){
	    return sem;
	}
	
	public int getParentID(){
	    return parentID;
	}
    }

    
    /**
     * Stop Nachos, and print out performance stats.
     */
    public static void halt() {
	Debug.print('+', "Shutdown, initiated by user program.\n");
	Simulation.stop();
    }

    /* Address space control operations: Exit, Exec, and Join */

    /**
     * This user program is done.
     *
     * @param status Status code to pass to processes doing a Join().
     * status = 0 means the program exited normally.
     */
    public static void exit(int status) {
	Debug.println('+', "User program exits with status=" + status
				+ ": " + NachosThread.currentThread().name);
	
	AddrSpace space = ((UserThread)NachosThread.currentThread()).space;
	MemManager memManager = MemManager.getInstance();
	memManager.cleanProgram(space);
	
	if(containsProcess(space)){

	    Debug.println('S', "Calling V on exiting sempahore for join."
		    +processes.size() +" SIZE OF exit "+exitStatus.size());

	    //store the status of this for the parent to collect if he joins,
	    //else if is discarded for now
	    int parentId = getParentId(space);
	    if(memManager.containsAddress(parentId)){
		addExitStatus(space,status);
	    }
	    removeAndReleaseProcessSem( space);
	}
	//if for some reason parent leaves without joining lets discard the status
	discardStatus(space);
	memManager.finishAddrs(space);
    }

    /**
     * Run the executable, stored in the Nachos file "name", and return the 
     * address space identifier.
     *
     * @param name The name of the file to execute.
     */
    public static int exec(String name) {
	Debug.println('S', "User asked to exec " + name);
	MemManager memManager = MemManager.getInstance();
	AddrSpace space = new AddrSpace();
	
	UserThread t = new UserThread(name, new Runnable(){
	    public void run(){
		Debug.println('S', "Exec running " + name);

		OpenFile executable;
		AddrSpace space_child = ((UserThread)NachosThread.currentThread()).space;
		
		if((executable = Nachos.fileSystem.open(name)) == null) {
		    Debug.println('+', "Unable to open executable file " + name);
		    addExitStatus(space_child, -1);
		    removeAndReleaseProcessSem(space_child);
		    memManager.finishAddrs(space_child);
		    return;
		}

		if(space.exec(executable) == -1) {
		    Debug.println('+', "Unable to read executable file " + name);
		    addExitStatus(space_child, -1);
		    removeAndReleaseProcessSem(space_child);
		    memManager.finishAddrs(space_child);
		    return;
		}

		space.initRegisters();		// set the initial register values
		space.restoreState();		// load page table register

		CPU.runUserCode();			// jump to the user progam
		Debug.ASSERT(false);		// machine->Run never returns;
		// the address space exits
		// by doing the syscall "exit"	
		//user t
	    };
	}, space);
	//have a lock in case a process needs to join
	Debug.println('S', "Adding " + space.getSpaceId() + " to possibly join.");
	addProcessInformation(space);
	Nachos.scheduler.readyToRun(t);
	//write back the spaceid to the register
	CPU.writeRegister(2, space.getSpaceId());
	return space.getSpaceId();
    }



    /**
     * Wait for the user program specified by "id" to finish, and
     * return its exit status.
     *
     * @param id The "space ID" of the program to wait for.
     * @return the exit status of the specified program.
     */
    public static int join(int id) {
	Debug.println('S', "Waiting to join " + id );
	Semaphore sem;
	lockProcess.acquire();
	if(processes.containsKey(id)){
	   sem = processes.get(id).getSemaphore();
	}else{
	    Debug.println('S', "Joined " + id + ", leaving join with status -1");
	    lockProcess.release();
	    return -1;//return user not found
	}
	lockProcess.release();
	
	sem.P();

	int status = getStatusOfChildThatExitedFromCurrentThread();
	
	Debug.println('S', "Joined " + id + ", leaving join with status " + status);
	return status;
    }

    
    

    /* File system operations: Create, Open, Read, Write, Close
     * These functions are patterned after UNIX -- files represent
     * both files *and* hardware I/O devices.
     *
     * If this assignment is done before doing the file system assignment,
     * note that the Nachos file system has a stub implementation, which
     * will work for the purposes of testing out these routines.
     */

    // When an address space starts up, it has two open files, representing 
    // keyboard input and display output (in UNIX terms, stdin and stdout).
    // Read and write can be used directly on these, without first opening
    // the console device.

    /** OpenFileId used for input from the keyboard. */
    public static final int ConsoleInput = 0;

    /** OpenFileId used for output to the display. */
    public static final int ConsoleOutput = 1;

    /**
     * Create a Nachos file with a specified name.
     *
     * @param name  The name of the file to be created.
     */
    public static void create(String name) { }

    /**
     * Remove a Nachos file.
     *
     * @param name  The name of the file to be removed.
     */
    public static void remove(String name) { }

    /**
     * Open the Nachos file "name", and return an "OpenFileId" that can 
     * be used to read and write to the file.
     *
     * @param name  The name of the file to open.
     * @return  An OpenFileId that uniquely identifies the opened file.
     */
    public static int open(String name) {return 0;}

    /**
     * Write "size" bytes from "buffer" to the open file.
     *
     * @param buffer Location of the data to be written.
     * @param size The number of bytes to write.
     * @param id The OpenFileId of the file to which to write the data.
     */
    public static void write(byte buffer[], int size, int id) {
	if (id == ConsoleOutput) {
	    for(int i = 0; i < size; i++) {
		Nachos.consoleDriver.putChar((char)buffer[i]);
	    }
	}
    }

    /**
     * Read "size" bytes from the open file into "buffer".  
     * Return the number of bytes actually read -- if the open file isn't
     * long enough, or if it is an I/O device, and there aren't enough 
     * characters to read, return whatever is available (for I/O devices, 
     * you should always wait until you can return at least one character).
     *
     * @param buffer Where to put the data read.
     * @param size The number of bytes requested.
     * @param id The OpenFileId of the file from which to read the data.
     * @return The actual number of bytes read.
     */
    public static int read(byte buffer[], int size, int id) {
	  int counter = 0;
	if (id == ConsoleInput) {
	    byte tmp;
	    for(int i = 0; i < size; i++) {
		if( (tmp = (byte)Nachos.consoleDriver.getChar())!= 0x0){
		    buffer[i] = tmp;
		    counter++;
		}
		
	    }
	}
	return counter;
    }

    /**
     * Close the file, we're done reading and writing to it.
     *
     * @param id  The OpenFileId of the file to be closed.
     */
    public static void close(int id) {}


    /*
     * User-level thread operations: Fork and Yield.  To allow multiple
     * threads to run within a user program. 
     */

    /**
     * Fork a thread to run a procedure ("func") in the *same* address space 
     * as the current thread.
     *
     * @param func The user address of the procedure to be run by the
     * new thread.
     */
    public static void fork(int func) {

	AddrSpace child = new AddrSpace();
	AddrSpace parent= ((UserThread)NachosThread.currentThread()).space;
	
	UserThread user = new UserThread("Fork", 
 new Runnable(){

	    public void run(){
		MemManager memManager = MemManager.getInstance();
		//copy page table for child if theres enough memor
		if(child.copyPageTableForForking(child, parent)==0){
		//set registers at the given function
		parent.initRegistersFork(func);
		child.restoreState();
		CPU.runUserCode();
		}else{
		    Debug.println('S', "removing child");
		    memManager.finishAddrs(child);
		}
	    }
	}, child);
	
	//add it to scheduler to run
	Nachos.scheduler.readyToRun(user);

    }

    /**
     * Yield the CPU to another runnable thread, whether in this address space 
     * or not. 
     */
    public static void yield() {
	Nachos.scheduler.yieldThread();
    }

    
    
    
    /**Helper methods for Syscall.java that take care of retrieving from hashmaps*/
    /**
     * Retrieves the status of the exiting child from current parent
     * @return the status, else -1 as an error
     */
    private static int getStatusOfChildThatExitedFromCurrentThread(){
	int parent = ((UserThread)NachosThread.currentThread()).space.getSpaceId();
	int status = -1;
	lockStatus.acquire();
	//check if there is a status else return error
	if(exitStatus.containsKey(parent)){
		status = exitStatus.remove(parent);
	}
	lockStatus.release();
	return status;
    }
    
    /**
     * 
     * @param space 
     * @return
     */
    private static int getParentId(AddrSpace space){
	    lockProcess.acquire();
	    int parentId = processes.get(space.getSpaceId()).getParentID();
	    lockProcess.release();
	    return parentId;
    }
    /**
     * 
     * @param space
     * @return
     */
    private static boolean containsProcess(AddrSpace space){
	lockProcess.acquire();
	boolean contains = processes.containsKey(space.getSpaceId());
	lockProcess.release();
	return contains;
    }
    /**
     * 
     * @param space
     */
    private static void addProcessInformation(AddrSpace space){
	lockProcess.acquire();
	processes.put(space.getSpaceId(), 
		new Syscall().new ProcessInformation(((UserThread)NachosThread.currentThread()).space.getSpaceId()));
	lockProcess.release();
    }
    
    /**
     * 
     * @param space
     * @param status
     */
    private static void addExitStatus(AddrSpace space,int status){
		lockStatus.acquire();
		exitStatus.put(processes.get(space.getSpaceId()).getParentID(), status);
		lockStatus.release();
    }
    
    /**
     * 
     * @param space
     */
    private static void removeAndReleaseProcessSem(AddrSpace space){
	    lockProcess.acquire();
	    processes.remove(space.getSpaceId()).getSemaphore().V();;
	    lockProcess.release();
    }
    
    /**
     * 
     * @param space space to discard status from
     */
    private static void discardStatus(AddrSpace space){
	lockStatus.acquire();
	if(exitStatus.containsKey(space.getSpaceId())){
	    exitStatus.remove(space.getSpaceId());
	    Debug.println('S', "Parent terminated without calling Join" + exitStatus.size());
	}
	lockStatus.release();
    }
    
    /**
     * 
     * 
     */
    public static int predictCPU(UserThread thread) {
	lockStatus.acquire();
	int ticks = thread.getTicks();
	lockStatus.release();
	return ticks;
    }
}
