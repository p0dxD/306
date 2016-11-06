/* Basic test of Exec() system call */
#include "syscall.h"
void printing();
void p();

int main()
{
  Fork(&printing);
  Write("Return Fork\r\n",13,1);
  Exit(0);
}

void printing(){
  Write("printing\r\n",10,1);
  Fork(&p);
  Exit(0);
}

void p(){
	Write("p\r\n",3,1);
	Exit(1);
}