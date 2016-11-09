package nachos.util;

import java.util.Comparator;
import java.util.Iterator;

import nachos.kernel.userprog.UserThread;
import nachos.machine.NachosThread;

@SuppressWarnings("serial")
public class PriorityQueue<T> extends java.util.PriorityQueue<T> implements Queue<T> {

    public PriorityQueue(int initialCapacity, Comparator<T> comparator) {
	super(initialCapacity, comparator);
    }

    
    public static void incrementTime(PriorityQueue<NachosThread> pq){
	
	   Iterator<NachosThread> it = pq.iterator();
	      
	      
	   while (it.hasNext()){
	       UserThread ut = ((UserThread)it.next());
	       ut.setStartTime(ut.getStartTime()+100);       
	   }
    }
    
}
