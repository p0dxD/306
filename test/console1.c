/* Basic test of console reads and writes */

#include "syscall.h"
#define INPUTSIZE 20

int main()
{
  char buffer[INPUTSIZE];
  int num;

  Write("Give me some input: \r\n", 22, ConsoleOutput);
  num = Read(buffer, INPUTSIZE, ConsoleInput);
  Write("You said: \r\n", 12, ConsoleOutput);
  Write(buffer, num , ConsoleOutput);
}

