package nachos.util;
import java.util.ArrayList;
import nachos.kernel.filesys.WorkEntry;
import nachos.kernel.threads.Semaphore;
import nachos.kernel.threads.SpinLock;

public class CSCANQueue implements Queue<WorkEntry> {
    
    private int currentPos;
    
    private ArrayList<WorkEntry> cscanQueue;
    
    private SpinLock s = new SpinLock("Add/Del Queue Sem");
    
    
    public CSCANQueue () {
	currentPos = 0;
	cscanQueue = new ArrayList<>();
    }

    @Override
    public boolean offer(WorkEntry newEntry) {
//	System.out.println("Adding to queue");
	// lock, only one item can be added/deleted at a time
//	s.P();
//	s.acquire();
//	System.out.println("Adding to queue inside");
	int sector = newEntry.getSectorNumber();
	if (cscanQueue.size() == 0) {
	    cscanQueue.add(newEntry);
//	    System.out.println("returning adding size 0");
//	    s.V();
//	    s.release();
	    return true;
	}
	// find the correct position to place in queue
	int min = currentPos;
	int max = cscanQueue.get(0).getSectorNumber();;
	for (int i=0; i < cscanQueue.size(); i++) {
	    
	    if(sector >= min && sector <= max) {
		cscanQueue.add(i, newEntry);
//		s.V();
//		s.release();
//		System.out.println("returning adding");
		return true;
	    }
	    else if (sector >= min && sector > max) {
		min = max;	    
		max = cscanQueue.get(i).getSectorNumber();
	    }
	    else {
		// sector is less than the current position and
		// needs to be moved to 'next cycle'
		
		// if we are at the end of the list, add to the end
		if (i == cscanQueue.size()-1) {
		    cscanQueue.add(newEntry);
//		    s.V();
//		    s.release();
//		    System.out.println("returning adding");
		    return true;
		}
		// otherwise, min = 0, and max = first sector on 'next' cycle
		min = 0;
		for (int j=i+1; j < cscanQueue.size(); j++) {
		    if (cscanQueue.get(j).getSectorNumber() < currentPos) {
			// move i to position j (first of next cycle) and set 
			// max to the first sector in the next cycle
			i = j-1;
			max = cscanQueue.get(j).getSectorNumber();
		    }
		}
	    }
	}
//	s.V();
//	s.release();
//	System.out.println("returning adding");
	return false;
    }

    @Override
    public WorkEntry peek() {
	if (cscanQueue.size() == 0)
	    return null;
	else 
	    return cscanQueue.get(0);
    }

    @Override
    public WorkEntry poll() {
//	s.P();
//	s.acquire();
	if (cscanQueue.size() == 0) {
//	    System.out.println("removing empty queue");
//	    s.V();
//	    s.release();
	    return null;
	}
	else {
//	    System.out.println("removing not empty queue");
	    WorkEntry w = cscanQueue.get(0);
	    cscanQueue.remove(0);
	    currentPos = w.getSectorNumber();
//	    s.V();
//	    s.release();
	    return w;
	}
    }

    @Override
    public boolean isEmpty() {
	return cscanQueue.isEmpty();
    }

    @Override
    public int size() {
	return cscanQueue.size();
    }
    
    

}
