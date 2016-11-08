package nachos.kernel.userprog.test;

import nachos.Debug;
import nachos.kernel.Nachos;
import nachos.machine.NachosThread;


import java.util.Random;

public class ExecTest implements Runnable{
    private int sleepingThreadQuantum = 1000;
    
    public static void start(){
	NachosThread thread = new NachosThread("Exec test",  new ExecTest());
	Nachos.scheduler.readyToRun(thread);
    }
    
    @Override
    public void run() {
	// TODO Auto-generated method stub

	Random randomNum = new Random();
	int coinFlip = 1;
	int  n = (int)(Math.random()*100 + 1);
	int i = 100;
	while(coinFlip !=0){
	    Debug.println('A', ("Executing program with burst " + n*10));
	    Debug.println('A', ("Got value " + coinFlip + " for probability."));
	    new ProgTest("test/predict1",1, n*10);
	    n = (int)(Math.random()*i + 1);
	    coinFlip = randomNum.nextInt(2);
	    i+=100;
	    Debug.println('A', ("Sleeping for quantum 1000"));
	    Nachos.scheduler.sleepThread(sleepingThreadQuantum);
	}

	Debug.println('A', "Ending ExecTest, ran " + ((i/100)-1) + " programs.");
	Nachos.scheduler.finishThread();
    }
    

    
}
