/* NoffHeader.java
 *     Classes defining the Nachos Object Code Format
 *
 *     Basically, we only know about three types of segments:
 *	code (read-only), initialized data, and unitialized data
 *
 * Copyright (c) 1992-1993 The Regents of the University of California.
 * Copyright (c) 1998 Rice University.
 * Copyright (c) 2003 State University of New York at Stony Brook.
 * All rights reserved.  See the COPYRIGHT file for copyright notice and
 * limitation of liability and disclaimer of warranty provisions.
 */

package nachos.noff;

import nachos.Debug;
import nachos.kernel.filesys.OpenFile;

/**
 * A NoffHeader object is the kernel representation of the header
 * portion of a NOFF executable file.  It contains information about the
 * three segments (code, initialized data, uninitialized) data in the
 * file, which is needed for initializing a user address space.
 * 
 * @author Thomas Anderson (UC Berkeley), original C version
 * @author Peter Druschel (Rice University), Java translation
 * @author Eugene W. Stark, Java version
 */
public class NoffHeader {
  /**
   * Magic number found at the beginning of a NOFF file.
   * It permits the kernel to immediately reject files that are definitely
   * not executable.
   */
  static final int noffMagic = 0xbadfad;

  /** Information about the executable code segment. */
  public NoffSegment code;

  /** Information about the initialized data segment. */
  public NoffSegment initData;

  /**
   * Information about the "uninitialized" data segment, which
   * must be zeroed before use.
   */
  public NoffSegment uninitData;
  
  /**
   * Private constructor, to force use of readHeader() to initialize
   * a NoffHeader.
   */
  private NoffHeader() { }

  /**  
   * Take bytes read from the file and interpret them as unsigned
   * rather than signed values.
   *
   * @param raw  A raw byte read from the file.
   * @return  The value of the byte when it is interpreted as an unsigned
   * integer quantity.
   */
  private static int convertByte(byte raw) {
    if (raw < 0) return 256 + raw;
    else return raw;
  }

  /**  
   * Take four bytes read from the file and assemble them into an
   * unsigned integer.
   *
   * @param raw  The four bytes read from the file.
   * @return  The value of the four bytes when interpreted as an unsigned
   * integer.
   */
  private static int convertWord(byte raw[]) {
    return convertByte(raw[0]) |
      (convertByte(raw[1]) << 8) | 
      (convertByte(raw[2]) << 16) |
      (convertByte(raw[3]) << 24);
  }

  /**
   * Read a word from a file and check that it matches noffMagic.
   *
   * @param f The file to read.
   * @return true if the word read matches noffMagic, false if
   * it does not or a word could not be read from the file.
   */
  private static boolean checkMagic(OpenFile f) {
      byte[] raw = new byte[4];
      if(f.read(raw, 0, 4) != 4)
	  return(false);
      if(convertWord(raw) != noffMagic)
	  return(false);
      return(true);
  }

  /**
   * Initialize a NoffHeader object by reading its values from a file.
   */
  public static NoffHeader readHeader(OpenFile f) {
    NoffHeader hdr = new NoffHeader();
    if (!checkMagic(f)) {
      Debug.println('a', "NoffHeader: Bad magic value");
      return(null);
    }
    hdr.code = NoffSegment.readSegment(f);
    hdr.initData = NoffSegment.readSegment(f);
    hdr.uninitData = NoffSegment.readSegment(f);
    if(hdr.code == null || hdr.initData == null || hdr.uninitData == null)
	return(null);
    return(hdr);
  }

  /**
   * A NoffSegment object contains information about one segment of a
   * NOFF executable file.
   */
  public static class NoffSegment {
      /** The location of segment in user virtual address space. */
      public int virtualAddr;

      /** The location of the segment in the NOFF file. */
      public int inFileAddr;

      /** The size of the segment in bytes. */
      public int size;

      /**
       * Private constructor, to force use of readSegment() to initialize
       * a NoffSegment.
       */
      private NoffSegment() { }

      /**
       * Initialize a NoffSegment with data from a file.
       *
       * @param f The file from which to read the data.
       */
      public static NoffSegment readSegment(OpenFile f) {
	  NoffSegment seg = new NoffSegment();
	  byte[] raw = new byte[4];

	  if(f.read(raw, 0, 4) != 4)
	      return(null);
	  seg.virtualAddr = convertWord(raw);

	  if(f.read(raw, 0, 4) != 4)
	      return(null);
	  seg.inFileAddr = convertWord(raw);

	  if(f.read(raw, 0, 4) != 4)
	      return(null);
	  seg.size = convertWord(raw);

	  return(seg);
      }
  }
}
