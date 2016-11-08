package nachos.util;
import java.util.Comparator;

import nachos.kernel.Nachos;
import nachos.kernel.userprog.UserThread;
import nachos.machine.NachosThread;

public class HRRNComparator implements Comparator<NachosThread>{

    @Override
    public int compare(NachosThread o1, NachosThread o2) {
	int timeWaitedO1 = Nachos.scheduler.getCurrentTime() - ((UserThread)o1).getStartTime();
	int timeWaitedO2 = Nachos.scheduler.getCurrentTime() - ((UserThread)o2).getStartTime();
	
	int responseRatioO1 = (timeWaitedO1 + ((UserThread)o1).getBurst())/((UserThread)o1).getBurst();
	int responseRatioO2 = (timeWaitedO2 + ((UserThread)o2).getBurst())/((UserThread)o2).getBurst();

	if (responseRatioO1 > responseRatioO2)
	    return 1;
	if (responseRatioO1 < responseRatioO2)
	    return -1;
	return 0;
	
	

    }

}
