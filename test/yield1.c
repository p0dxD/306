/* Basic test of Exec() system call */
#include "syscall.h"



int main()
{
  int i=0;
  while(i<=10){
  	i++;
  	Yield();
  	Write("looped yield1\n",14,1);
  }
  Exit(0);
}
