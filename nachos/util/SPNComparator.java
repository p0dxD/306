package nachos.util;

import java.util.Comparator;
import nachos.kernel.userprog.UserThread;
import nachos.machine.NachosThread;

public class SPNComparator implements Comparator<NachosThread>{

    @Override
    public int compare(NachosThread o1, NachosThread o2) {
	return getBurstOfUser(o1) <  getBurstOfUser(o2) ? -1 : 
	    (getBurstOfUser(o1)  == getBurstOfUser(o2) ? 0 : 1);
    }

    public int getBurstOfUser(NachosThread o){
	return ((UserThread)o).getBurst();
    }
    
}
