/* Basic test of Exec() system call */
#include "syscall.h"
int main()
{
  Exec("yield1");
  Exit(0);
}
