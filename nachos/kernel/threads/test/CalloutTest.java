package nachos.kernel.threads.test;
import nachos.kernel.threads.*;

public class CalloutTest implements Runnable{
    public CalloutTest(){
//	NachosThread t = new NachosThread("Test", );
//	Callout call = new Callout();
	
    }
    public static void start(){
	new CalloutTest();
    }
   

    @Override
    public void run() {
	// TODO Auto-generated method stub
	
    }
    
    
    
    class Test implements Runnable{

	@Override
	public void run() {
	    // TODO Auto-generated method stub
	    System.out.println("Test");
	}
	
    }
}
