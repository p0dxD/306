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
    
    public Paging(DiskDriver diskCache){
	    Debug.print('f', "Initializing the file system.\n");
	    this.diskCache = diskCache;
	    numDiskSectors = diskCache.getNumSectors();
	    diskSectorSize = diskCache.getSectorSize();
	    System.out.println("This cache has " + diskCache.getNumSectors() + " sectors.");
	    System.out.println("Each sector is of size " + diskCache.getSectorSize() + ".");
    }
}
