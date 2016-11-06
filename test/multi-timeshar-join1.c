#include "syscall.h"

int main()
{
  SpaceId SID;
  int i;
  char result;
  
  SID = Exec("test/multi-timeshar-join2");
  Join(SID);
  SID = Exec("test/multi-timeshar-join2");
  //for (i=0;i<10000;i++) ;
  result = Join(SID);
  Write(&result,1,ConsoleOutput);
}
  
