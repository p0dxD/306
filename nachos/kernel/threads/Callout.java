package nachos.kernel.threads;

import java.util.HashMap;
import java.util.PriorityQueue;

import nachos.machine.CPU;
import nachos.machine.InterruptHandler;
import nachos.machine.Machine;
import nachos.machine.Timer;

public class Callout {
    private final PriorityQueue<Long> runnablesQueue = new PriorityQueue<>();
    private final HashMap<Long, Runnable> scheduledEvents = new HashMap<>();
    private static Timer timer;
    private static int currentTime;
    public Callout(){
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

	Long wakeUpTime = new Long(currentTime + ticksFromNow);
	scheduledEvents.put(wakeUpTime, runnable);
	runnablesQueue.add(wakeUpTime);
	
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
	    // TODO Auto-generated method stub
	    currentTime += 100;
	    ru
	    
	    
	}
	
    }
    
}
