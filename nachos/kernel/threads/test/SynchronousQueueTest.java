package nachos.kernel.threads.test;

import nachos.kernel.Nachos;
import nachos.machine.NachosThread;
import nachos.util.SynchronousQueue;

public class SynchronousQueueTest implements Runnable {
    private SynchronousQueue synchronousQueue;

    public SynchronousQueueTest(SynchronousQueue queue){
	this.synchronousQueue = queue;
    }
	

    public static void start(){
	NachosThread thread = new NachosThread("SynchronousQueueTest",  new SynchronousQueueTest(new SynchronousQueue()));
	System.out.println("in dynch");
	Nachos.scheduler.readyToRun(thread);
	
    }
   
    @Override
    public void run() {
//	test1();
//	test2();
	test3();
	Nachos.scheduler.finishThread();

    }
    
    
    public void test1(){
	// TODO Auto-generated method stub
	System.out.println("TEST 1: Produce 3 sequentially, Consume 3 sequentially");
	NachosThread t1 = new NachosThread("test1() Thread1:",  new ProducerTest<String>("test1() Produce1", synchronousQueue));
	NachosThread t2 = new NachosThread("test1() Thread2:",  new ProducerTest<String>("test1() Produce2",synchronousQueue));
	NachosThread t3 = new NachosThread("test1() Thread3:",  new ProducerTest<String>("test1() Produce3",synchronousQueue));
	
	NachosThread t4 = new NachosThread("test1() Thread4:",  new ConsumerTest<String>("test1() Consume1",synchronousQueue));
	NachosThread t5 = new NachosThread("test1() Thread5:",  new ConsumerTest<String>("test1() Consume2",synchronousQueue));
	NachosThread t6 = new NachosThread("test1() Thread6:",  new ConsumerTest<String>("test1() Consume3",synchronousQueue));
	Nachos.scheduler.readyToRun(t1);
	Nachos.scheduler.readyToRun(t2);
	Nachos.scheduler.readyToRun(t3);
	Nachos.scheduler.readyToRun(t4);
	Nachos.scheduler.readyToRun(t5);
	Nachos.scheduler.readyToRun(t6);
    }
    
    public void test2(){
	// TODO Auto-generated method stub
	System.out.println("TEST 2: Consume 3 sequentially, then Produce 3 sequentially");

	NachosThread t4 = new NachosThread("test2() Thread4:",  new ConsumerTest<String>("test2() Consume1",synchronousQueue));
	NachosThread t5 = new NachosThread("test2() Thread5:",  new ConsumerTest<String>("test2() Consume2",synchronousQueue));
	NachosThread t6 = new NachosThread("test2() Thread6:",  new ConsumerTest<String>("test2() Consume3",synchronousQueue));
	
	NachosThread t1 = new NachosThread("test2() Thread1:",  new ProducerTest<String>("test2() Produce1", synchronousQueue));
	NachosThread t2 = new NachosThread("test2() Thread2:",  new ProducerTest<String>("test2() Produce2",synchronousQueue));
	NachosThread t3 = new NachosThread("test2() Thread3:",  new ProducerTest<String>("test2() Produce3",synchronousQueue));
	Nachos.scheduler.readyToRun(t1);
	Nachos.scheduler.readyToRun(t2);
	Nachos.scheduler.readyToRun(t3);
	Nachos.scheduler.readyToRun(t4);
	Nachos.scheduler.readyToRun(t5);
	Nachos.scheduler.readyToRun(t6);
    }
    
    public void test3(){
   	// TODO Auto-generated method stub
   	System.out.println("TEST 3: More Producers than Consumers");
   	NachosThread t1 = new NachosThread("test3() Thread1:",  new ProducerTest<String>("test3() Produce1", synchronousQueue));
//   	NachosThread t2 = new NachosThread("test3() Thread2:",  new ProducerTest<String>("test3() Produce2",synchronousQueue));
//   	NachosThread t3 = new NachosThread("test3() Thread3:",  new ProducerTest<String>("test3() Produce3",synchronousQueue));
   	
   	NachosThread t4 = new NachosThread("test3() Thread4:",  new ConsumerTest<String>("test3() Consume1",synchronousQueue));
   	NachosThread t5 = new NachosThread("test3() Thread5:",  new ConsumerTest<String>("test3() Consume2",synchronousQueue));
   	Nachos.scheduler.readyToRun(t1);
//   	Nachos.scheduler.readyToRun(t2);
//   	Nachos.scheduler.readyToRun(t3);
   	Nachos.scheduler.readyToRun(t4);
   	Nachos.scheduler.readyToRun(t5);
       }
    
    
    
    class ProducerTest<T> implements Runnable{
	public String message;
	private SynchronousQueue<T> syncQueue;
	
	public ProducerTest(String message, SynchronousQueue<T> queue){
	    this.message = message;
	    this.syncQueue = queue;
	}
	
	@Override
	public void run() {
	    // TODO Auto-generated method stub
	    T obj = (T) new Object();
	    System.out.println(message + " : putting " + obj);
	    syncQueue.put(obj);
	    Nachos.scheduler.finishThread();
	}

    }
    
    class ConsumerTest<T>implements Runnable{
	public String message;
	private SynchronousQueue<T> syncQueue;
	
	public ConsumerTest(String message, SynchronousQueue<T> queue){
	    this.message = message;
	    this.syncQueue = queue;
	}
	
	@Override
	public void run() {
	    // TODO Auto-generated method stub
	    T obj;
	    obj = syncQueue.take();
	    System.out.println(message + " : taking " + obj);
	    Nachos.scheduler.finishThread();
	}

    }
    
}
