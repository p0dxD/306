package nachos.kernel.threads.test;
import nachos.kernel.Nachos;
import nachos.kernel.filesys.test.FileSystemTest;
import nachos.kernel.threads.*;
import nachos.machine.NachosThread;

public class CalloutTest implements Runnable{
    public CalloutTest(){

	
    }
    public static void start(){
	NachosThread thread = new NachosThread("Callout test",  new CalloutTest());
	Nachos.scheduler.readyToRun(thread);
	
    }
   

    @Override
    public void run() {
	// TODO Auto-generated method stub
	System.out.println("Test run");
	Test t1 = new Test("Test 1");
	Test t2 = new Test("Test 2");
	
	Callout call = new Callout();
	
	call.schedule(t1, 100);
	
	call.schedule(t2, 159);
	
	Nachos.scheduler.finishThread();
    }
    
    
    
    class Test implements Runnable{
	public String message;
	
	public Test(String message){
	    this.message = message;
	}
	
	@Override
	public void run() {
	    // TODO Auto-generated method stub
	    System.out.println(message);
	    
	}
	
    }
}
