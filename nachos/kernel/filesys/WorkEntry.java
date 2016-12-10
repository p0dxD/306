package nachos.kernel.filesys;

import nachos.kernel.threads.Semaphore;

/**
 * The entries in your work queue will have to contain information 
 * sufficient for the interrupt service routine to accomplish its task. 
 * In particular, a work queue entry will have to contain the number of 
 * the sector to be read or written, a flag indicating whether read or write 
 * access is desired, a reference to the kernel buffer to be used for the 
 * transfer, and also a semaphore on which the requesting process sleeps 
 * until the request has been processed.
 * @author jose
 *
 */
public class WorkEntry {
    private Semaphore semaphore;
    private int sectorNumber;
    private char taskToBeCompleted = '\0';//w for write, r for read
    private byte[] buffer;
    private int indexOffset;
    
    /**
     * A WorkEntry consists of four parts, sector, buffer, task, and a semaphore
     * @param sectorNumber where to write or read from
     * @param buffer the contents
     * @param taskToBeCompleted either write or read
     */
    public WorkEntry(int sectorNumber, byte[] buffer,int indexOffset, char taskToBeCompleted){
	semaphore = new Semaphore("WorkEntry",0);
	this.sectorNumber = sectorNumber;
	this.buffer = buffer;
	this.taskToBeCompleted = taskToBeCompleted;
	this.setIndexOffset(indexOffset);
    }
    
    
    /*Setters and getters for the above instance variables*/
    public Semaphore getSemaphore() {
	return semaphore;
    }
    public void setSemaphore(Semaphore semaphore) {
	this.semaphore = semaphore;
    }
    
    public int getSectorNumber() {
	return sectorNumber;
    }
    public void setSectorNumber(int sectorNumber) {
	this.sectorNumber = sectorNumber;
    }
    
    public char getTaskToBeCompleted() {
	return taskToBeCompleted;
    }
    public void setTaskToBeCompleted(char taskToBeCompleted) {
	this.taskToBeCompleted = taskToBeCompleted;
    }
    
    public byte[] getBuffer() {
	return buffer;
    }
    public void setBuffer(byte[] buffer) {
	this.buffer = buffer;
    }


    public int getIndexOffset() {
	return indexOffset;
    }


    public void setIndexOffset(int indexOffset) {
	this.indexOffset = indexOffset;
    }
    
}