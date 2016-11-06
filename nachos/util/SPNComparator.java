package nachos.util;

import java.util.Comparator;
import nachos.kernel.userprog.UserThread;
import nachos.machine.NachosThread;

public class SPNComparator implements Comparator<NachosThread>{

    @Override
    public int compare(NachosThread o1, NachosThread o2) {
	if (((UserThread)o1).getBurst() > ((UserThread)o1).getBurst())
	    return 1;
	if (((UserThread)o1).getBurst() < ((UserThread)o1).getBurst())
	    return -1;
	return 0;
    }

}
