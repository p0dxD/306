package nachos.kernel.userprog;

import java.util.ArrayList;
import java.util.HashMap;

import nachos.Debug;
import nachos.kernel.devices.DiskDriver;
import nachos.kernel.filesys.BitMap;
import nachos.kernel.threads.Lock;


public class Paging {
    /** Access to the disk on which the filesystem resides. */
    private final DiskDriver diskCache;
    
    /** Number of sectors on the disk. */
    public final int numDiskSectors;
    
    /** Sector size of the disk. */
    public final int diskSectorSize;
    
    /**information about the sectors*/
    int[] sectors;
    /**Keeps track of the pages we have allocated physically*/
    ArrayList<DiskAddressMapping> physicalSectorsExtended = new ArrayList<>();
    
    /**Contains the sectors with the DiskAddressMapping*/
    HashMap<DiskAddressMapping, Integer> swapMap = new HashMap<>();
    
    /*Locks*/
    private Lock physicalSectorsExtendedLock;
    private Lock swapMapLock;
    
    public Paging(DiskDriver diskCache){
	    Debug.print('L', "Initializing the cache driver.\n");
	    this.diskCache = diskCache;
	    numDiskSectors = diskCache.getNumSectors();
	    diskSectorSize = diskCache.getSectorSize();
	    physicalSectorsExtendedLock = new Lock("Lock for adding physical pages");
	    swapMapLock= new Lock("SwapMap Lock");
	    
	    sectors = new int[numDiskSectors];
	    //zero them out, not being used
	    for(int i =0; i < numDiskSectors; i++){
		sectors[i] = 0;//initially none in use
	    }
    }
    
    
    /**
     * Adds to physicall extended location
     */
    public void addToMapping(DiskAddressMapping map){
	physicalSectorsExtendedLock.acquire();
	physicalSectorsExtended.add(map);
	physicalSectorsExtendedLock.release();
    }
    
    /**
     * Checks swap to see if it has reference of page 
     * @param map 
     * @return
     */
    public boolean isInSwap(DiskAddressMapping map){
	swapMapLock.acquire();
	boolean contains = swapMap.containsKey(map);
	swapMapLock.release();
	return contains;
    }
    
}
