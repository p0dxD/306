package nachos.kernel.threads.test;
import nachos.Debug;
import nachos.kernel.Nachos;
import nachos.kernel.threads.*;
import nachos.machine.NachosThread;

public class CalloutTest implements Runnable{
    public CalloutTest(){

	
    }
    public static void start(){
	NachosThread thread = new NachosThread("Callout test one",  new CalloutTest());
	Nachos.scheduler.readyToRun(thread);
	NachosThread thread2 = new NachosThread("Callout test two",  new CalloutTest());
	Nachos.scheduler.readyToRun(thread2);
	
    }
   

    @Override
    public void run() {

	Test t1 = new Test("Test 1");
	Test t2 = new Test("Test 2");
	Test t3 = new Test("Test 3");
	Test t4 = new Test("Test 4");
	Test t5 = new Test("Test 5");
	Test t6 = new Test("Test 6");
	
	Callout call = new Callout();
	call.schedule(t1, 100);
	call.schedule(t2, 50);
	call.schedule(t3, 300);
	call.schedule(t4, 150);	
	call.schedule(t5, 500);
	call.schedule(t6, 500);
	
	Nachos.scheduler.finishThread();
    }
    
    
    
    class Test implements Runnable{
	public String message;
	
	public Test(String message){
	    this.message = message;
	}
	
	@Override
	public void run() {
	    Debug.println('C', "Run method inside test message: "+ message);
	}
	
    }
}
