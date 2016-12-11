#include "syscall.h"

extern void *heap_start, *heap_limit;
int strlen(char *arg);
void press_to_cont();
void display_message(char *message);

int
main(){
	display_message("Triggering bad address by adding 'a'.\r\n");
 	*((char *)heap_start) = 'a';
 	display_message("Added a now printing its values.\r\n");
	Write(heap_start,1,1);
	press_to_cont();
	display_message("Adding more address to heap, 786 more.\r\n");
	heap_limit+=786;
	display_message("Adding content.\r\n");
	((char *)heap_limit)[0] = 'a';
	((char *)heap_limit)[1] = 'b';
	((char *)heap_limit)[2] = 'c';
	((char *)heap_limit)[3] = 'd';
	((char *)heap_limit)[4] = '\r';
	((char *)heap_limit)[5] = '\n';
	((char *)heap_limit)[6] = '\0';
	display_message("displaying content content: \r\n");
	display_message(heap_limit);
	press_to_cont();
	
	display_message("Adding more address to heap, 1000 more.\r\n");
	heap_limit+=1000;
	display_message("Adding content.\r\n");
	((char *)heap_limit)[0] = 'c';
	((char *)heap_limit)[1] = 'd';
	((char *)heap_limit)[2] = 'e';
	((char *)heap_limit)[3] = 'f';
	((char *)heap_limit)[4] = '\r';
	((char *)heap_limit)[5] = '\n';
	((char *)heap_limit)[6] = '\0';
	
	display_message("displaying content content: \r\n");
	display_message(heap_limit);
	press_to_cont();
	
	
	display_message("Adding more address to heap, 2000 more.\r\n");
	heap_limit+=2000;
	display_message("Adding content.\r\n");
	((char *)heap_limit)[0] = 'g';
	((char *)heap_limit)[1] = 'h';
	((char *)heap_limit)[2] = 'i';
	((char *)heap_limit)[3] = 'j';
	((char *)heap_limit)[4] = '\r';
	((char *)heap_limit)[5] = '\n';
	((char *)heap_limit)[6] = '\0';
	
	display_message("displaying content content: \r\n");
	display_message(heap_limit);
	display_message("Process will terminate on next enter.\r\n");
	press_to_cont();
	Halt();
}

void display_message(char *message){
	Write(message,strlen(message),1);
}

/*Simple strlen to get size of a string, null terminated*/
int strlen(char *arg){
	int i = 0;
	while(arg[i++]!='\0');
	return --i;
}

void press_to_cont(){
Write("\r\nPress Enter to Continue\r\n",26,1);
	char test[1];
	Read(test, 1, 0);
	while(test[0] != '\n')
	Read(test, 1, 0);
	;
	
	Write("\r\n",2,1);
}