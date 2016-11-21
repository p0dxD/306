package nachos.util;
import java.util.ArrayList;
import nachos.kernel.filesys.WorkEntry;
import nachos.kernel.threads.Semaphore;

public class CSCANQueue implements Queue<WorkEntry> {
    
    private int currentPos;
    
    private ArrayList<WorkEntry> cscanQueue = new ArrayList<WorkEntry>();
    
    private Semaphore s = new Semaphore("Add/Del Queue Sem", 1);
    
    
    public CSCANQueue () {
	currentPos = 0;
    }

    @Override
    public boolean offer(WorkEntry newEntry) {
	// lock, only one item can be added/deleted at a time
	//s.P();
	int sector = newEntry.getSectorNumber();
	
	// if queue is empty, or sector == currentPos, just add
	if (cscanQueue.size() == 0 || sector == currentPos) {
	    cscanQueue.add(0, newEntry);
	    currentPos = sector;
	    //s.V();
	    return true;
	}
	
	// find the correct position to place in queue
	for (int i=0; i < cscanQueue.size(); i++) {
	   // if sector = current list item
	   if (sector == cscanQueue.get(i).getSectorNumber()) {
	       cscanQueue.add(i, newEntry);
	       //s.V();
	       return true;
	   }
	   // if we are at the end of the list, place at end
	   else if (i == cscanQueue.size()-1) {
	       cscanQueue.add(newEntry);
	       //s.V();
	       return true;
	   }
	   // if we are not at the end of the queue, do we fit between the 2 indexes we are looking at?
	   else if ((sector > cscanQueue.get(i).getSectorNumber() && sector <= cscanQueue.get(i+1).getSectorNumber())
		   || (sector > cscanQueue.get(i).getSectorNumber() && cscanQueue.get(i+1).getSectorNumber() < currentPos)
		   || (sector < cscanQueue.get(i).getSectorNumber() && sector <= cscanQueue.get(i+1).getSectorNumber() && 
		   cscanQueue.get(i+1).getSectorNumber() < currentPos))	   {
	       cscanQueue.add(i+1, newEntry);
	       //s.V();
	       return true;
	   }
	}
	//s.V();
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
	//s.P();
	if (cscanQueue.size() == 0) {
	    //s.V();
	    return null;
	}
	else {
	    WorkEntry w = cscanQueue.get(0);
	    cscanQueue.remove(0);
	    currentPos = w.getSectorNumber();
	    //s.V();
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
