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

    
    public static void resort(Queue<NachosThread> pq, Comparator<NachosThread> cmp){
	Comparator<NachosThread> spn = new SPNComparator();
    	Queue<NachosThread> readyListUser = new PriorityQueue<NachosThread>(1,spn);
	
	
	@SuppressWarnings("unchecked")
	Iterator<NachosThread> it = ((java.util.PriorityQueue<NachosThread>) pq).iterator();
	while(it.hasNext()){
	    readyListUser.offer(it.next());
	}
	pq = readyListUser;
	System.out.println("readyListUser elements");
	 ((PriorityQueue<NachosThread>) readyListUser).displayElements();
	
    }
    
    public void displayElements(){
	
	   Iterator<T> it = this.iterator();
	      
	   System.out.println ( "Priority queue values are: ");
	      
	   while (it.hasNext()){
	       System.out.print( "Value: "+ ((UserThread)it.next()).getBurst()); 
	   }
	   System.out.println();
    }
}
