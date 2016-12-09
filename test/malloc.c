#include "syscall.h"

extern void *heap_start, *heap_limit;

int
main(){
heap_start+=1300;
int test = 0;
int test1 = 0;
int test2 = 0;
int test3 = 0;
int test4 = 0;
int test5 = 0;
int test6 = 0;
int test7 = 0;
int test8 = 0;
int test9 = 0;
int test10 = 0;

 	*((char *)heap_start) = 'a';
 	*((char *)heap_limit) = 'b';
 	*((char *)heap_limit+1200) = 'c';
 	*((char *)heap_start+1200) = 'a';

	Write("heap_start",1,1);
	Write("heap_limit",1,1);
	
	
}