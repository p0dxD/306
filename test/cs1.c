#define MEMSIZE 24
#define PAGESIZE 128
#define ARRAYSIZE (((MEMSIZE+1)*PAGESIZE)/4)


main()
{
  int i = 0;
  int sum;
int A[ARRAYSIZE] = {0,1,2,3,4,5,6,7,8,9};
  sum = 0;

  for(i=0;i<ARRAYSIZE;i++)
    sum += A[i];

  for(i=0;i<ARRAYSIZE;i++)
    sum += A[i];

  if(sum==90) {
    Write("OK\r\n", 4, 1);
  } else {
    Write("Broken\n", 7, 1);
  }
  Exit(0);
}
