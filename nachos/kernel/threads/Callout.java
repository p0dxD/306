package nachos.kernel.threads;

import java.util.HashMap;
import java.util.PriorityQueue;

import nachos.Debug;
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
	Debug.println('C', "****Callout created****");
	timer = Machine.getTimer(0);
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
	int oldLevel = CPU.setLevel(CPU.IntOff);
	mutex.acquire();
	Long wakeUpTime = new Long(currentTime + ticksFromNow);
	scheduledEvents.put(wakeUpTime, runnable);
	runnablesQueue.add(wakeUpTime);
	Debug.println('C', "Callout schedule" +" runnable added, expected run time " + ticksFromNow);
	mutex.release();
	CPU.setLevel(oldLevel);
    }
    
    
    public static class TimerInterrupt implements InterruptHandler{
	private final Timer timer;
	
	public TimerInterrupt(Timer timer){
	    this.timer = timer;
	}
	
	@Override
	public void handleInterrupt() {

	    mutex.acquire();
	    currentTime += 100;
	    Long nextWakeTime = runnablesQueue.peek();
	    if(nextWakeTime == null){
		Callout.timer.stop();
	    }
	    if(nextWakeTime != null && nextWakeTime <= currentTime){
		Debug.println('C', "Callout HandleInterrupt: " +"running ticks " + nextWakeTime + " runnable");
		Runnable event = scheduledEvents.get(nextWakeTime);
		event.run();
		runnablesQueue.poll(); // remove from the queue.
		
	    }
	    mutex.release();

	}
	
    }
    
}
