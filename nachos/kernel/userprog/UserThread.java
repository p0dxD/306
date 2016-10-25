// UserThread.java
//	A UserThread is a NachosThread extended with the capability of
//	executing user code.
//
// Copyright (c) 1992-1993 The Regents of the University of California.
// Copyright (c) 1998 Rice University.
// Copyright (c) 2003 State University of New York at Stony Brook.
// All rights reserved.  See the COPYRIGHT file for copyright notice and
// limitation of liability and disclaimer of warranty provisions.

package nachos.kernel.userprog;

import nachos.machine.MIPS;
import nachos.machine.Machine;
import nachos.machine.NachosThread;
import nachos.machine.TranslationEntry;
import nachos.noff.NoffHeader;

import java.util.ArrayList;

import nachos.Debug;
import nachos.kernel.filesys.OpenFile;
import nachos.machine.CPU;

/**
 * A UserThread is a NachosThread extended with the capability of
 * executing user code.  It is kept separate from AddrSpace to provide
 * for the possibility of having multiple UserThreads running in a
 * single AddrSpace.
 * 
 * @author Thomas Anderson (UC Berkeley), original C++ version
 * @author Peter Druschel (Rice University), Java translation
 * @author Eugene W. Stark (Stony Brook University)
 */
public class UserThread extends NachosThread {
    /** The context in which this thread will execute. */
    public final AddrSpace space;
    //each thread has its own pageTable
    private TranslationEntry pageTable[];

    // A thread running a user program actually has *two* sets of 
    // CPU registers -- one for its state while executing user code,
    // and one for its state while executing kernel code.
    // The kernel registers are managed by the super class.
    // The user registers are managed here.

    /** User-level CPU register state. */
    private int userRegisters[] = new int[MIPS.NumTotalRegs];

    /**
     * Initialize a new user thread.
     *
     * @param name  An arbitrary name, useful for debugging.
     * @param runObj Execution of the thread will begin with the run()
     * method of this object.
     * @param addrSpace  The context to be installed when this thread
     * is executing in user mode.
     */
    public UserThread(String name, Runnable runObj, AddrSpace addrSpace) {
	super(name, runObj);
	space = addrSpace;
	//find the set stack size throughout the system, then allocate mem for thread's own stack. 
	initThreadPageTable(addrSpace.getPageTable(),AddrSpace.getUserStackSize());
    }
    
    /*
     * Each thread has its own pageTable. However, each thread also has its own stack. 
     * Grabs the virtual page table from the address space, shared by all threads, and then initializes
     * its own thread pageTable. 
     */
    public void initThreadPageTable(TranslationEntry[] pageTable, int tablePageSize){
	this.pageTable = new TranslationEntry[tablePageSize];
	//calculate how many pages of the pageTable is for user stack. 
	int pagesForStack = (int)(AddrSpace.getUserStackSize() / Machine.PageSize);
	//copy all pages from the address space's pageTable, except for the stack space. 
	for(int i=0;i<(tablePageSize-pagesForStack);i++){
	    this.pageTable[i].virtualPage =pageTable[i].virtualPage;
	    this.pageTable[i].physicalPage =pageTable[i].physicalPage;
	    this.pageTable[i].valid =pageTable[i].valid;
	    this.pageTable[i].use =pageTable[i].use;
	    this.pageTable[i].dirty =pageTable[i].dirty;
	}
	//get free space from physical memory for this threads own stack. 
	ArrayList<Integer> physPagesForStack = AddrSpace.physicalMemoryLocation(pagesForStack);
	//map the retrieved free physical pages to the thread's pageTable's stack area. 
	for(int i= (tablePageSize-pagesForStack);i<tablePageSize; i++){
	    this.pageTable[i].virtualPage = i;
	    this.pageTable[i].physicalPage = physPagesForStack.get((tablePageSize-pagesForStack)-i); //calculation to start iterating at 0 through pagesForStack-1
	    this.pageTable[i].valid = true;
	    this.pageTable[i].use= false;
	    this.pageTable[i].dirty= false;
	}
		
    }
    
    /**
     * Save the CPU state of a user program on a context switch.
     */
    @Override
    public void saveState() {
	// Save state associated with the address space.
	space.saveState();  

	// Save user-level CPU registers.
	for (int i = 0; i < MIPS.NumTotalRegs; i++)
	    userRegisters[i] = CPU.readRegister(i);

	// Save kernel-level CPU state.
	super.saveState();
    }

    /**
     * Restore the CPU state of a user program on a context switch.
     */
    @Override
    public void restoreState() {
	// Restore the kernel-level CPU state.
	super.restoreState();

	// Restore the user-level CPU registers.
	for (int i = 0; i < MIPS.NumTotalRegs; i++)
	    CPU.writeRegister(i, userRegisters[i]);

	// Restore state associated with the address space.
	space.restoreState();
    }
}
