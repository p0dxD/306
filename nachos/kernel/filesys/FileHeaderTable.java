package nachos.kernel.filesys;

import java.util.ArrayList;
import java.util.HashMap;

import nachos.kernel.threads.Lock;

public class FileHeaderTable {
    
    
    private HashMap<FileHeader, Lock> lockMap = new HashMap<>();
    

    /** empty constructor */
    public FileHeaderTable() {
	
    }
    
    /** opens a particular fileheader for read or write
     * @param f  (fileheader to access)
     * @return true if the fileheader doesn't exist and can be opened.
     * return false if the fileheader is already open (in use)
     */
    public void openFileHeader(FileHeader f) {
	// is the fileheader already open? if so, get and wait on lock
	if (lockMap.containsKey(f))
	    lockMap.get(f).acquire();
	// otherwise, make a new lock and associate it with this header
	else {
	    Lock newLock = new Lock("for fileheader " + f.getDisc());
	    lockMap.put(f, newLock);
	}
	
    }
    
    public void closeFileHeader(FileHeader f) {
	// is anyone waiting on the lock? if so, leave it open.
	
	// otherwise, close it.
    }

}
