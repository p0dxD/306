package nachos.kernel.userprog.test;

import nachos.kernel.Nachos;
import nachos.machine.NachosThread;

public class ExecTest implements Runnable{

    
    public static void start(){
	NachosThread thread = new NachosThread("Exec test",  new ExecTest());
	Nachos.scheduler.readyToRun(thread);
    }
    @Override
    public void run() {
	// TODO Auto-generated method stub
	new ProgTest("test/exec1",1);
	new ProgTest("test/exec1",2);
	new ProgTest("test/exec1",3);
	new ProgTest("test/exec1",4);
	Nachos.scheduler.finishThread();
    }
    
}
