package nachos.util;

import java.util.Comparator;

import nachos.kernel.Nachos;
import nachos.kernel.userprog.UserThread;
import nachos.machine.NachosThread;

public class SRTComparator implements Comparator<NachosThread>{

    @Override
    public int compare(NachosThread o1, NachosThread o2) {
	
	// If this is a NachosThread that is not a UserThread, it's priority is higher than
	// UserThreads.
	if (!o1.getClass().equals("UserThread"))
	    return 1;
	
	// Otherwise, check it against other UserThreads.
	int timeRemainingO1 = ((UserThread)o1).getBurst() - ((UserThread)o1).timeExecuted;
	int timeRemainingO2 = ((UserThread)o1).getBurst() - ((UserThread)o1).timeExecuted;

	if (timeRemainingO2 > timeRemainingO1)
	    return 1;
	if (timeRemainingO2 < timeRemainingO1)
	    return -1;
	return 0;
    }

}
