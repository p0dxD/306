package nachos.kernel.userprog;

import java.util.ArrayList;
import java.util.HashMap;

import nachos.Debug;
import nachos.kernel.Nachos;
import nachos.kernel.filesys.OpenFile;
import nachos.kernel.threads.SpinLock;
import nachos.machine.Machine;
import nachos.machine.TranslationEntry;

public class MemManager {
    private final SpinLock lockMaping  = new SpinLock("Lock for maping");
    private int[] isTaken  = new int[Machine.NumPhysPages];
    private HashMap<Integer, ArrayList<Integer>> maping  = new HashMap<>();
    private HashMap<Integer, AddrSpace> addresses = new HashMap<>();
    private final SpinLock lockAddrs  = new SpinLock("Lock for maping");
    private final SpinLock lockPhysical = new SpinLock("Lock for maping");
    
    //Singleton of MemManager
    static MemManager instance = new MemManager();
    
    private MemManager(){

    }
    
    public static MemManager getInstance(){
	return instance;
    }
    
    
    /*
     * Takes in a virtual index within the virtual page table, and returns the 
     * index within the  physical page table in main memory. 
     */
    public  int convertVirtualToPhysical(long address, AddrSpace space){
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
     * Get the char at the physical index. 
     * @param address
     * @param space
     * @return
     */
    public  char getMeCharAtAddress(long address, AddrSpace space){
        int physicalIndexAddress = convertVirtualToPhysical(address, space);
        return (char)Machine.mainMemory[physicalIndexAddress];
    }
    
    
    /**
     * Writes a char to indicated memory location
     * @param address where to start writing to
     * @param c character to write
     * @return new address location
     */
    public  void writeCharToPhysicalMem(int address, AddrSpace space, byte c){
        int physicalIndexAddress = convertVirtualToPhysical(address, space);
        Machine.mainMemory[physicalIndexAddress] = c;
    }
    
    /**
     * 
     * @param space
     */
    public  void finishAddrs(AddrSpace space){
  	removeAddress(space);
  	Nachos.scheduler.finishThread();
    }
    
    /**
     * 
     * @param address
     * @param space
     * @return
     */
    public  String getStringFromAddress(long address, AddrSpace space){

  	StringBuilder string  = new StringBuilder();
  	char tmp;
  	while((tmp = getMeCharAtAddress(address, space)) != '\0'){

  	    string.append(tmp);
  	    address++;
  	}
  	return string.toString();
    }
    
    
    /**
     * 
     * @param address
     * @param space
     * @param len
     * @param buf
     */
    public  void getCharsFromMemory(int address, AddrSpace space, int len, byte[] buf) {
  	char i = 0;
  	while(i < len){	  
  	    buf[i] = (byte)getMeCharAtAddress(address, space);
  	    address++;
  	    i++;
  	}

    }
    
    /**
     * 
     * @param address
     * @param space
     * @param arr
     * @return
     */
    public  int writeByteArrayToPhysicalMem(int address, AddrSpace space, byte[] arr){
        int sizeWritten = 0;
        while(sizeWritten < arr.length){
  	  writeCharToPhysicalMem(address,space, arr[sizeWritten]);
  	  sizeWritten++;
  	  address++;
        }
        
        return sizeWritten;
        
    }
    
    /**
     * 
     * @param space
     */
    public  void cleanProgram(AddrSpace space){

        lockMaping.acquire();
        ArrayList<Integer> physical = maping.get(space.getSpaceId());
        lockMaping.release();
        
        lockPhysical.acquire();
        for(Integer i: physical){
  	  isTaken[i]--;
  	  if(isTaken[i]==0){
  	      clearPhysPageIndex(i);
  	  }
  	  
        }
        lockPhysical.release();
        
        lockMaping.acquire();
        maping.remove(space.getSpaceId());
        lockMaping.release();

        
        Debug.println('S', "Done cleaning up process.");

    }
    
    
    /**
     * 
     * @param pagesNeeded
     * @return
     */
    public  ArrayList<Integer> physicalMemoryLocation(int pagesNeeded){
	  ArrayList<Integer> physicalLocation = new ArrayList<Integer>();
	  
	  lockPhysical.acquire();
	  for(int i = 0; (i < isTaken.length) && (pagesNeeded > 0); i++){
	      if(isTaken[i]==0){
		  physicalLocation.add(i);
		  isTaken[i]++;
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
    public  int getFreePageNum(){
	  int count = 0;
	  lockPhysical.acquire();
	  for(int i = 0; i < isTaken.length; i++){
	      if(isTaken[i]==0){
		  count++;
	      }
	  }
	  lockPhysical.release();
	  return count;
    }
    
    
    /**
     * Checks if we have enough available physical pages within the machine to accomodate the requested pages. 
     * @param pagesNeeded
     * @return
     */
    public  boolean isEnoughPhysMem(int pagesNeeded){
	  return getFreePageNum() >= pagesNeeded;
    }
    
    /**
     * 
     * @param spaceId
     * @param physicalLocation
     */
    public  void addPhysicalLocationForSpaceId(int spaceId, ArrayList<Integer> physicalLocation){
	      lockMaping.acquire();
	      maping.put(spaceId, physicalLocation);
	      lockMaping.release();
    }
    
    /**
     * 
     * @param spaceId
     * @param space
     */
    public  void addNewAddrSpace(int spaceId, AddrSpace space){
	lockAddrs.acquire();
	addresses.put(spaceId, space);
	lockAddrs.release();
    }
    

    /**
     *  clears the physical page, of size Machine.PageSize. Calculates the physical page index offset in main memory
     *  Then zeroes out the size of one page.  
     * @param physPageIndex
     */
    public static void clearPhysPageIndex(int physPageIndex){
        int startIndex = physPageIndex*Machine.PageSize;
        for(int i=0;i<Machine.PageSize;i++){
  	  Machine.mainMemory[startIndex]= (byte)0;
  	  startIndex++;
        }
    }
    
    /**
     * 
     * @param parentId
     * @return
     */
    public  boolean containsAddress(int parentId){
	    lockAddrs.acquire();
	    boolean contains = addresses.containsKey(parentId);
	    lockAddrs.release();
	    return contains;
    }
    
    /**
     * 
     * @param space
     */
    public  void removeAddress(AddrSpace space){
	lockAddrs.acquire();
	addresses.remove(space.getSpaceId());
	lockAddrs.release();
    }
    
    /**
     * 
     * @param physical
     * @param executable
     */
    public void copySegmentToPhysical(ArrayList<Integer> physical, OpenFile executable){	  
	  for(int i = 0; i < physical.size();i++){
	      executable.read(Machine.mainMemory, physical.get(i)*Machine.PageSize, Machine.PageSize);
	  }
    }
    
    /**
     * 
     * @param space
     * @param physicalChild
     */
    public void addReferenceFromFork(AddrSpace space, ArrayList<Integer> physicalChild){
    	lockMaping.acquire();
    	ArrayList<Integer> parentPhysicalSpots = maping.get(space.getSpaceId());
    	lockMaping.release();
    	
    	lockPhysical.acquire();
    	for(int i =0; i < (space.getPageTableLength()-getPagesForStackSize()); i++){
    	    isTaken[parentPhysicalSpots.get(i)]++;//add reference
    	    physicalChild.add(parentPhysicalSpots.get(i));
    	}
    	lockPhysical.release();
    	
    	
    }
    
    /**
     * 
     * @return
     */
    public int getPagesForStackSize(){
	return (int)(AddrSpace.getUserStackSize() / Machine.PageSize);
    }
}
