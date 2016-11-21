package nachos.util;
import java.util.ArrayList;
import nachos.kernel.filesys.WorkEntry;
import nachos.kernel.threads.Semaphore;

public class CSCANQueue implements Queue<WorkEntry> {
    
    private int currentPos;
    
    private ArrayList<WorkEntry> cscanQueue;
    
    private Semaphore s = new Semaphore("Add/Del Queue Sem", 1);
    
    
    public CSCANQueue () {
	currentPos = 0;
    }

    @Override
    public boolean offer(WorkEntry newEntry) {
	// lock, only one item can be added/deleted at a time
	s.P();
	int sector = newEntry.getSectorNumber();
	if (cscanQueue.size() == 0) {
	    cscanQueue.add(newEntry);
	    s.V();
	    return true;
	}
	// find the correct position to place in queue
	int min = currentPos;
	int max = cscanQueue.get(0).getSectorNumber();;
	for (int i=0; i < cscanQueue.size(); i++) {
	    
	    if(sector >= min && sector <= max) {
		cscanQueue.add(i, newEntry);
		s.V();
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
		    s.V();
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
			break;
		    }
		}
	    }
	}
	s.V();
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
	s.P();
	if (cscanQueue.size() == 0) {
	    s.V();
	    return null;
	}
	else {
	    WorkEntry w = cscanQueue.get(0);
	    cscanQueue.remove(0);
	    currentPos = w.getSectorNumber();
	    s.V();
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
