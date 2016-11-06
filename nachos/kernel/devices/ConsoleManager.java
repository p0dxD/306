package nachos.kernel.devices;

import java.util.HashMap;

import nachos.Debug;
import nachos.kernel.Nachos;
import nachos.machine.Console;

public class ConsoleManager {
    static boolean isTaken[] = new boolean[Nachos.options.NUM_CONSOLES];
    static HashMap<ConsoleDriver, Integer> consoleMap = new HashMap<>();
    
    public static final int DEFAULT = 0;
    
    /**
     * 
     * @return
     */
    public ConsoleDriver getConsole(){
	for(int i = 1; i < isTaken.length; i++){
	    if(!isTaken[i]){
		ConsoleDriver driver = new ConsoleDriver(Console.guiConsole());
		consoleMap.put(driver, i);
		isTaken[i] = true;
		return driver;
		}
	}
	return null;
	
    }
    /**
     * 
     * @param consoleToFree
     */
    public void freeConsole(ConsoleDriver consoleToFree){

	if(consoleMap.containsKey(consoleToFree) && (consoleToFree != null)){
	    int index = consoleMap.remove(consoleToFree);
	    isTaken[index] = false;
	}else{
	    Debug.println('C', "Console was never here.");
	}
	
    }
}
