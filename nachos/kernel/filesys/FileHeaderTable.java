package nachos.kernel.filesys;

import java.util.ArrayList;

public class FileHeaderTable {
    
    
    private ArrayList<FileHeader> openHeaders;  //list of open fileheaders
    
    /** empty constructor */
    public FileHeaderTable() {
	
    }
    
    /** opens a particular fileheader for read or write
     * @param f  (fileheader to access)
     * @return true if the fileheader doesn't exist and can be opened.
     * return false if the fileheader is already open (in use)
     */
    public boolean openFileHeader(FileHeader f) {
	
	if (openHeaders.contains(f))
	    return false;
	else {
	    openHeaders.add(f);
	    return true;
	}
    }
    
    public boolean closeFileHeader(FileHeader f) {
	if (openHeaders.contains(f)) {
	    openHeaders.remove(f);
	    return true;
	}
	else
	    return false;
    }

}
