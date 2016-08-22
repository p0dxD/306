package nachos.noff;

import java.io.*;

/**
 * Class for reading COFF files.
 * 
 * @author Thomas Anderson (UC Berkeley), original C version
 * @author Eugene W. Stark, Java version
 */

class CoffInput {

    /** Number of sections. */
    private short numSections;

    /** Data we care about in a COFF section. */
    public static class Section {
	String name;		// Name of section
	int pAddr;		// Physical address
	int size;		// Size
	int scnptr;		// File pointer to raw data for section
	byte[] data;		// Section data
    };

    /** Table of all sections. */
    private Section[] sections;

    /* Magic numbers that can appear in COFF file. */
    private static final int MIPSELMAGIC = 0x0162;
    private static final int OMAGIC = 0407;
    //private static final int SOMAGIC = 0x0701;

    /** Underlying Java file. */
    private RandomAccessFile inFile;

    /**
     * Initialize a new CoffInput object from a file.
     */
    public CoffInput(String name) throws FileNotFoundException {
	inFile = new RandomAccessFile(name, "r");
    }

    /**
     * Retrieve the table of COFF sections.
     */
    public Section[] getSections() {
	return(sections);
    }

    /**
     * Retrieve a COFF section by name, assuming the COFF file has
     * already been read in and interpreted.
     */
    public Section getSection(String name) {
	if(sections == null)
	    return(null);
	for(int i = 0; i < numSections; i++) {
	    if(sections[i] != null && name.equals(sections[i].name))
		return(sections[i]);
	}
	return(null);
    }

    /**
     * Read a COFF file into memory, initializing the sections table.
     */
    public void read() throws Exception {
	readFileHdr();
	readSystemHdr();
	for(int i = 0; i < numSections; i++)
	    readSectionHdr(i);
	System.out.println("Loading " + numSections + " sections:");
	for(int i = 0; i < numSections; i++)
	    readSectionData(i);
    }

    /**
     * Read the COFF file header, checking the magic number and noting
     * the number of sections.
     */
    private void readFileHdr() throws Exception {
	short magic = readShort();	// magic number
	if(magic != MIPSELMAGIC)
	    throw new Exception("File is not a MIPSEL COFF file");
	numSections = readShort();	// number of sections
	readInt(); 			// time & date stamp
	readInt();			// file pointer to symbolic header
	readInt();			// sizeof(symbolic hdr)
	readShort();			// sizeof(optional hdr)
	readShort();			// flags

	sections = new Section[numSections];
    }

    /**
     * Read the COFF system header, checking the magic number.
     */
    private void readSystemHdr() throws Exception {
	short magic = readShort();	// magic number
	// Check magic
	if(magic != OMAGIC)
	    throw new Exception("File is not an OMAGIC file");
	readShort();		// version stamp
	readInt();		// text size in bytes, padded to DW bdry
	readInt();		// initialized data "  "
	readInt();		// uninitialized data "  "
	readInt();		// entry pt.
	readInt();		// base of text used for this file
	readInt();		// base of data used for this file
	readInt();		// base of bss used for this file
	readInt();		// general purpose register mask
	readInt();		// co-processor register masks [4]
	readInt();
	readInt();
	readInt();
	readInt();		// the gp value used for this object
    }

    /**
     * Read in a COFF section header.
     */
    private void readSectionHdr(int i) throws IOException {
	Section s = new Section();
	sections[i] = s;
	s.name = (new String(readBytes(8))).trim();	// section name
	s.pAddr = readInt();		// physical address, aliased s_nlib
	readInt();			// virtual address
	s.size = readInt();		// section size
	s.scnptr = readInt();		// file ptr to raw data for section
	readInt();			// file ptr to relocation
	readInt();			// file ptr to gp histogram
	readShort();			// number of relocation entries
	readShort();			// number of gp histogram entries
	readInt();			// flags
    }

    /**
     * Read in the data for a COFF section, assuming that the header
     * info has already been read in.
     */
    private void readSectionData(int i) throws IOException {
	Section s = sections[i];
	System.out.println
	    ("\t\"" + s.name +
	     "\", filepos 0x" + Integer.toHexString(s.scnptr)
	     + ", mempos 0x" + Integer.toHexString(s.pAddr)
	     + ", size 0x" + Integer.toHexString(s.size));
	inFile.seek(s.scnptr);
	s.data = readBytes(s.size);
    }

    private int readInt() throws IOException {
	byte[] buf = new byte[4];
	inFile.read(buf);
	return(bytesToInt(buf, 0));
    }

    private short readShort() throws IOException {
	byte[] buf = new byte[2];
	inFile.read(buf);
	return(bytesToShort(buf, 0));
    }

    private byte[] readBytes(int n) throws IOException {
	byte[] buf = new byte[n];
	inFile.read(buf);
	return(buf);
    }

    /**
     * Utility method to deserialize a sequence of bytes into an integer.
     *
     * @param buffer The buffer from which the value is to be deserialized.
     * @param pos Starting offset from the beginning of the buffer at which
     * the serialized bytes representing the value exist.
     * @return The integer value represented by the bytes in the buffer.
     */
    private static int bytesToInt(byte[] buffer, int pos) {
	return (buffer[pos+3] << 24) | 
	    ((buffer[pos+2] << 16) & 0xff0000) |
	    ((buffer[pos+1] << 8) & 0xff00) | 
	    (buffer[pos+0] & 0xff);
    }

    /**
     * Utility method to deserialize a sequence of bytes into a short integer.
     *
     * @param buffer The buffer from which the value is to be deserialized.
     * @param pos Starting offset from the beginning of the buffer at which
     * the serialized bytes representing the value exist.
     * @return The integer value represented by the bytes in the buffer.
     */
    private static short bytesToShort(byte[] buffer, int pos) {
	return (short)((buffer[pos+1] << 8) | (buffer[pos+0] & 0xff));
    }
}
