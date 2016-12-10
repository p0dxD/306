
package nachos.kernel.filesys;

import java.util.ArrayList;
import java.util.HashMap;

import nachos.kernel.threads.Lock;
import nachos.kernel.threads.Semaphore;

public class FileHeaderTable {
    
    
    private HashMap<FileHeader, Lock> lockMap = new HashMap<>();
    
    private HashMap<FileHeader, Integer> threadsWaiting = new HashMap<>();
    
    private Semaphore mutex = new Semaphore("open file header", 1);
    

    /** empty constructor */
    public FileHeaderTable() {
	
    }
    
    /** opens a particular fileheader for read or write
     * @param f  (fileheader to access)
     */
    public void openFileHeader(FileHeader f) {
	// is the fileheader already open? if so, get and wait on lock
	if (lockMap.containsKey(f)) {
	    int numWaiting = threadsWaiting.get(f)+1;
	    threadsWaiting.replace(f,numWaiting);
	    lockMap.get(f).acquire();
	}
	// otherwise, make a new lock and associate it with this header
	else {
	    Lock newLock = new Lock("for fileheader " + f.getDisc());
	    lockMap.put(f, newLock);
	    // also, add to hashmap threadsWaiting, with initial value 0
	    // (no one is waiting yet)
	    threadsWaiting.put(f, 0);
	    //newLock.acquire();
	}

    }
    
    public void closeFileHeader(FileHeader f) {
		
	// is anyone waiting on the lock? if so, leave it open.
	if (threadsWaiting.get(f) != 0) {
	    int numWaiting = threadsWaiting.get(f)-1;
	    threadsWaiting.replace(f,numWaiting);
	    lockMap.get(f).release();
	}
	// otherwise, close it.
	else {
	    //lockMap.get(f).release();
	    lockMap.remove(f);
	    threadsWaiting.remove(f);
	}
	
    }

}