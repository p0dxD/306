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
  static boolean[] isTaken = new boolean[Machine.NumPhysPages];
  //stores key as the address spaceId, and the value as the virtual-physical page mapping within this address space. 
  static HashMap<Integer, ArrayList<Integer>> maping = new HashMap<>();
  //identifier for the address space. 
  private int SpaceId; 
  
  static SpinLock lockMaping = new SpinLock("Lock for maping");
  static SpinLock lockAddrs = new SpinLock("Lock for maping");
  static SpinLock lockPhysical = new SpinLock("Lock for maping");
  
  public static HashMap<Integer, AddrSpace> addresses = new HashMap<>();
  
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
      lockAddrs.acquire();
      addresses.put(SpaceId, this);
      lockAddrs.release();

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

    getFreePages(size, SpaceId, noffH, executable);
    return(0);
  }
  
  public  String getStringFromAddress(long address, AddrSpace space){

	StringBuilder string  = new StringBuilder();
	char tmp;
	while((tmp =AddrSpace.getMeCharAtAddress(address, space)) != '\0'){

	    string.append(tmp);
	    address++;
	}
	return string.toString();
  }
  
  public static void getCharsFromMemory(int address, AddrSpace space, int len, byte[] buf) {
	char i = 0;
	while(i < len){	  
	    buf[i] = (byte)AddrSpace.getMeCharAtAddress(address, space);
	    address++;
	    i++;
	}

  }
  
  public static int writeByteArrayToPhysicalMem(int address, AddrSpace space, byte[] arr){
      int sizeWritten = 0;
      while(sizeWritten < arr.length){
	  writeCharToPhysicalMem(address,space, arr[sizeWritten]);
	  sizeWritten++;
	  address++;
      }
      
      return sizeWritten;
      
  }
  /**
   * Writes a char to indicated memory location
   * @param address where to start writing to
   * @param c character to write
   * @return new address location
   */
  public static void writeCharToPhysicalMem(int address, AddrSpace space, byte c){
      int physicalIndexAddress = convertVirtualToPhysicalIndex(address, space);
      Machine.mainMemory[physicalIndexAddress] = c;
  }
  
  public static void finishAddrs(AddrSpace space){
	AddrSpace.lockAddrs.acquire();
	AddrSpace.addresses.remove(space.getSpaceId());
	AddrSpace.lockAddrs.release();
	Nachos.scheduler.finishThread();
  }
  
  //get the char at the physical index. 
  public static char getMeCharAtAddress(long address, AddrSpace space){
      int physicalIndexAddress = convertVirtualToPhysicalIndex(address, space);
      return (char)Machine.mainMemory[physicalIndexAddress];
  }
  
 
  
  /*
   * Takes in a virtual index within the virtual page table, and returns the 
   * index within the  physical page table in main memory. 
   */
  public static int convertVirtualToPhysicalIndex(long address, AddrSpace space){
      int virtualIndex = (int)address/Machine.PageSize;
      int virtualOffset = (int)address%Machine.PageSize;
      lockMaping.acquire();
      ArrayList<Integer> physicalPages = maping.get(space.getSpaceId());
      lockMaping.release();
      //the actual physical address
      int physicalIndexAddress  = (physicalPages.get(virtualIndex)*Machine.PageSize) + virtualOffset;
      return physicalIndexAddress;
  }
  
  /**
   * Initialize the user-level register set to values appropriate for
   * starting execution of a user program loaded in this address space.
   *
   * We write these directly into the "machine" registers, so
   * that we can immediately jump to user code.
   */
  public void initRegisters() {
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
  
  
  /**
   * 
   */
  public void cleanProgram(){

      lockMaping.acquire();
      ArrayList<Integer> physical = maping.get(this.SpaceId);
      lockMaping.release();
      
      lockPhysical.acquire();
      for(Integer i: physical){
	  clearPhysPageIndex(i);
	  isTaken[i] = false;
      }
      lockPhysical.release();
      
      lockMaping.acquire();
      maping.remove(this.SpaceId);
      lockMaping.release();

      
      Debug.println('S', "Done cleaning up process.");

  }
  
  /*
   * Each thread has its own pageTable. However, each thread also has its own stack. 
   * Grabs the virtual page table from the address space, shared by all threads, and then initializes
   * its own thread pageTable. 
   */
  public  void initThreadPageTable(AddrSpace space){
      	space.initPageTable(this.getPageTableLength());
      	TranslationEntry[] pageTable = space.getPageTable();
      	


	//calculate how many pages of the pageTable is for user stack. 
	int pagesForStack = (int)(AddrSpace.getUserStackSize() / Machine.PageSize);
	//copy all pages from the address space's pageTable, except for the stack space. 
	
	for(int i=0; i < (this.getPageTableLength()-pagesForStack);i++){

	    pageTable[i] = new TranslationEntry();
	    pageTable[i].virtualPage = this.pageTable[i].virtualPage;
	    
	    pageTable[i].physicalPage = this.pageTable[i].physicalPage;
	    pageTable[i].valid =this.pageTable[i].valid;
	    pageTable[i].use =this.pageTable[i].use;
	    pageTable[i].dirty =this.pageTable[i].dirty;
	}
	ArrayList<Integer> physical = new ArrayList<>();
	
	lockMaping.acquire();
	ArrayList<Integer> parent = maping.get(this.SpaceId);
	
	
	for(int i =0; i < (this.getPageTableLength()-pagesForStack); i++){
	    physical.add(parent.get(i));
	}
	
	lockMaping.release();
	
	//get free space from physical memory for this threads own stack. 
	ArrayList<Integer> physPagesForStack = AddrSpace.physicalMemoryLocation(pagesForStack);
	//map the retrieved free physical pages to the thread's pageTable's stack area. 
	for(int i= (this.getPageTableLength()-pagesForStack);i<this.getPageTableLength(); i++){
	    pageTable[i] = new TranslationEntry();
	    pageTable[i].virtualPage = i;
	    pageTable[i].physicalPage = physPagesForStack.get(i-(this.getPageTableLength()-pagesForStack)); //calculation to start iterating at 0 through pagesForStack-1
	    pageTable[i].valid = true;
	    pageTable[i].use= false;
	    pageTable[i].dirty= false;
	}
	
	for(int i= 0;i< physPagesForStack.size(); i++){
	    physical.add(physPagesForStack.get(i));
	}
	
	      lockMaping.acquire();
	      maping.put(space.SpaceId, physical);
	      lockMaping.release();
		
  }
  
  public static byte[] getPageArrayAtIndex(int index){
      byte arr[] = new byte[Machine.PageSize];
      
      System.arraycopy(Machine.mainMemory, index*Machine.PageSize, arr, 0, Machine.PageSize);
      return arr;
  }
  
  /*
   *  clears the physical page, of size Machine.PageSize. Calculates the physical page index offset in main memory
   *  Then zeroes out the size of one page. 
   */
  public void clearPhysPageIndex(int physPageIndex){
      int startIndex = physPageIndex*Machine.PageSize;
      for(int i=0;i<Machine.PageSize;i++){
	  Machine.mainMemory[startIndex]= (byte)0;
	  startIndex++;
      }
  }
  
      /**
       * Checks the memory management unit whether we can accomdate a request for physical page tables. 
       * @param byteSize
       */
      public void getFreePages(long byteSize, int SpaceId, NoffHeader noffH,OpenFile executable){
	  int numPages = (int)(byteSize / Machine.PageSize);
	    Debug.ASSERT((numPages <= Machine.NumPhysPages),// check we're not trying
			 "AddrSpace constructor: Not enough memory!");
	                                                // to run anything too big --
							// at least until we have
							// virtual memory
	  if(isEnoughPhysMem(numPages)){

	      ArrayList<Integer> physicalLocation = physicalMemoryLocation(numPages);
	      lockMaping.acquire();
	      maping.put(SpaceId, physicalLocation);

	      lockMaping.release();
	      
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
	  	clearPhysPageIndex(physicalLocation.get(i));
	      
	      // then, copy in the code and data segments into memory
	      if (noffH.code.size > 0) {
	        Debug.println('a', "Initializing code segment, at " +
	  	    noffH.code.virtualAddr + ", size " +
	  	    noffH.code.size);

	        executable.seek(noffH.code.inFileAddr);
//	        executable.read(Machine.mainMemory, noffH.code.virtualAddr, noffH.code.size);//fix convert to physical
	        //read part by part page by page
	        copySegmentToPhysical(physicalLocation, executable);
	      }

	      if (noffH.initData.size > 0) {
	        Debug.println('a', "Initializing data segment, at " +
	  	    noffH.initData.virtualAddr + ", size " +
	  	    noffH.initData.size);

	        executable.seek(noffH.initData.inFileAddr);
//	        executable.read(Machine.mainMemory, noffH.initData.virtualAddr, noffH.initData.size);//same as top
	        //convert v to p
	        copySegmentToPhysical(physicalLocation, executable);
	      }
	  }else{

	      Debug.print('M', "Not enough Physical mem.");
	  }
      }
      
      public void copySegmentToPhysical(ArrayList<Integer> physical, OpenFile executable){	  
	  for(int i = 0; i < physical.size();i++){
	      executable.read(Machine.mainMemory, physical.get(i)*Machine.PageSize, Machine.PageSize);
	  }
      }
      
      /**
       * Checks if we have enough available physical pages within the machine to accomodate the requested pages. 
       * @param pagesNeeded
       * @return
       */
      public boolean isEnoughPhysMem(int pagesNeeded){
	  return getFreePageNum() >= pagesNeeded;
      }
      
      /**
       * 
       * @param pagesNeeded
       * @return
       */
      public static ArrayList<Integer> physicalMemoryLocation(int pagesNeeded){
	  ArrayList<Integer> physicalLocation = new ArrayList<Integer>();
	  
	  lockPhysical.acquire();
	  for(int i = 0; (i < isTaken.length) && (pagesNeeded > 0); i++){
	      if(!isTaken[i]){
		  physicalLocation.add(i);
		  isTaken[i] = true;
		  pagesNeeded--;
	      }
	  }
	  lockPhysical.release();
	  
	  return physicalLocation;
      }
      
      /**
       * Returns the number of physical pages that are free. 
       * @return
       */
      public int getFreePageNum(){
	  int count = 0;
	  lockPhysical.acquire();
	  for(int i = 0; i < isTaken.length; i++){
	      if(!isTaken[i]){
		  count++;
	      }
	  }
	  lockPhysical.release();
	  return count;
      }
            
}
