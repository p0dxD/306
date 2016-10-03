package nachos.util;

import nachos.kernel.threads.Callout;
import nachos.kernel.threads.Semaphore;

/**
 * This class is patterned after the SynchronousQueue class
 * in the java.util.concurrent package.
 *
 * A SynchronousQueue has no capacity: each insert operation
 * must wait for a corresponding remove operation by another
 * thread, and vice versa.  A thread trying to insert an object
 * enters a queue with other such threads, where it waits to
 * be matched up with a thread trying to remove an object.
 * Similarly, a thread trying to remove an object enters a
 * queue with other such threads, where it waits to be matched
 * up with a thread trying to insert an object.
 * If there is at least one thread waiting to insert and one
 * waiting to remove, the first thread in the insertion queue
 * is matched up with the first thread in the removal queue
 * and both threads are allowed to proceed, after transferring
 * the object being inserted to the thread trying to remove it.
 * At any given time, the <EM>head</EM> of the queue is the
 * object that the first thread on the insertion queue is trying
 * to insert, if there is any such thread, otherwise the head of
 * the queue is null.
 */

public class SynchronousQueue<T> implements Queue<T> {
    private Semaphore dataSemaphore;
    private Semaphore spaceSemaphore;
    private Semaphore bufferLock;
    private Queue<T> putOffers;
    private Queue<T> takeOffers;
    private Callout callout;
    //Timeout return variables. 
    private boolean status;
    private T obj;
    
    
    
    /**
     * Initialize a new SynchronousQueue object.
     */
    public SynchronousQueue() {
	//semaphores
	dataSemaphore = new Semaphore("dataSemaphore",0);
	spaceSemaphore = new Semaphore("spaceSemaphore",0);
	bufferLock = new Semaphore("bufferLock",1);
	//queues
	putOffers = new FIFOQueue();
	takeOffers = new FIFOQueue();
	//callout
	callout = new Callout();
    }

    /**
     * Adds the specified object to this queue,
     * waiting if necessary for another thread to remove it.
     *
     * @param obj The object to add.
     */
    public boolean put(T obj) {
	if(obj == null){
	    return false;
	}
	
	bufferLock.P();
	System.out.println("put(): adding into putOffers");
	putOffers.offer(obj);
	bufferLock.V();
	
	//update semaphores to wake up consumer threads. 
	dataSemaphore.V(); // we have data now
	spaceSemaphore.P(); //wait until there is space available
	System.out.println("put(): returning now");
	return true;
    }

    /**
     * Retrieves and removes the head of this queue,
     * waiting if necessary for another thread to insert it.
     *
     * @return the head of this queue.
     */
    public T take() {
	T returnObject;
	System.out.println("take(): waiting on data available P()");
	dataSemaphore.P(); // wait until there is data
	bufferLock.P();
	System.out.println("take(): taking from putOffers");
	returnObject = putOffers.poll();
	bufferLock.V();	
	spaceSemaphore.V(); // we have one more space
	System.out.println("take(): returning object "+returnObject);
	return returnObject;
    }

    /**
     * Adds an element to this queue, if there is a thread currently
     * waiting to remove it, otherwise returns immediately.
     * 
     * @param e  The element to add.
     * @return  true if the element was successfully added, false if the element
     * was not added.
     */
    @Override
    public boolean offer(T e) {
	bufferLock.P();
	if(takeOffers.isEmpty()){ //no thread to take this object
	    bufferLock.V();
	    return false;
	}
	putOffers.offer(e);
	bufferLock.V();
	
	//update semaphores to wake up consumer threads. 
	spaceSemaphore.P(); //wait until there is space available
	dataSemaphore.V(); // we have data now
	
	return true;
    }
    
    /**
     * Retrieves and removes the head of this queue, if another thread
     * is currently making an element available.
     * 
     * @return  the head of this queue, or null if no element is available.
     */
    @Override
    public T poll() { 
	T obj; 
	
	bufferLock.P();
	if(putOffers.isEmpty()){ //no thread offering an object
	    //release lock and return immediately. 
	    bufferLock.V();
	    return null;
	}
	//take out the object and return it. 
	obj = putOffers.poll();
	bufferLock.V();
	
	//update semaphores to wake up producer threads. 
	spaceSemaphore.V(); //wait until there is space available
	dataSemaphore.P(); // we have data now
	return obj;

    }
    
    /**
     * Always returns null.
     *
     * @return  null
     */
    @Override
    public T peek() { return null; }
    
    /**
     * Always returns true.
     * 
     * @return true
     */
    @Override
    public boolean isEmpty() { return true; }

    // The following methods are to be implemented for the second
    // part of the assignment.

    /**
     * Adds an element to this queue, waiting up to the specified
     * timeout for a thread to be ready to remove it.
     * 
     * @param e  The element to add.
     * @param timeout  The length of time (in "ticks") to wait for a
     * thread to be ready to remove the element, before giving up and
     * returning false.
     * @return  true if the element was successfully added, false if the element
     * was not added.
     */
    public boolean offer(T e, int timeout) {
	callout.schedule(new Runnable(){
	    public void run(){
		bufferLock.P();
		if(takeOffers.isEmpty()){
		    status = false;
		}else{
		    putOffers.offer(e);
		    status = true;
		}
		bufferLock.V();
		//wake up consumer threads to take the object
		spaceSemaphore.P();
		dataSemaphore.V();
	    };
	}, timeout);
	return status;
    }
    
    /**
     * Retrieves and removes the head of this queue, waiting up to the
     * specified timeout for a thread to make an element available.
     * 
     * @param timeout  The length of time (in "ticks") to wait for a
     * thread to make an element available, before giving up and returning
     * true.
     * @return  the head of this queue, or null if no element is available.
     */
    public T poll(int timeout) {
	callout.schedule(new Runnable(){
	    public void run(){
		bufferLock.P();
		if(putOffers.isEmpty()){
		    obj = null;
		}else{
		    obj = putOffers.poll();
		}
		bufferLock.V();
		//wake up producer threads 
		dataSemaphore.P();
		spaceSemaphore.V();
	    };
	}, timeout);
	return obj;
    }
}