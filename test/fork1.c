/* Basic test of Exec() system call */
#include "syscall.h"
void printing();

int main()
{
  Fork(&printing);
  Write("Return Fork",11,1);
  Exit(0);
}

void printing(){
  Write("Forked",6,1);
  Exit(0);
}