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

void display_message(char *message){
	Write(message,strlen(message),1);
}

void display_message_of_free_block_available(){
	char *message_blocks = "\r\nNumber of free blocks: ";
	display_message(message_blocks);
	print_int_num(numb_of_free_blocks());
	Write("\r",1,1);
}

void new_line(){
	Write("\n",1,1);
	Write("\r",1,1);
}

void print_memory_start_loc(){
	display_message("Printing memory start location:");
}
/****main****/
int main(){

	display_message("=== Test1: Allocation test ===");
	new_line();
	display_message("About to malloc for 128");
	new_line();
	void *memory = malloc(128);
	print_memory_start_loc();
	new_line();
	print_n(memory);
	new_line();
	((char *)memory)[0] = 't';
	((char *)memory)[1] = 'e';
	((char *)memory)[2] = 's';
	((char *)memory)[3] = 't';
	((char *)memory)[4] = '1';
	((char *)memory)[5] = '\0';
	display_message("Displaying the contents stored");
	new_line();
	display_message(memory);
	new_line();
	//display message of blocks
	display_message_of_free_block_available();
    display_message("\r\ntest1 done.\r\n");
    press_to_cont();
    
    display_message("=== Test2: Allocation test ===");
	new_line();
	display_message("About to malloc for 500");
	new_line();
	void *memory2 = malloc(500);
	print_memory_start_loc();
	new_line();
	print_n(memory2);
	new_line();
	((char *)memory2)[0] = 't';
	((char *)memory2)[1] = 'e';
	((char *)memory2)[2] = 's';
	((char *)memory2)[3] = 't';
	((char *)memory2)[4] = '2';
	((char *)memory2)[5] = '\0';
	display_message("Displaying the contents stored");
	new_line();
	display_message(memory);
	new_line();
	//display message of blocks
	display_message_of_free_block_available();
    display_message("\r\ntest2 done.\r\n");
    press_to_cont();
    
    display_message("=== Test4: Free test ===");
	new_line();
   	display_message("Freeing first block.");
   	new_line();
   	free(memory);
	display_message_of_free_block_available();
	new_line();
	press_to_cont();
	display_message("Freeing second block.");
	new_line();
   	free(memory2);
   	display_message_of_free_block_available();
   	new_line();
   	
   	
   	display_message("=== Test5: Allocation test ===");
	new_line();
	display_message("About to malloc for 2000");
	new_line();
	memory = malloc(2000);
	print_memory_start_loc();
	new_line();
	print_n(memory);
	new_line();
	((char *)memory)[0] = 't';
	((char *)memory)[1] = 'e';
	((char *)memory)[2] = 's';
	((char *)memory)[3] = 't';
	((char *)memory)[4] = '3';
	((char *)memory)[5] = '\0';
	display_message("Displaying the contents stored");
	new_line();
	display_message(memory);
	new_line();
	//display message of blocks
	display_message_of_free_block_available();
    display_message("\r\ntest3 done.\r\n");
    press_to_cont();
    
    display_message("=== Test4: Allocation and free test ===");
	new_line();
   	display_message("About to malloc for 200");
	new_line();
	memory2 = malloc(200);
	print_memory_start_loc();
	new_line();
	print_n(memory2);
	new_line();
   	
   	display_message("About to malloc for 400");
	new_line();
	void *memory3 = malloc(400);
	print_memory_start_loc();
	new_line();
	print_n(memory3);
	new_line();
   	
   	display_message("About to malloc for 150");
	new_line();
	void *memory4 = malloc(150);
	print_memory_start_loc();
	new_line();
	print_n(memory4);
	new_line();
   	display_message_of_free_block_available();
   	new_line();
   	
   	display_message("Freeing second block (400 one).");
   	free(memory3);
   	new_line();
   	display_message_of_free_block_available();
	new_line();
	
	display_message("Freeing first block (200 one).");
   	free(memory2);
   	new_line();
   	display_message_of_free_block_available();
	new_line();
	
	display_message("Freeing third block (150 one).");
   	free(memory4);
   	new_line();
   	display_message_of_free_block_available();
	new_line();
	
	
	display_message("Process will terminate on next enter.\r\n");
	press_to_cont();
	Halt();
}
