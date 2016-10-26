/* Basic test of Exec() system call */

#include "syscall.h"
int fork();
int
main()
{
int i = 0;
while(i <10){
  int test = Exec("test/halt2");
  Yield();
  Join(test);
  i++;
  }

  Write("Exect1\n", 9, 1);
  //Yield();
  Exit(0);
  //Halt();
}

int fork(){
	Write("test\n", 10, 1);
	Exit(0);
}