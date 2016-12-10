/***************************Testing **************************************************************/
#include "malloc.h"


/****************************Functions For debugging **************************************************************/
void press_to_cont(){
Write("\r\nPress Enter to Continue\r\n",26,1);
	char test[1];
	Read(test, 1, 0);
	while(test[0] != '\n')
	Read(test, 1, 0);
	;
	
	Write("\r\n",2,1);
}

char* itoa(int i, char b[]){
    char const digit[] = "0123456789";
    char* p = b;
    if(i<0){
        *p++ = '-';
        i *= -1;
    }
    int shifter = i;
    do{ //Move to where representation ends
        ++p;
        shifter = shifter/10;
    }while(shifter);
    *p = '\0';
    do{ //Move back, inserting digits as u go
        *--p = digit[i%10];
        i = i/10;
    }while(i);
    return b;
}

/*Simple strlen to get size of a string, null terminated*/
int strlen(char *arg){
	int i = 0;
	while(arg[i++]!='\0');
	return --i;
}

/*Prints a number to screen*/
int print_n(void *num){
	char value[32];//max num for an int
	itoa((int)num,value);
	Write(value,strlen(value),1);
}

/*Prints a number to screen*/
int print_int_num(int num){
	char value[32];//max num for an int
	itoa(num,value);
	Write(value,strlen(value),1);
}

/*counts num of free blocks around the place and returns value*/
int numb_of_free_blocks(){
	sf_free_header* ptr = freelist_head;
	int i;
	for(i =0; ptr!=0;i++){
		ptr = ptr->next;
	}
	return i;
}

/****main****/
int main(){
	char *message_blocks = "\r\nNumber of free blocks: ";
	
	Write("=== Test1: Allocation test ===\n",31,1);
	Write("\r",1,1);
	void *memory = malloc(128);
	print_n(memory);
	((char *)memory)[0] = 't';
	((char *)memory)[1] = 'e';
	((char *)memory)[2] = 's';
	((char *)memory)[3] = 't';
	
	Write(memory,4,1);
	//sf_free(memory);
	//display message of blocks
	Write(message_blocks,strlen(message_blocks),1);
	print_int_num(numb_of_free_blocks());
	
	Write("\r",1,1);
    Write("\r\ntest1 done.\r\n",13,1);
    press_to_cont();
    
    
    Write("=== Test2: Allocation test ===\n",31,1);
	Write("\r",1,1);
	void *memory2 = malloc(50);
	print_n(memory2);
	((char *)memory2)[0] = 't';
	((char *)memory2)[1] = 'e';
	((char *)memory2)[2] = 's';
	((char *)memory2)[3] = 't';
	((char *)memory2)[4] = '2';
	
	Write(memory2,5,1);
    Write("\r\ntest2 done.\r\n",13,1);
    press_to_cont();
   
   	free(memory);
   	Write(message_blocks,strlen(message_blocks),1);
	print_int_num(numb_of_free_blocks());
	
	
}
