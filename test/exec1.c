/* Basic test of Exec() system call */

#include "syscall.h"
int fork();
int
main(int argc, char** argv)
{
int i = 0;
while(i <10){
  int test = Exec("test/halt2");
  Yield();
  Fork(&fork);
  Join(test);
  i++;
  }

  Write("Exect1\r\n", 8, 1);
  Yield();
  Exit(0);
  //Halt();
}

int fork(){
	Write("test\r\n", 6, 1);
	Exec("test/fork2");
	Exit(0);
}