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
    
    /**map of sectors to entries*/
    ArrayList<swapMapEntry> mappingList;
    
    /**ID of eviction for FIFO*/
    int evictionID;
    
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
	    evictionID = 0;
    }
    
    
    /**
     * Adds to physical extended location
     */
    public void getPages(int numPages){
	int[] pageEntries = new int[numPages];
	// find not in use sector for each page
	int position=0;
	for (int i : sectors) {
	    if (i == 0) {
		// give first page location of the not in use sector
		pageEntries[position] = i;
		position++;
		if (position == numPages)
		    // if we are here there was enough space in memory
		    break;
	    }
	}
	if (position != numPages) {
	    //we need to evict
	    while (position != numPages) {
		// remove FIFO by moving evictionID through the table
		pageEntries[position] = evictionID;
		evictionID++;
		position++;
	    }
	}
	// clear memory of sectors
	// zero out here
	
	int vpn, id;
	for (int i : pageEntries) {
	    // add to swap list
	}
	    
    }
	
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
