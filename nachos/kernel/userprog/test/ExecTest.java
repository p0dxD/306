package nachos.kernel.userprog.test;

import nachos.Debug;
import nachos.kernel.Nachos;
import nachos.machine.NachosThread;

import java.util.Random;

public class ExecTest implements Runnable {
    private int sleepingThreadQuantum = 1000;
    static int processesCount = 500;
    
    public static void start() {
	NachosThread thread = new NachosThread("Exec test", new ExecTest());
	Nachos.scheduler.readyToRun(thread);
    }

    @Override
    public void run() {
	// TODO Auto-generated method stub
	test3();
	Nachos.scheduler.finishThread();
    }

    
    public void test2(){
	Random randomNum = new Random();
	int coinFlip = 1;
	int n = (int) (Math.random() * 100 + 1);
	int i = 100, j = 0;
	while (j++ < 5) {
	    Debug.println('A', ("Executing program with burst " + n * 10));
	    Debug.println('A', ("Got value " + coinFlip + " for probability."));
	    new ProgTest("test/predict1", 1, n * 10);
	    n = (int) (Math.random() * i + 1);
	    coinFlip = randomNum.nextInt(2);
	    i += 100;
	    Debug.println('A', ("Sleeping for quantum 1000"));
	    // Nachos.scheduler.sleepThread(sleepingThreadQuantum);
	}

	Debug.println('A',
		"Ending ExecTest, ran " + ((i / 100) - 1) + " programs.");
    }
    
     public void test1() {
	int sleepIteration = 1000; // S=1000 initially
	while (sleepIteration < 20000) {
	    Random randIterationGenerator = new Random();
	    double chanceThreshold = 0.10;
	    double probability = Math.random();
	    int max = 100, min = 1, totalIteration = 0;
	    while (true) {
		if (probability > chanceThreshold) {
		    int burst = randIterationGenerator.nextInt((max - min) + 1)
			    + min;
		    new ProgTest("test/predict1", 1, burst * 10);
		    totalIteration += burst;
		    // if it reaches about 10000 ticks
		    System.out.println(burst);
		}
		if (totalIteration > 10000) {
		    break;
		}
		probability = Math.random();
		min = max;
		max = max + 100;
		chanceThreshold = 0.90 * chanceThreshold;
		Nachos.scheduler.sleepThread(sleepingThreadQuantum);
		System.out.println(
			"CONTINUE Prob:" + probability + ", Thres" + chanceThreshold);

	    }
	    sleepIteration++;
	}
    }
    
    

    public void test3() {
    	sleepThreadDriver(processesCount);
    }

    /*
     * Sleep Driver Thread that sleeps thread for random ticks. 
     * upon wake, it creates a process that runs arbitrarily amount of tick time. 
     * It will continue sleeping and creating processes until reaching the processesCount. 
     */
    public void sleepThreadDriver(int processesCount){
    	int finishedProcesses = 0;
    	while(finishedProcesses < processesCount){
    		randSleepExecProcess(0.1,1000,2000);
    		System.out.println("SleepThreadDriver():Finished process no. "+ (finishedProcesses+1));
    		finishedProcesses++;
    	}
    }

    public void randSleepExecProcess(double alpha,int sleepQuantum, int sleepStep){
    	double sleepProbability=Math.random();//generate event
    	double threshold=alpha; //trigger sleep threshold. 
    	if(sleepProbability<=threshold){
    		Nachos.scheduler.sleepThread(sleepQuantum);
    		launchProcessExponentialProb(0.10); 
    		return;
    	}
    	//check exponential thresholds. 
    	double exponentThreshold = alpha;
        while (true) {
    		sleepQuantum += 2000;
    		exponentThreshold = 0.90 * exponentThreshold;
    		threshold +=exponentThreshold;
    		if (sleepProbability <= threshold) {
    			//sleep and update counter. 
    			Nachos.scheduler.sleepThread(sleepQuantum);
    			launchProcessExponentialProb(0.10); 
    			return;
    		}
        }
    }

    /*
     * Launches a program process with exponential probability average
     */
    public void launchProcessExponentialProb(double alpha){
    	double probability = Math.random(); //an event occurred. 
    	double threshold = alpha; //init threshold 
    	int max = 100, min = 1; //random generated iteration range. 
    	if (probability <= threshold) {
    		launchProcessIterate(min,max);
    		return;
    	}
    	//check each exponential probability region, where the event probability occured. 
    	double exponentialChance = alpha;
    	while (true) {
    		min = max;
    		max = max + 100;
    		exponentialChance = 0.9*exponentialChance;
    		threshold += exponentialChance;
    		//System.out.println("Prob target:" + probability + ", threshold: " + threshold + ", exponentialChance: " + exponentialChance);
    		if (probability <= threshold) {
    		    launchProcessIterate(min,max);
    		    break;
    		}
        }
    }


    /*
     *  Starts a process with random burst between min and max. Returns the random burst. 
     */
    public int launchProcessIterate(int min, int max){
    	int burst = getRandomInt(min,max);
    	new ProgTest("test/predict1", 1, burst * 10);
    	processesCount++;
    	//System.out.println("launchProcessIterate(): burst value = " + burst);
    	return burst;
    }

    /*
     * Retrieves number between range(min,max)
     */
    public int getRandomInt(int min, int max){
    	Random rand = new Random();
    	return rand.nextInt((max - min) + 1) + min;
    }
    
    

}
