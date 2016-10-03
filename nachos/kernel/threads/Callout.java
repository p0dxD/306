package nachos.kernel.threads;

import java.util.HashMap;
import java.util.PriorityQueue;

import nachos.machine.CPU;
import nachos.machine.InterruptHandler;
import nachos.machine.Machine;
import nachos.machine.Timer;

public class Callout {
    private static final PriorityQueue<Long> runnablesQueue = new PriorityQueue<>();
    private static final HashMap<Long, Runnable> scheduledEvents = new HashMap<>();
    public static Timer timer;
    private static int currentTime;
    private static final SpinLock mutex = new SpinLock("callout mutex");
    
    public Callout(){
	timer = Machine.getTimer(0);
	System.out.println("Timer: " +timer.interval);
	timer.setHandler(new TimerInterrupt(timer));
	timer.start();
    }
    
    /**
     * Schedule a callout to occur at a specified number of
     * ticks in the future.
     *
     * @param runnable  A Runnable to be invoked when the specified
     * time arrives.
     * @param ticksFromNow  The number of ticks in the future at
     * which the callout is to occur.
     */
    public void schedule(Runnable runnable, int ticksFromNow){
	//add to priority queue
	System.out.println("Added a runnable");
	int oldLevel = CPU.setLevel(CPU.IntOff);
	mutex.acquire();
	Long wakeUpTime = new Long(currentTime + ticksFromNow);
	scheduledEvents.put(wakeUpTime, runnable);
	runnablesQueue.add(wakeUpTime);
	mutex.release();
	CPU.setLevel(oldLevel);
    }
    
//    public static void main(String args[]){
//	Machine.init();
//	Callout call = new Callout();
//	System.out.println("test" + Machine.getTimer(0));
//	
//    }
    
    public static class TimerInterrupt implements InterruptHandler{
	private final Timer timer;
	
	public TimerInterrupt(Timer timer){
	    this.timer = timer;
	}
	
	@Override
	public void handleInterrupt() {

//	    System.out.println("Test");
	    mutex.acquire();
	    currentTime += 100;
	    Long nextWakeTime = runnablesQueue.peek();
//	    System.out.println("Test");
	    if(nextWakeTime == null){
		Callout.timer.stop();
	    }
	    if(nextWakeTime != null && nextWakeTime <= currentTime){
		System.out.println("handleInterrupt():" + nextWakeTime);
		Runnable event = scheduledEvents.get(nextWakeTime);
		event.run();
		runnablesQueue.poll(); // remove from the queue.
		
	    }
	    mutex.release();

	}
	
    }
    
}
