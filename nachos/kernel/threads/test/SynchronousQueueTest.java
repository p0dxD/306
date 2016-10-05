package nachos.kernel.threads.test;

import nachos.Debug;
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
	Nachos.scheduler.readyToRun(thread);
	
    }
   
    @Override
    public void run() {
	test1();
	test2();
	test3();
	test4();
	test5();
	test6();
	test7();
	test8();
	test9();
	test10();

	test11();
	test12();
	test13();
	test14();
	test15();
	//next have them wakeup  
	Nachos.scheduler.finishThread();

    }
    
    
    public void test1(){

	Debug.println('Q', "TEST 1 put and take: Produce 3 sequentially, Consume 3 sequentially");
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

	Debug.println('Q', "TEST 2 put and take: Creating consumers first equal amount than producers");
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
   	// More consumers than producers
   	Debug.println('Q', "TEST 3 put and take: More Consumers than producers");
   	NachosThread t1 = new NachosThread("test3() Thread1:",  new ProducerTest<String>("test3() Produce1", synchronousQueue));
   	
   	NachosThread t4 = new NachosThread("test3() Thread4:",  new ConsumerTest<String>("test3() Consume1",synchronousQueue));
   	NachosThread t5 = new NachosThread("test3() Thread5:",  new ConsumerTest<String>("test3() Consume2",synchronousQueue));
   	Nachos.scheduler.readyToRun(t1);
   	Nachos.scheduler.readyToRun(t4);
   	Nachos.scheduler.readyToRun(t5);
       }
    
    public void test4(){
   	// More producers  than consumers
   	Debug.println('Q', "TEST 4 put and take: More producers than Consumers");
   	NachosThread t1 = new NachosThread("test4() Thread1:",  new ProducerTest<String>("test4() Produce1", synchronousQueue));
  	NachosThread t5 = new NachosThread("test4() Thread1:",  new ProducerTest<String>("test4() Produce2", synchronousQueue));
  	
   	NachosThread t4 = new NachosThread("test4() Thread4:",  new ConsumerTest<String>("test4() Consume1",synchronousQueue));
   	Nachos.scheduler.readyToRun(t1);
   	Nachos.scheduler.readyToRun(t4);
   	Nachos.scheduler.readyToRun(t5);
       }
     
    public void test5(){

	Debug.println('Q', "TEST 5 poll and offer: Produce 3 sequentially, Consume 3 sequentially");
	NachosThread t1 = new NachosThread("test5() Thread1:",  new ProducerPollOffer<String>("test5() Produce1", synchronousQueue));
	NachosThread t2 = new NachosThread("test5() Thread2:",  new ProducerPollOffer<String>("test5() Produce2",synchronousQueue));
	NachosThread t3 = new NachosThread("test5() Thread3:",  new ProducerPollOffer<String>("test5() Produce3",synchronousQueue));
	
	NachosThread t4 = new NachosThread("test5() Thread4:",  new ConsumerPollOffer<String>("test5() Consume1",synchronousQueue));
	NachosThread t5 = new NachosThread("test5() Thread5:",  new ConsumerPollOffer<String>("test5() Consume2",synchronousQueue));
	NachosThread t6 = new NachosThread("test5() Thread6:",  new ConsumerPollOffer<String>("test5() Consume3",synchronousQueue));
	Nachos.scheduler.readyToRun(t1);
	Nachos.scheduler.readyToRun(t2);
	Nachos.scheduler.readyToRun(t3);
	Nachos.scheduler.readyToRun(t4);
	Nachos.scheduler.readyToRun(t5);
	Nachos.scheduler.readyToRun(t6);
    }
    
    public void test6(){

	Debug.println('Q', "TEST 6 poll and offer: Creating consumers first equal amount than producers");
	NachosThread t4 = new NachosThread("test6() Thread4:",  new ConsumerPollOffer<String>("test6() Consume1",synchronousQueue));
	NachosThread t5 = new NachosThread("test6() Thread5:",  new ConsumerPollOffer<String>("test6() Consume2",synchronousQueue));
	NachosThread t6 = new NachosThread("test6() Thread6:",  new ConsumerPollOffer<String>("test6() Consume3",synchronousQueue));
	
	NachosThread t1 = new NachosThread("test6() Thread1:",  new ProducerPollOffer<String>("test6() Produce1", synchronousQueue));
	NachosThread t2 = new NachosThread("test6() Thread2:",  new ProducerPollOffer<String>("test6() Produce2",synchronousQueue));
	NachosThread t3 = new NachosThread("test6() Thread3:",  new ProducerPollOffer<String>("test6() Produce3",synchronousQueue));
	Nachos.scheduler.readyToRun(t1);
	Nachos.scheduler.readyToRun(t2);
	Nachos.scheduler.readyToRun(t3);
	Nachos.scheduler.readyToRun(t4);
	Nachos.scheduler.readyToRun(t5);
	Nachos.scheduler.readyToRun(t6);
    }
    
    public void test7(){
   	// More consumers than producers
   	Debug.println('Q', "TEST 7 poll and offer: More Consumers than producers");
   	NachosThread t1 = new NachosThread("test7() Thread1:",  new ProducerPollOffer<String>("test7() Produce1", synchronousQueue));
   	
   	NachosThread t4 = new NachosThread("test7() Thread4:",  new ConsumerPollOffer<String>("test7() Consume1",synchronousQueue));
   	NachosThread t5 = new NachosThread("test7() Thread5:",  new ConsumerPollOffer<String>("test7() Consume2",synchronousQueue));
   	Nachos.scheduler.readyToRun(t1);
   	Nachos.scheduler.readyToRun(t4);
   	Nachos.scheduler.readyToRun(t5);
       }
    
    public void test8(){
   	// More producers  than consumers
   	Debug.println('Q', "TEST 8 poll and offer: More producers than Consumers");
   	NachosThread t1 = new NachosThread("test8() Thread1:",  new ProducerPollOffer<String>("test8() Produce1", synchronousQueue));
  	NachosThread t5 = new NachosThread("test8() Thread1:",  new ProducerPollOffer<String>("test8() Produce2", synchronousQueue));
  	
   	NachosThread t4 = new NachosThread("test8() Thread4:",  new ConsumerPollOffer<String>("test8() Consume1",synchronousQueue));
   	Nachos.scheduler.readyToRun(t1);
   	Nachos.scheduler.readyToRun(t4);
   	Nachos.scheduler.readyToRun(t5);
       }  
    public void test9(){
   	// 3 hanging producers and 3 polling threads that take them immediately
   	Debug.println('Q', "TEST 9 poll and offer: 3 hanging producers, 3 poll taking them immediately");
   	NachosThread t1 = new NachosThread("test9() Thread1:",  new ProducerTest<String>("test9() Produce1 [type = hanging]", synchronousQueue));
   	NachosThread t2 = new NachosThread("test9() Thread2:",  new ProducerTest<String>("test9() Produce2 [type = hanging]", synchronousQueue));
   	NachosThread t3 = new NachosThread("test9() Thread3:",  new ProducerTest<String>("test9() Produce3 [type = hanging]", synchronousQueue));

   	NachosThread t4 = new NachosThread("test9() Thread4:",  new ConsumerPollOffer<String>("test9() Consume1 [type = immediate]",synchronousQueue));
   	NachosThread t5 = new NachosThread("test9() Thread5:",  new ConsumerPollOffer<String>("test9() Consume2 [type = immediate]",synchronousQueue));
   	NachosThread t6 = new NachosThread("test9() Thread6:",  new ConsumerPollOffer<String>("test9() Consume3 [type = immediate]", synchronousQueue));

   	Nachos.scheduler.readyToRun(t1);
   	Nachos.scheduler.readyToRun(t2);
   	Nachos.scheduler.readyToRun(t3);

   	Nachos.scheduler.readyToRun(t4);
   	Nachos.scheduler.readyToRun(t5);
   	Nachos.scheduler.readyToRun(t6);

       }
    
    public void test10(){
   	// 3 hanging consumers, three polling threads 
   	Debug.println('Q', "TEST 10 poll and offer: 3 hanging consumers, 3 offers giving their objects immediately");
   	NachosThread t1 = new NachosThread("test10() Thread1:",  new ConsumerTest<String>("test10() Consume1 [type = hanging]", synchronousQueue));
   	NachosThread t2 = new NachosThread("test10() Thread2:",  new ConsumerTest<String>("test10() Consume2 [type = hanging]", synchronousQueue));
   	NachosThread t3 = new NachosThread("test10() Thread3:",  new ConsumerTest<String>("test10() Consume3 [type = hanging]", synchronousQueue));

   	NachosThread t4 = new NachosThread("test10() Thread4:",  new ProducerPollOffer<String>("test10() Produce1 [type = immediate]",synchronousQueue));
   	NachosThread t5 = new NachosThread("test10() Thread5:",  new ProducerPollOffer<String>("test10() Produce2 [type = immediate]",synchronousQueue));
   	NachosThread t6 = new NachosThread("test10() Thread6:",  new ProducerPollOffer<String>("test10() Produce3 [type = immediate]", synchronousQueue));

   	Nachos.scheduler.readyToRun(t1);
   	Nachos.scheduler.readyToRun(t2);
   	Nachos.scheduler.readyToRun(t3);

   	Nachos.scheduler.readyToRun(t4);
   	Nachos.scheduler.readyToRun(t5);
   	Nachos.scheduler.readyToRun(t6);
       }  
    
    public void test11(){
   	//3 offers returning null immediately and 3 hanging consumers"
   	Debug.println('Q', "TEST 11 poll and offer: 3 offers returning null immediately and 3 hanging consumers");
   	NachosThread t1 = new NachosThread("test11() Thread1:",  new ProducerPollOffer<String>("test11() Produce1 [type = immediate]", synchronousQueue));
   	NachosThread t2 = new NachosThread("test11() Thread2:",  new ProducerPollOffer<String>("test11() Produce2 [type = immediate]", synchronousQueue));
   	NachosThread t3 = new NachosThread("test11() Thread3:",  new ProducerPollOffer<String>("test11() Produce3 [type = immediate]", synchronousQueue));

   	NachosThread t4 = new NachosThread("test11() Thread4:",  new ConsumerTest<String>("test11() Consume1 [type = hanging]",synchronousQueue));
   	NachosThread t5 = new NachosThread("test11() Thread5:",  new ConsumerTest<String>("test11() Consume2 [type = hanging]",synchronousQueue));
   	NachosThread t6 = new NachosThread("test11() Thread6:",  new ConsumerTest<String>("test11() Consume3 [type = hanging]", synchronousQueue));

   	Nachos.scheduler.readyToRun(t1);
   	Nachos.scheduler.readyToRun(t2);
   	Nachos.scheduler.readyToRun(t3);

   	Nachos.scheduler.readyToRun(t4);
   	Nachos.scheduler.readyToRun(t5);
   	Nachos.scheduler.readyToRun(t6);
       }  
    
    public void test12(){
   	// 3 poll returning null immediately, 3 hanging producers,
   	Debug.println('Q', "TEST 12 poll and offer: 3 poll returning immediately, 3 hanging producers");
   	NachosThread t1 = new NachosThread("test12() Thread1:",  new ConsumerPollOffer<String>("test12() Consumer1 [type = immediate]", synchronousQueue));
   	NachosThread t2 = new NachosThread("test12() Thread2:",  new ConsumerPollOffer<String>("test12() Consumer2 [type = immediate]", synchronousQueue));
   	NachosThread t3 = new NachosThread("test12() Thread3:",  new ConsumerPollOffer<String>("test12() Consumer3 [type = immediate]", synchronousQueue));

   	NachosThread t4 = new NachosThread("test12() Thread4:",  new ProducerTest<String>("test12() Produce1 [type = hanging]",synchronousQueue));
   	NachosThread t5 = new NachosThread("test12() Thread5:",  new ProducerTest<String>("test12() Produce2 [type = hanging]",synchronousQueue));
   	NachosThread t6 = new NachosThread("test12() Thread6:",  new ProducerTest<String>("test12() Produce3 [type = hanging]", synchronousQueue));

   	Nachos.scheduler.readyToRun(t1);
   	Nachos.scheduler.readyToRun(t2);
   	Nachos.scheduler.readyToRun(t3);

   	Nachos.scheduler.readyToRun(t4);
   	Nachos.scheduler.readyToRun(t5);
   	Nachos.scheduler.readyToRun(t6);

       }
    
 //FOR THE timeout ONES
    
    public void test13(){
   	// 3 hanging producers and 3 polling threads that take them immediately
   	Debug.println('Q', "TEST 13 poll and offer(timeout): 3 timeing producers, 3 poll taking them immediately");
   	NachosThread t1 = new NachosThread("test13() Thread1:",  new ProducerOfferTimeout<String>("test13() Produce1 [type = timeout]", synchronousQueue, 300));
   	NachosThread t2 = new NachosThread("test13() Thread2:",  new ProducerOfferTimeout<String>("test13() Produce2 [type = timeout]", synchronousQueue, 200));
   	NachosThread t3 = new NachosThread("test13() Thread3:",  new ProducerOfferTimeout<String>("test13() Produce3 [type = timoe]", synchronousQueue, 100));

   	NachosThread t4 = new NachosThread("test13() Thread4:",  new ConsumerTest<String>("test13() Consume1 [type = immediate]",synchronousQueue));
   	NachosThread t5 = new NachosThread("test13() Thread5:",  new ConsumerTest<String>("test13() Consume2 [type = immediate]",synchronousQueue));
   	NachosThread t6 = new NachosThread("test13() Thread6:",  new ConsumerTest<String>("test13() Consume3 [type = immediate]", synchronousQueue));

   	Nachos.scheduler.readyToRun(t1);
   	Nachos.scheduler.readyToRun(t2);
   	Nachos.scheduler.readyToRun(t3);

   	Nachos.scheduler.readyToRun(t4);
   	Nachos.scheduler.readyToRun(t5);
   	Nachos.scheduler.readyToRun(t6);

       }
    
    public void test14(){
   	// 3 hanging consumers, three polling threads 
   	Debug.println('Q', "TEST 10 poll(timeout) and offer: 3 timeing consumers, 3 offers giving their objects immediately");
   	NachosThread t1 = new NachosThread("test14() Thread1:",  new ConsumerPollTimeout<String>("test14() Consume1 [type = timeout]", synchronousQueue, 300));
   	NachosThread t2 = new NachosThread("test14() Thread2:",  new ConsumerPollTimeout<String>("test14() Consume2 [type = timeout]", synchronousQueue, 400));
   	NachosThread t3 = new NachosThread("test14() Thread3:",  new ConsumerPollTimeout<String>("test14() Consume3 [type = timeout]", synchronousQueue, 500));

   	NachosThread t4 = new NachosThread("test14() Thread4:",  new ProducerPollOffer<String>("test14() Produce1 [type = immediate]",synchronousQueue));
   	NachosThread t5 = new NachosThread("test14() Thread5:",  new ProducerPollOffer<String>("test14() Produce2 [type = immediate]",synchronousQueue));
   	NachosThread t6 = new NachosThread("test14() Thread6:",  new ProducerPollOffer<String>("test14() Produce3 [type = immediate]", synchronousQueue));

   	Nachos.scheduler.readyToRun(t1);
   	Nachos.scheduler.readyToRun(t2);
   	Nachos.scheduler.readyToRun(t3);

   	Nachos.scheduler.readyToRun(t4);
   	Nachos.scheduler.readyToRun(t5);
   	Nachos.scheduler.readyToRun(t6);
       }  
    
    public void test15(){
   	//3 offers returning null immediately and 3 hanging consumers"
   	Debug.println('Q', "TEST 15 poll(timeout) and offer()timeout: 3 offers returning null timely and 3 timely consumers");
   	NachosThread t1 = new NachosThread("test15() Thread1:",  new ProducerOfferTimeout<String>("test15() Produce1 [type = timer]", synchronousQueue,100));
   	NachosThread t2 = new NachosThread("test15() Thread2:",  new ProducerOfferTimeout<String>("test15() Produce2 [type = timer]", synchronousQueue,200));
   	NachosThread t3 = new NachosThread("test15() Thread3:",  new ProducerOfferTimeout<String>("test15() Produce3 [type = timer]", synchronousQueue,300));

   	NachosThread t4 = new NachosThread("test15() Thread4:",  new ConsumerPollTimeout<String>("test15() Consume1 [type = timer]",synchronousQueue,100));
   	NachosThread t5 = new NachosThread("test15() Thread5:",  new ConsumerPollTimeout<String>("test15() Consume2 [type = timer]",synchronousQueue,200));
   	NachosThread t6 = new NachosThread("test15() Thread6:",  new ConsumerPollTimeout<String>("test15() Consume3 [type = timer]", synchronousQueue,300));

   	Nachos.scheduler.readyToRun(t1);
   	Nachos.scheduler.readyToRun(t2);
   	Nachos.scheduler.readyToRun(t3);

   	Nachos.scheduler.readyToRun(t4);
   	Nachos.scheduler.readyToRun(t5);
   	Nachos.scheduler.readyToRun(t6);
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
	    Debug.println('Q', message + " : putting " + obj);
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
	    Debug.println('Q', message + " : taking " + obj);
	    Nachos.scheduler.finishThread();
	}

    }
    
    
    class ProducerPollOffer<T> implements Runnable{
	public String message;
	private SynchronousQueue<T> syncQueue;
	
	public ProducerPollOffer(String message, SynchronousQueue<T> queue){
	    this.message = message;
	    this.syncQueue = queue;
	}
	
	@Override
	public void run() {
	    // TODO Auto-generated method stub
	    T obj = (T) new Object();
	    Debug.println('Q', message + " seeing if there's takers for: " + obj + " else return immediately");
	    syncQueue.offer(obj);
	    Nachos.scheduler.finishThread();
	}

    }
    
    class ConsumerPollOffer<T>implements Runnable{
	public String message;
	private SynchronousQueue<T> syncQueue;
	
	public ConsumerPollOffer(String message, SynchronousQueue<T> queue){
	    this.message = message;
	    this.syncQueue = queue;
	}
	
	@Override
	public void run() {
	    // TODO Auto-generated method stub
	    T obj;
	    obj = syncQueue.poll();
	    Debug.println('Q', message + " retrieved actual object or return immediately null, content is : " + obj);
	    Nachos.scheduler.finishThread();
	}

    }
  
    
    //TIMEOUTS
    class ProducerOfferTimeout<T> implements Runnable{
	public String message;
	private SynchronousQueue<T> syncQueue;
	private int timeout = 0;
	
	public ProducerOfferTimeout(String message, SynchronousQueue<T> queue, int timeout){
	    this.message = message;
	    this.syncQueue = queue;
	    this.timeout = timeout;
	}
	
	@Override
	public void run() {
	    // TODO Auto-generated method stub
	    T obj = (T) new Object();
	    Debug.println('Q', message + " seeing if there's takers for: " + obj + " else return immediately");
	    syncQueue.offer(obj, timeout);
	    Nachos.scheduler.finishThread();
	}

    }
    
    class ConsumerPollTimeout<T>implements Runnable{
	public String message;
	private SynchronousQueue<T> syncQueue;
	private int timeout = 0;
	
	public ConsumerPollTimeout(String message, SynchronousQueue<T> queue, int timeout){
	    this.message = message;
	    this.syncQueue = queue;
	    this.timeout = timeout;
	}
	
	@Override
	public void run() {
	    // TODO Auto-generated method stub
	    T obj;
	    obj = syncQueue.poll(timeout);
	    Debug.println('Q', message + " retrieved actual object or return immediately null, content is : " + obj);
	    Nachos.scheduler.finishThread();
	}

    }
}
