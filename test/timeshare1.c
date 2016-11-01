#include "syscall.h"

int main()
{
  int i, j;
  
  Exec("test/timeshare2");
  Exec("test/timeshare3");
  Exec("test/timeshare4");
  
  for(i=0;i<10;i++) {
	for(j=0; j < 100; j++);
    Write("Timesharing 1\n",14,ConsoleOutput);
  }
  
  Exit(0);
}
