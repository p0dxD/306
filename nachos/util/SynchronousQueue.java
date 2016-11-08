package nachos.util;

import java.util.concurrent.atomic.AtomicBoolean;

import nachos.Debug;
import nachos.kernel.threads.Callout;
import nachos.kernel.threads.Condition;
import nachos.kernel.threads.Lock;
import nachos.kernel.threads.Semaphore;
import nachos.kernel.threads.SpinLock;


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
    private Queue<T> buffer;
    private static final SpinLock mutex = new SpinLock("callout mutex");
//    private Queue<T> takeOffers;
    private Callout callout;
    //Timeout return variables. 
    private boolean status;
//    private T obj;
    private int consumers =0, producers = 0;
    
    
    /**
     * Initialize a new SynchronousQueue object.
     */
    public SynchronousQueue() {
	//semaphores
	dataSemaphore = new Semaphore("dataSemaphore",0);
	spaceSemaphore = new Semaphore("spaceSemaphore",0);
	bufferLock = new Semaphore("bufferLock",1);
	//queues
	buffer = new FIFOQueue<>();
//	takeOffers = new FIFOQueue<>();
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
	Debug.println('Q', "Put: adding into putOffers object " + obj);
	buffer.offer(obj);
	producers++;
	bufferLock.V();
	
	//update semaphores to wake up consumer threads. 
	Debug.println('Q', "Put: hanging on consumer to take this");
	dataSemaphore.V(); // we have data now
	spaceSemaphore.P(); //wait until consumer comes in takes object
	bufferLock.P();
	producers--;
	bufferLock.V();
	Debug.println('Q', "Put: returning now.");
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
	bufferLock.P();
	consumers++;
	bufferLock.V();
	
	Debug.println('Q', "Take: waiting on data available P()");
	dataSemaphore.P(); //BLOCK: wait until there is data
	bufferLock.P();

	Debug.println('Q', "Take: taking from putOffers");
	returnObject = buffer.poll();
	consumers--;
	bufferLock.V();	
	spaceSemaphore.V(); //FREE: Hanging producers, we have one more space

	Debug.println('Q', "Take: returning object " + returnObject);
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
	producers++;
	bufferLock.V();
	
	bufferLock.P();
	Debug.println('Q', "Offer: checking for available consumers");
	if(consumers <= 0){ //no thread to take this object
	    producers--;
	    bufferLock.V();
	    Debug.println('Q', "Offer: no consumers found returning");
	    return false;
	}
	Debug.println('Q', "Offer: consumer found putting " + e);
	buffer.offer(e);
	bufferLock.V();
	
	//update semaphores to wake up consumer threads. 
	dataSemaphore.V(); // we have data now
	spaceSemaphore.P(); //balances out the V() of consumer, should never actually hang

	bufferLock.P();
	producers--;
	bufferLock.V();
	Debug.println('Q', "Offer: not hanging.");
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
	consumers++;
	bufferLock.V();
	
	bufferLock.P();
	Debug.println('Q', "Poll: checking for available producers");
	if(producers <= 0){ //no thread offering an object
	    //release lock and return immediately. 
	    consumers--;
	    Debug.println('Q', "Poll: no producers found returning");
	    bufferLock.V();
	    return null;
	}
	//take out the object and return it. 
	obj = buffer.poll();
	Debug.println('Q', "Poll: got " + obj);
	consumers--;
	bufferLock.V();
	
	//update semaphores to wake up producer threads. 
	
	dataSemaphore.P(); // less data, balances out the V() on dataSemaphore of the producer. Should never hang
	spaceSemaphore.V(); //Update: we have space now
	Debug.println('Q', "Poll: not hanging.");
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
	
	bufferLock.P();
	Semaphore sem = new Semaphore("Sleeping semaphore", 0);
	callout.schedule(new Runnable(){
	    public void run(){
		    
		mutex.acquire();
		producers++;
		mutex.release();
		Debug.println('Q', "Offer: awaking  " + timeout);
		sem.V();
	
		
	    };
	}, timeout);
	
	bufferLock.V();
	Debug.println('Q', "Offer: sleeping for " + timeout);
	sem.P();
	
	
	bufferLock.P();
	Debug.println('Q', "Offer: checking for available consumers");
	if(consumers <= 0){ //no thread to take this object
	    producers--;
	    bufferLock.V();
	    Debug.println('Q', "Offer: no consumers found returning");
	    return false;
	}
	Debug.println('Q', "Offer: consumer found putting " + e);
	buffer.offer(e);
	bufferLock.V();
	
	//update semaphores to wake up consumer threads. 
	dataSemaphore.V(); // we have data now
	spaceSemaphore.P(); //balances out the V() of consumer, should never actually hang

	bufferLock.P();
	producers--;
	bufferLock.V();
	Debug.println('Q', "Offer: not hanging.");
	return true;


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
	
	bufferLock.P();
	Semaphore sem = new Semaphore("Sleeping semaphore", 0);
	callout.schedule(new Runnable(){
	    public void run(){
		    
		mutex.acquire();
		consumers++;
		mutex.release();
		Debug.println('Q', "Poll: awaking  " + timeout);
		sem.V();
		
		
	    };
	}, timeout);
	
	bufferLock.V();
	
	Debug.println('Q', "Offer: sleeping for " + timeout);
	sem.P();
	
	T obj; 
	
	bufferLock.P();
	Debug.println('Q', "Poll: checking for available producers");
	if(producers <= 0){ //no thread offering an object
	    //release lock and return immediately. 
	    consumers--;
	    Debug.println('Q', "Poll: no producers found returning");
	    bufferLock.V();
	    return null;
	}
	//take out the object and return it. 
	obj = buffer.poll();
	Debug.println('Q', "Poll: got " + obj);
	consumers--;
	bufferLock.V();
	
	//update semaphores to wake up producer threads. 
	
	dataSemaphore.P(); // less data, balances out the V() on dataSemaphore of the producer. Should never hang
	spaceSemaphore.V(); //Update: we have space now
	Debug.println('Q', "Poll: not hanging.");
	return obj;
    }

    @Override
    public int size() {
	// TODO Auto-generated method stub
	return 0;
    }
    
}