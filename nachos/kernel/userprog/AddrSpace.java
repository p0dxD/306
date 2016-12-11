// AddrSpace.java
//	Class to manage address spaces (executing user programs).
//
//	In order to run a user program, you must:
//
//	1. link with the -N -T 0 option 
//	2. run coff2noff to convert the object file to Nachos format
//		(Nachos object code format is essentially just a simpler
//		version of the UNIX executable object code format)
//	3. load the NOFF file into the Nachos file system
//		(if you haven't implemented the file system yet, you
//		don't need to do this last step)
//
// Copyright (c) 1992-1993 The Regents of the University of California.
// Copyright (c) 1998 Rice University.
// Copyright (c) 2003 State University of New York at Stony Brook.
// All rights reserved.  See the COPYRIGHT file for copyright notice and
// limitation of liability and disclaimer of warranty provisions.

package nachos.kernel.userprog;

import java.util.ArrayList;
import java.util.HashMap;

import nachos.Debug;
import nachos.machine.CPU;
import nachos.machine.MIPS;
import nachos.machine.Machine;
import nachos.machine.NachosThread;
import nachos.machine.TranslationEntry;
import nachos.noff.NoffHeader;
import nachos.kernel.Nachos;
import nachos.kernel.filesys.OpenFile;
import nachos.kernel.threads.SpinLock;

/**
 * This class manages "address spaces", which are the contexts in which
 * user programs execute.  For now, an address space contains a
 * "segment descriptor", which describes the the virtual-to-physical
 * address mapping that is to be used when the user program is executing.
 * As you implement more of Nachos, it will probably be necessary to add
 * other fields to this class to keep track of things like open files,
 * network connections, etc., in use by a user program.
 *
 * NOTE: Most of what is in currently this class assumes that just one user
 * program at a time will be executing.  You will have to rewrite this
 * code so that it is suitable for multiprogramming.
 * 
 * @author Thomas Anderson (UC Berkeley), original C++ version
 * @author Peter Druschel (Rice University), Java translation
 * @author Eugene W. Stark (Stony Brook University)
 */
public class AddrSpace {

  /** Page table that describes a virtual-to-physical address mapping. */
  private TranslationEntry pageTable[];
  /** Default size of the user stack area -- increase this as necessary! */
  private static final int UserStackSize = 1024;
  
  //MEMORY MANAGEMENT AREA
  //keeps track of the physical pages we have free or taken.
//  static int[] isTaken = new int[Machine.NumPhysPages];
  //stores key as the address spaceId, and the value as the virtual-physical page mapping within this address space. 
//  static HashMap<Integer, ArrayList<Integer>> maping = new HashMap<>();
  //identifier for the address space. 
  private int SpaceId; 
  private MemManager memManager = MemManager.getInstance();
 
  public void setPageTable(TranslationEntry pageTable[]){
      this.pageTable = pageTable;
  }
  
  public static int getUserStackSize(){
      return AddrSpace.UserStackSize;
  }
  
  public int getPageTableLength(){
      return pageTable.length;
  }
  public TranslationEntry[] getPageTable(){
      return this.pageTable;
  }
  
  public void initPageTable(int size){
      this.pageTable = new TranslationEntry[size];
  }
  /**
   * Constructor for a new address space.
   */
  public AddrSpace() { 
      
      SpaceId = this.hashCode();
      //add code to add this address space
      memManager.addNewAddrSpace(SpaceId, this);

  }

  /**
   * Load the program from a file "executable", and set everything
   * up so that we can start executing user instructions.
   *
   * Assumes that the object code file is in NOFF format.
   *
   * First, set up the translation from program memory to physical 
   * memory.  For now, this is really simple (1:1), since we are
   * only uniprogramming.
   *
   * @param executable The file containing the object code to 
   * 	load into memory
   * @return -1 if an error occurs while reading the object file,
   *    otherwise 0.
   */
  public int exec(OpenFile executable) {
    NoffHeader noffH;
    long size;
    if((noffH = NoffHeader.readHeader(executable)) == null)
	return(-1);
    // how big is address space?
    size = roundToPage(noffH.code.size)
	     + roundToPage(noffH.initData.size + noffH.uninitData.size)
	     + UserStackSize;	// we need to increase the size
    				// to leave room for the stack

    int status = getFreePages(size, SpaceId, noffH, executable);
    return status;
  }
  

  
 
  
  /**
   * Initialize the user-level register set to values appropriate for
   * starting execution of a user program loaded in this address space.
   *
   * We write these directly into the "machine" registers, so
   * that we can immediately jump to user code.
   */
  public void initRegisters() {
     Debug.println('S', "INSIDE INIT REGISTERS");
    int i;
    for (i = 0; i < MIPS.NumTotalRegs; i++)
      CPU.writeRegister(i, 0);

    // Initial program counter -- must be location of "Start"
    CPU.writeRegister(MIPS.PCReg, 0);	

    // Need to also tell MIPS where next instruction is, because
    // of branch delay possibility
    CPU.writeRegister(MIPS.NextPCReg, 4);

    // Set the stack register to the end of the segment.
    // NOTE: Nachos traditionally subtracted 16 bytes here,
    // but that turns out to be to accomodate compiler convention that
    // assumes space in the current frame to save four argument registers.
    // That code rightly belongs in start.s and has been moved there.
    int sp = pageTable.length * Machine.PageSize;
    CPU.writeRegister(MIPS.StackReg, sp);
    Debug.println('a', "Initializing stack register to " + sp);
  }
  /**
   * Initializes the register pointers to start execution
   * @param func of where to start execution
   */
  public void initRegistersFork(int func){
	for (int i = 0; i < MIPS.NumTotalRegs; i++){
	    CPU.writeRegister(i, 0);		    
	}		
	//pass in user address of procedure for the user program in mem
	//to start from
	CPU.writeRegister(MIPS.PCReg, func);	
	//next user instruction due to possible branch delay
	CPU.writeRegister(MIPS.NextPCReg, func+4);
	int sp = this.getPageTableLength()* Machine.PageSize;
	CPU.writeRegister(MIPS.StackReg, sp);
	Debug.println('a', "Initializing stack register to " + sp + " for forked thread.");	
  }
  /**
   * On a context switch, save any machine state, specific
   * to this address space, that needs saving.
   *
   * For now, nothing!
   */
  public void saveState() {
      
  }
  
  //done when switching one process 
  //proper page to happen
  
  
  /**
   * On a context switch, restore any machine state specific
   * to this address space.
   *
   * For now, just tell the machine where to find the page table.
   */
  public void restoreState() {
    CPU.setPageTable(pageTable);
  }

  /* 
   * Returns this spaceId variable. 
   */
  public int getSpaceId(){
      return this.SpaceId;
  }
  
  /**
   * Utility method for rounding up to a multiple of CPU.PageSize;
   */
  private long roundToPage(long size) {
    return(Machine.PageSize * ((size+(Machine.PageSize-1))/Machine.PageSize));
  }
  
  

  public static byte[] getPageArrayAtIndex(int index){
      byte arr[] = new byte[Machine.PageSize];
      
      System.arraycopy(Machine.mainMemory, index*Machine.PageSize, arr, 0, Machine.PageSize);
      return arr;
  }
  
  
      /**
       * Checks the memory management unit whether we can accomdate a request for physical page tables. 
       * @param byteSize
       */
      public int getFreePages(long byteSize, int SpaceId, NoffHeader noffH,OpenFile executable){
	  int numPages = (int)(byteSize / Machine.PageSize);
	    Debug.ASSERT((numPages <= Machine.NumPhysPages),// check we're not trying
			 "AddrSpace constructor: Not enough memory!");
	                                                // to run anything too big --
							// at least until we have
							// virtual memory
	  if(memManager.isEnoughPhysMem(numPages)){

	      ArrayList<Integer> physicalLocation = memManager.getPhysicalMemoryLocations(numPages);
	      memManager.addPhysicalLocationForSpaceId(SpaceId,physicalLocation);
	      
	    Debug.println('a', "Initializing address space, numPages=" 
			+ numPages + ", size=" + byteSize);
	      //store the physical and link it to this physical
	      pageTable = new TranslationEntry[numPages];
	      for (int i = 0; i < numPages; i++) {
		      pageTable[i] = new TranslationEntry();
		      pageTable[i].virtualPage = i; 
		      pageTable[i].physicalPage = physicalLocation.get(i);
		      pageTable[i].valid = true;
		      pageTable[i].use = false;
		      pageTable[i].dirty = false;
		      pageTable[i].readOnly = false;  // if code and data segments live on
						      // separate pages, we could set code 
						      // pages to be read-only
	       }
	      
	      //zero out the memory physical location for this program 
	      for(int i = 0; i < numPages; i++)
	  	MemManager.clearPhysPageIndex(physicalLocation.get(i));
	      
	      // then, copy in the code and data segments into memory
	      if (noffH.code.size > 0) {
	        Debug.println('a', "Initializing code segment, at " +
	  	    noffH.code.virtualAddr + ", size " +
	  	    noffH.code.size);

	        executable.seek(noffH.code.inFileAddr);
//	        executable.read(Machine.mainMemory, noffH.code.virtualAddr, noffH.code.size);//fix convert to physical
	        int startIndexForPhysical = noffH.code.virtualAddr/Machine.PageSize;
	        int endIndexOfPhysical = (int)Math.ceil(noffH.code.size/(double)Machine.PageSize)
	        	+noffH.code.virtualAddr/Machine.PageSize;
	        
	        //read part by part page by page
	        memManager.copySegmentToPhysical(physicalLocation, executable, 
	        	startIndexForPhysical, endIndexOfPhysical);
	      }

	      if (noffH.initData.size > 0) {
	        Debug.println('a', "Initializing data segment, at " +
	  	    noffH.initData.virtualAddr + ", size " +
	  	    noffH.initData.size);

	        executable.seek(noffH.initData.inFileAddr);
//	        executable.read(Machine.mainMemory, noffH.initData.virtualAddr, noffH.initData.size);//same as top
	        //convert v to p
	        
	        int endIndexOfPhysical = (int)Math.ceil(noffH.initData.size/(double)Machine.PageSize)
	        	+noffH.initData.inFileAddr/Machine.PageSize;
	        int startIndexForPhysical = noffH.initData.virtualAddr/Machine.PageSize;
	        
	        memManager.copySegmentToPhysical(physicalLocation, executable, 
	        	startIndexForPhysical, endIndexOfPhysical);
	        
	      }
	      return 0;
	  }else{

	      Debug.print('M', "Not enough Physical mem.");
	      return -1;
	  }
      }
      
      

      /*
       * Each thread has its own pageTable. However, each thread also has its own stack. 
       * Grabs the virtual page table from the address space, shared by all threads, and then initializes
       * its own thread pageTable. 
       */
      public  int copyPageTableForForking(AddrSpace spaceChild, AddrSpace spaceParent){
          spaceChild.initPageTable(spaceParent.getPageTableLength());
          TranslationEntry[] pageTableChild = spaceChild.getPageTable();
          TranslationEntry[] pageTableParent = spaceParent.getPageTable();
          if(memManager.getPagesForStackSize() <= memManager.getFreePageNum()){//TODO:make size of stack only
    	//calculate how many pages of the pageTable is for user stack. 
    	int pagesForStack = memManager.getPagesForStackSize();
    	//copy all pages from the address space's pageTable, except for the stack space. 
    	
    	for(int i=0; i < (spaceParent.getPageTableLength()-pagesForStack);i++){
    	    
    	    pageTableChild[i] = new TranslationEntry();
    	    pageTableChild[i].virtualPage = pageTableParent[i].virtualPage;
    	    pageTableChild[i].physicalPage = pageTableParent[i].physicalPage;
    	    pageTableChild[i].valid =pageTableParent[i].valid;
    	    pageTableChild[i].use =pageTableParent[i].use;
    	    pageTableChild[i].dirty =pageTableParent[i].dirty;
    	}
    	ArrayList<Integer> physical = new ArrayList<>();
    	
    	memManager.addReferenceFromFork(spaceParent, physical);
    	
    	
    	//get free space from physical memory for this threads own stack. 
    	ArrayList<Integer> physPagesForStack = memManager.getPhysicalMemoryLocations(pagesForStack);
    	//map the retrieved free physical pages to the thread's pageTable's stack area. 
    	for(int i= (spaceParent.getPageTableLength()-pagesForStack);i<spaceParent.getPageTableLength(); i++){
    	    pageTableChild[i] = new TranslationEntry();
    	    pageTableChild[i].virtualPage = i;
    	    pageTableChild[i].physicalPage = physPagesForStack.get(i-
    		    (spaceParent.getPageTableLength()-pagesForStack)); //calculation to start iterating at 0 through pagesForStack-1
    	    pageTableChild[i].valid = true;
    	    pageTableChild[i].use= false;
    	    pageTableChild[i].dirty= false;
    	}
    	
    	for(int i= 0;i< physPagesForStack.size(); i++){
    	    physical.add(physPagesForStack.get(i));
    	}
    		memManager.addPhysicalLocationForSpaceId(spaceChild.getSpaceId(), physical);
    	      
    	      return 0;
          }else{
              Debug.println('S', "Not enough memory for forking");
              return -1;
          }
      }   
      
      /**
       * Expands a pagetable by the requested pages
       * @param space to expand
       * @param pages to expand by
       */
      public void expandPageTable(int numPages){
	  Debug.println('L', "Expanding vp table by " +numPages);
  	//get current pageTable
  	 TranslationEntry pageTable[] = this.getPageTable();
  	 TranslationEntry newPageTable[] = new TranslationEntry[pageTable.length+numPages];
  	 //copy the old values
  	 int i;
  	 for(i = 0 ; i < pageTable.length; i++){
  	     newPageTable[i] = pageTable[i];
  	 }
  	 //create the new values
  	 for (; 0 < numPages; i++, numPages--) {
  	     newPageTable[i] = new TranslationEntry();
  	     newPageTable[i].virtualPage = i; 
  	     newPageTable[i].valid = false;
  	     newPageTable[i].use = false;
  	     newPageTable[i].dirty = false;
  	     newPageTable[i].readOnly = false;   // if code and data segments live on
  						// separate pages, we could set code 
  					       // pages to be read-only
  	 }
  	 //sets the page table to the newly created
  	 this.setPageTable(newPageTable);
  	 //restore state to modified pageTable
  	 this.restoreState();
      }
      
      /**
       * Sets the physical of a virtual page
       * @param vmPageNum of which to set it to
       * @param physicalPage location
       */
      public void setPhysicalMemoryOfEntry(int vmPageNum,int physicalPage){
	  Debug.println('L', "Setting physical memory for  vmPageNum " +vmPageNum +" on physical "+ physicalPage);
//	  if(){
	      //clear page otherwise
	      MemManager.clearPhysPageIndex(physicalPage); 
//	  }else{
	      //get memory from paging
//	  }
	  pageTable[vmPageNum].physicalPage = physicalPage;
	  pageTable[vmPageNum].valid = true;
      }
            
}
