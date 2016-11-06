#include "syscall.h"


/* this program checks the asychronous stub filesystem */

/* use about 4 pages of physical memory */

main()
{
	int c1, c2;
	
	Write("Starting\r\n", 10, 1);
// 	c1 = Exec("test/cs2");
	c2 = Exec("test/cs3");
	Join(c1);
	Join(c2);
	Write("Done\r\n", 6, 1);
	Exit(0);
}
