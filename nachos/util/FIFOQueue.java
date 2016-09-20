package nachos.util;

/**
 * Implementation of the Queue interface using a LinkedList from the Java API
 * to obtain a queue with first-in, first-out behavior.
 * 
 * @author Eugene W. Stark
 * @version 20140117
 */
@SuppressWarnings("serial")
public class FIFOQueue<T> extends java.util.LinkedList<T> implements Queue<T> {
    private int capacity; //not sure about this
    /**
     * Adds an element to this queue, if it is possible to do so immediately
     * without violating capacity restrictions.
     * 
     * @param e  The element to add.
     * @return  true if the element was successfully added, false if the element
     * was not added.
     */
    public boolean offer(T e){
	return (this.size() <= capacity)? (this.add(e)): false;
    }
    
    /**
     * Retrieves, but does not remove, the head of this queue, or returns null
     * if this queue is empty.
     * 
     * @return  The element at the head of the queue, or null if the queue is
     * empty.
     */
    public T peek(){
	return this.getFirst();
    }
    
    /**
     * Retrieves and removes the head of this queue, or returns null if this queue is empty.
     * 
     * @return  the head of this queue, or null if this queue is empty.
     */
    public T poll(){
	return this.removeFirst();
    }
    
    /**
     * Test whether this queue is currently empty.
     * 
     * @return true if this queue is currently empty.
     */
    public boolean isEmpty(){
	return this.isEmpty();
    }
}
