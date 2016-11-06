/* Basic test of Exec() system call */
#include "syscall.h"



int main()
{
  int i=0;
  while(i<=100){
  	i++;
  	Yield();
  	Write("looped yield1\r\n",15,1);
  }
  Exit(0);
}
