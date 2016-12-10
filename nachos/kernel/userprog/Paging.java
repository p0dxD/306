package nachos.kernel.userprog;

import nachos.Debug;
import nachos.kernel.devices.DiskDriver;
import nachos.kernel.filesys.BitMap;


public class Paging {
    /** Access to the disk on which the filesystem resides. */
    private final DiskDriver diskCache;
    
    /** Number of sectors on the disk. */
    public final int numDiskSectors;
    
    /** Sector size of the disk. */
    public final int diskSectorSize;
    
    /**information about the sectors*/
    int[] sectors;
    
    public Paging(DiskDriver diskCache){
	    Debug.print('L', "Initializing the cache driver.\n");
	    this.diskCache = diskCache;
	    numDiskSectors = diskCache.getNumSectors();
	    diskSectorSize = diskCache.getSectorSize();
	    sectors = new int[numDiskSectors];
	    for(int i =0; i < numDiskSectors; i++){
		sectors[i] = 0;//initially none in use
	    }
    }
    
    /**
     * Checks to see if a page is contained within the cache
     */
    public void swap(){
	
    }
}
