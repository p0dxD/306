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
	      
	   System.out.println ( "Priority queue values are: ");
	      
	   while (it.hasNext()){
	       UserThread ut = ((UserThread)it.next());
	       System.out.print( "CurrentTime: "+ ut.getStartTime()); 
	       ut.setStartTime(ut.getStartTime()+100);       
	       System.out.print( "new Time: "+ ut.getStartTime()); 
	   }
	   System.out.println();
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
