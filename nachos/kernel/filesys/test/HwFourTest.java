package nachos.kernel.filesys.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import nachos.Debug;
import nachos.kernel.Nachos;
import nachos.kernel.filesys.OpenFile;
import nachos.kernel.threads.Callout;
import nachos.machine.NachosThread;

public class HwFourTest implements Runnable{
    private static final int TransferSize = 10;
    
    public static void start(){
	System.out.println("Starting hw4");
	NachosThread thread = new NachosThread("Callout test one",  new HwFourTest());
	Nachos.scheduler.readyToRun(thread);
	
    }
    
	@Override
	public void run() {
	    // TODO Auto-generated method stub
	    CopyTest t1 = new CopyTest("test/shell","test1");
	    Callout call = new Callout();
	    call.schedule(t1, 100);
//	    CopyTest t2 = new CopyTest("test/shell","test2");
//	    call.schedule(t2, 200);
	    Nachos.scheduler.finishThread();
	}
	
	    private void copy(String from, String to) {
		File fp;
		FileInputStream fs;
		OpenFile openFile;
		int amountRead;
		long fileLength;
		byte buffer[];

		// Open UNIX file
		fp = new File(from);
		if (!fp.exists()) {
		    Debug.printf('+', "Copy: couldn't open input file %s\n", from);
		    return;
		}

		// Figure out length of UNIX file
		fileLength = fp.length();

		// Create a Nachos file of the same length
		Debug.printf('f', "Copying file %s, size %d, to file %s\n", from,
			new Long(fileLength), to);
		if (!Nachos.fileSystem.create(to, (int)fileLength)) {	 
		    // Create Nachos file
		    Debug.printf('+', "Copy: couldn't create output file %s\n", to);
		    return;
		}

		openFile = Nachos.fileSystem.open(to);
		Debug.ASSERT(openFile != null);

		// Copy the data in TransferSize chunks
		buffer = new byte[TransferSize];
		try {
		    fs = new FileInputStream(fp);
		    while ((amountRead = fs.read(buffer)) > 0)
			openFile.write(buffer, 0, amountRead);	
		} catch (IOException e) {
		    Debug.print('+', "Copy: data copy failed\n");      
		    return;
		}
		// Close the UNIX and the Nachos files
		//delete openFile;
		try {fs.close();} catch (IOException e) {}
	    }
	    
	    class CopyTest implements Runnable{
		private String from;
		private String to;
		
		public CopyTest(String from ,String to){
		   this.from = from;
		   this.to= to;
		}
		
		@Override
		public void run() {
		    Debug.println('F', "Copying "+ from +" to "+ to);
		    copy(from,to);
		    
		}
		
	    }
	   
	
}
