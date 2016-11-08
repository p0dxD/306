package nachos.util;
import java.util.Comparator;

import nachos.kernel.Nachos;
import nachos.kernel.userprog.UserThread;
import nachos.machine.NachosThread;

public class HRRNComparator implements Comparator<NachosThread>{

    @Override
    public int compare(NachosThread o1, NachosThread o2) {
	
	// If this is a NachosThread that is not a UserThread, it's priority is higher than
	// UserThreads (it is in kernel mode).
	if (!o1.getClass().equals("UserThread"))
	    return 1;
		
	// Otherwise, check it against other UserThreads.
	int timeWaitedO1 = Nachos.scheduler.getCurrentTime() - ((UserThread)o1).timeInserted;
	int timeWaitedO2 = Nachos.scheduler.getCurrentTime() - ((UserThread)o2).timeInserted;
	
	int responseRatioO1 = (timeWaitedO1 + ((UserThread)o1).getBurst())/((UserThread)o1).getBurst();
	int responseRatioO2 = (timeWaitedO2 + ((UserThread)o2).getBurst())/((UserThread)o2).getBurst();

	if (responseRatioO1 > responseRatioO2)
	    return 1;
	if (responseRatioO1 < responseRatioO2)
	    return -1;
	return 0;
    }

}
