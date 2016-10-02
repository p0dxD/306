package nachos.kernel.threads.test;
import nachos.kernel.threads.*;

public class CalloutTest implements Runnable{
    public CalloutTest(){
	Test t1 = new Test("Test 1");
	Test t2 = new Test("Test 2");
	Callout call = new Callout();
	call.schedule(t1, 200);
	call.schedule(t2, 159);
	
    }
    public static void start(){
	new CalloutTest();
    }
   

    @Override
    public void run() {
	// TODO Auto-generated method stub
	
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
