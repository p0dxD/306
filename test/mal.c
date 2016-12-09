/**
 * All functions you make for the assignment must be implemented in this file.
 * Do not submit your assignment with a main function in this file.
 * If you submit with a main function in this file, you will get a zero.
 */
#include "mal.h"
/*
prototypes
*/

static void* find_fit(int size);
static void place(void *bp, int asize, int size);
static int extend_by(int block_size);
static void* extend_heap(int amount);
static void free_block(void* ptr);
static void* coalesce(void* ptr);
static int coalesce_case(void* ptr);
static int print_num(void *num);

/**
 * You should store the head of your free list in this variable.
 * Doing so will make it accessible via the extern statement in sfmm.h
 * which will allow you to pass the address to sf_snapshot in a different file.
 */


sf_free_header* freelist_head = 0;
void* address_new = 0;/*ADDED*/
void* init_address = 0;/*ADDED*/

/*ALIGMENT*/
#define ALIGMENT 16
#define ALIGN(size) (((size) + (ALIGMENT-1)) & ~(ALIGMENT-1))
#define PAGE_SIZE 128
#define DSIZE 16
extern void *heap_start, *heap_limit;


void* sf_malloc(int size) {
  void* bp = 0;
  int asize;      /* Adjusted block size */
  if(size == 0)
    return 0;
/*Aling initial address*/
  init_address = heap_limit;
  int k = ((int)init_address & 0xF);
/*ALIGNMENT*/
  if(k == 0){
      heap_limit += 8;
  }else{
    if(k == 8){
      /*Do nothing*/
    }else if(k > 8){
    heap_limit+=((16-k)+8);
    }else if(k < 8){
    heap_limit+=(8-k);
    }
  }
/*END ALIGNMENT*/
init_address = heap_limit;

  if(freelist_head == 0){
    heap_start = init_address;/*keep check of the start*/
    heap_limit += PAGE_SIZE;
      freelist_head = init_address;
      freelist_head->header.alloc = 0;
      freelist_head->header.requested_size = 0;/*4064 TOTAL payload*/
      freelist_head->header.block_size = PAGE_SIZE>>4;/*block size 4072 due to 24 padding for payload*/
      freelist_head->next = 0;
      freelist_head->prev = 0;
      sf_footer *sb = heap_limit-8;
      sb->block_size = PAGE_SIZE>>4;
      sb->alloc = 0;

  }
  /*align what ever they give me, and add for footer and header*/
  asize = ALIGN(size)+16;

if((bp = find_fit(asize))!= 0){
  place(bp, asize, size);

  return bp+8;/*RETURN A TEST NOT THE REAL DEAL*/

}

int x_tend = extend_by(asize);

if ((bp = extend_heap(x_tend)) == 0){
  return 0;/*No more space*/
}

place(bp, asize, size);
  return bp+8;


}
/****************************************FREE************************************************************/

/*FREE mem*/
void sf_free(void *ptr) {
  if(ptr == 0){
    return;
  }
    ptr = ptr - 8;/*get it pointing at header*/
    free_block(ptr);
    coalesce(ptr);
}


/****************************************HELPERS************************************************************/

static void* coalesce(void* ptr){
  void* before = 0;
  int block_size;
  sf_footer* sb;
  if(ptr != heap_limit){
    before = ptr-8;
    before = before-(((sf_footer *)before)->block_size<<4)+8;
  }else{
    before = ptr;
  }
  void* after = ptr + (((sf_free_header*)ptr)->header.block_size<<4);


  switch(coalesce_case(ptr)){

    case 2:/*The one aftr is free*/

    if(((sf_free_header*)before)->prev!= 0 && ((sf_free_header*)before)->prev->next != 0)
      ((sf_free_header*)before)->prev->next = ((sf_free_header*)before)->next;/*Make my next its next*/
    if(((sf_free_header*)before)->next!= 0 && ((sf_free_header*)before)->next->prev != 0)
      ((sf_free_header*)before)->next->prev = ((sf_free_header*)before)->prev;/*Make my prev its prev*/

      /*Extend block*/
      block_size = (((sf_free_header* )ptr)->header.block_size)+(((sf_free_header* )before)->header.block_size);
      ((sf_free_header*)before)->header.alloc = 0;
      ((sf_free_header*)before)->header.requested_size = 0;
      ((sf_free_header*)before)->header.block_size = block_size;

      sb = ptr+ (((sf_free_header* )ptr)->header.block_size<<4)-8;
      sb->alloc = 0;
      sb->block_size = block_size;
      /*Set the head accordingly*/
      ptr = before;/*first for header*/

    break;
    case 3:/*The one aftr and before are free*/

    if(((sf_free_header*)after)->prev != 0 && ((sf_free_header*)after)->prev->next != 0)
      ((sf_free_header*)after)->prev->next = ((sf_free_header*)after)->next;/*Make my next its next*/
    if(((sf_free_header*)after)->next!= 0 && ((sf_free_header*)after)->next->prev != 0)
      ((sf_free_header*)after)->next->prev = ((sf_free_header*)after)->prev;/*Make my prev its prev*/

      /*Extend block*/
      block_size = (((sf_free_header* )ptr)->header.block_size)+(((sf_free_header* )after)->header.block_size);
      ((sf_free_header*)ptr)->header.alloc = 0;
      ((sf_free_header*)ptr)->header.requested_size = 0;
      ((sf_free_header*)ptr)->header.block_size = block_size;

      sb = after+ (((sf_free_header* )after)->header.block_size<<4)-8;
      sb->alloc = 0;
      sb->block_size = block_size;
      /*Set the head accordingly*/

    break;
    case 4:/*Case 4*/

    if(((sf_free_header*)before)->prev!= 0 && ((sf_free_header*)before)->prev->next != 0)
      ((sf_free_header*)before)->prev->next = ((sf_free_header*)before)->next;/*Make my next its next*/
    if(((sf_free_header*)before)->next!= 0 && ((sf_free_header*)before)->next->prev != 0)
      ((sf_free_header*)before)->next->prev = ((sf_free_header*)before)->prev;/*Make my prev its prev*/


    if(((sf_free_header*)after)->prev!= 0 && ((sf_free_header*)after)->prev->next != 0)
      ((sf_free_header*)after)->prev->next = ((sf_free_header*)after)->next;/*Make my next its next*/
    if(((sf_free_header*)after)->next!= 0 && ((sf_free_header*)after)->next->prev != 0)
      ((sf_free_header*)after)->next->prev = ((sf_free_header*)after)->prev;/*Make my prev its prev*/

    /*Extend block*/
    block_size = (((sf_free_header* )ptr)->header.block_size)+(((sf_free_header* )after)->header.block_size)+(((sf_free_header* )before)->header.block_size);
    ((sf_free_header*)before)->header.alloc = 0;
    ((sf_free_header*)before)->header.requested_size = 0;
    ((sf_free_header*)before)->header.block_size = block_size;

    sb = after+ (((sf_free_header* )after)->header.block_size<<4)-8;
    sb->alloc = 0;
    sb->block_size = block_size;
    /*Set the head accordingly*/

    ptr = before;/*first for header*/


    break;
    case 1:
    default:/**/

    break;
  }/*END switch*/
  /*TAKE CARE OF HEAD*/
  if(freelist_head == ((sf_free_header*)before)){
    /*Make the next the head unless its also being removed*/
    if( ((sf_free_header*)before)->next != ((sf_free_header*)after)){
      freelist_head = ((sf_free_header*)before)->next;
    }else{
      /*Check if its not null*/
      if(((sf_free_header*)before)->next->next != 0){
        /*make the next next head*/
        freelist_head = ((sf_free_header*)before)->next->next;

      }else{
        freelist_head = 0; /*No other candidates*/
      }
    }
  }else if(freelist_head == ((sf_free_header*)after)){
    if(((sf_free_header*)after)->next != ((sf_free_header*)before)){
      freelist_head = ((sf_free_header*)after)->next;
    }else{
      /*Check if its not null*/
      if(((sf_free_header*)after)->next->next != 0){
        /*make the next next head*/
        freelist_head = ((sf_free_header*)after)->next->next;

      }else{
        freelist_head = 0; /*No other candidates*/
      }
    }
  }
  /*END HEAD MOVEMENT*/
#ifdef ADDRESS
  if(freelist_head != 0){
    void* bp;
      for(bp = freelist_head; bp != 0; bp = ((sf_free_header*)bp)->next) {
        if((bp > ptr)&&((sf_free_header*)bp)->prev==0){
           ((sf_free_header*)bp)->prev = ptr;
           freelist_head = ptr;
           freelist_head->next = bp;
           freelist_head->prev = 0;
        }else if((bp > ptr)&&((sf_free_header*)bp)->prev!=0&&((sf_free_header*)bp)->next!=0){
            ((sf_free_header*)ptr)->next = bp;
            ((sf_free_header*)ptr)->prev = ((sf_free_header*)bp)->prev;
            ((sf_free_header*)bp)->prev->next = ptr;
            ((sf_free_header*)bp)->prev = ptr;
        }else{
          ((sf_free_header*)bp)->next = ptr;
          ((sf_free_header*)ptr)->next = 0;
          ((sf_free_header*)ptr)->prev = bp;
        }
      }
  }else{
    freelist_head = ptr;
    freelist_head->next = 0;
    freelist_head->prev = 0;
  }

#else/*LIFO or other*/

  if(freelist_head != 0){
    freelist_head->prev = ptr;
    ((sf_free_header*)ptr)->next = freelist_head;
    ((sf_free_header*)ptr)->prev = 0;
    freelist_head = ptr;
}else{
    freelist_head = ptr;
    freelist_head->next = 0;
    freelist_head->prev = 0;
}
#endif

return ptr;
}


/*Sends type of case it needs to be addressed*/
static int coalesce_case(void* ptr){
  void* before = 0;
  if(ptr != heap_start){
    before = ptr-8;
  }else{
    before = ptr;
  }
  void* after = ptr + (((sf_free_header*)ptr)->header.block_size<<4);
/*CHECK for boundaries*/
    if(before == heap_start && after == heap_limit){
      return 5;
    }else if(before == heap_start){/*check if its the start*/
    /*if this is the heap start then we only chekc header*/
    if(((sf_free_header* )after)->header.alloc == 1){
      /*sf_blockprint(((sf_free_header* )after));*///CHANGED
      return 5;/*theres nothing to unite only */
    }else if(((sf_free_header* )after)->header.alloc == 0){
      return 3;/*have to unite with right*/
    }
  }else if(after == heap_limit){/*check if its end*/
    if(((sf_footer *)before)->alloc == 1){
      return 5;/*theres nothing to unite only */
    }else if(((sf_footer *)before)->alloc == 0){
      return 2;/*have to unite with right*/
    }
  }else{

  if(((sf_footer *)before)->alloc == 1 && ((sf_free_header* )after)->header.alloc == 1){/*Left is free*/
    return 1;
  }else if(((sf_footer *)before)->alloc == 0 && ((sf_free_header* )after)->header.alloc == 1){/*Right is free*/
    return 2;
  }else if(((sf_footer *)before)->alloc == 1 && ((sf_free_header* )after)->header.alloc == 0){/*both are free*/
    return 3;
  }else if(((sf_footer *)before)->alloc == 0 && ((sf_free_header* )after)->header.alloc == 0){/*Both are full*/
    return 4;
  }
}/*End else*/
return -1;
}

static void free_block(void* ptr){
  ((sf_free_header*)ptr)->header.alloc = 0;
  ((sf_free_header*)ptr)->header.requested_size = 0;
  sf_footer *sb = ptr + (((sf_free_header*)ptr)->header.block_size<<4)-8;
  sb->alloc = 0;
}

/*Finds the best match else returns null*/
static void* find_fit(int asize){

#ifdef NEXT
void *bp;
  for(bp = freelist_head; bp != 0; bp = ((sf_free_header*)bp)->next) {
    if( asize == (((sf_free_header*)bp)->header.block_size<<4)) {
      return bp;
  }else if(asize+32 < (((sf_free_header*)bp)->header.block_size<<4)){
      return bp;
  }
}
#else
void *bp;
  for(bp = freelist_head; bp != 0; bp = ((sf_free_header*)bp)->next) {
    if( asize == (((sf_free_header*)bp)->header.block_size<<4)) {
      return bp;
  }else if(asize+32 < (((sf_free_header*)bp)->header.block_size<<4)){
      return bp;
  }
}
#endif

  return 0; /* No fit */
}

/*PLACE*/
static void place(void *bp, int asize,int size){

  if(asize == (((sf_free_header*)bp)->header.block_size<<4)){
      ((sf_free_header*)bp)->header.alloc = 1;
      ((sf_free_header*)bp)->header.requested_size = size;
      sf_footer *sb = bp+asize-8;
      sb->block_size = asize>>4;
      sb->alloc = 1;
      /*if it was head then make if theres a next the head*/
      /*else it will just place the allocate*/
      if(((sf_free_header*)bp)->prev == 0 && ((sf_free_header*)bp)->next != 0){
        freelist_head = ((sf_free_header*)bp)->next;/*Make next the head*/
        freelist_head->prev = 0;/*Make the new head pointer previous null*/

      }else if(((sf_free_header*)bp)->prev == 0 && ((sf_free_header*)bp)->next == 0){/*Head case*/
        freelist_head = 0;/*make head null*/

      }else if(((sf_free_header*)bp)->prev != 0 && ((sf_free_header*)bp)->next != 0){/*Middle case*/
        ((sf_free_header*)bp)->prev->next = ((sf_free_header*)bp)->next;
        ((sf_free_header*)bp)->next->prev = ((sf_free_header*)bp)->prev;

      }else if(((sf_free_header*)bp)->prev != 0 && ((sf_free_header*)bp)->next == 0){/*Tail case*/
        ((sf_free_header*)bp)->prev->next = 0;
      }

  }else{


    void* temp = bp;
    /*Update pointer*/
    /*update pointers accordingly*/
    if(((sf_free_header*)bp)->prev == 0 && ((sf_free_header*)bp)->next != 0){
      /*set head pointer*/
      freelist_head = bp + asize;/*update head pointer*/
      /*Fixed pointer of next previous*/
      ((sf_free_header*)bp)->next->prev = bp+asize;
      /*Fix the pointer to next*/
      freelist_head->next = ((sf_free_header*)bp)->next;/*ADDED*/


    }else if(((sf_free_header*)bp)->prev == 0 && ((sf_free_header*)bp)->next == 0){/*Head case*/
      freelist_head = temp + asize;/*update head pointer*/
      freelist_head->next = 0;/*ADDED*/
      freelist_head->prev = 0;/*ADDED*/

    }else if(((sf_free_header*)bp)->prev != 0 && ((sf_free_header*)bp)->next != 0){/*Middle case*/
      ((sf_free_header*)bp)->prev->next = temp+asize;/*update the next andprev to point to new size*/
      ((sf_free_header*)bp)->next->prev = temp+asize;

    }else if(((sf_free_header*)bp)->prev != 0 && ((sf_free_header*)bp)->next == 0){/*Tail case*/
      ((sf_free_header*)bp)->prev->next = temp+asize;/*Change the address of the  previous in tail*/
    }
    /*save prev blocksize*/
    int block = ((sf_free_header*)bp)->header.block_size<<4;

    ((sf_free_header*)bp)->header.alloc = 1;
    ((sf_free_header*)bp)->header.requested_size = size;
    ((sf_free_header*)bp)->header.block_size = asize>>4;
    sf_footer *sb = bp+asize-8;
    sb->block_size = asize>>4;
    sb->alloc = 1;

    bp = bp + asize;/*Set pointer to new location*/

    /*Change block size only*/
    ((sf_free_header*)bp)->header.alloc = 0;
    ((sf_free_header*)bp)->header.requested_size = 0;
    ((sf_free_header*)bp)->header.block_size = (block-asize)>>4;
    /*Update the footer to display new size*/
    sb = bp+((block-asize)-8);
    sb->block_size = (block-asize)>>4;
    sb->alloc = 0;



  }

}/*End place*/

/*Extends the heap by specified amount, then coaleses*/
static void* extend_heap(int amount){
  void* initial_address_in_heap = heap_limit;/*to retrive for later*/
  int temp = amount;
  while(amount > 0){
  heap_limit +=PAGE_SIZE;
  --amount;
}/*End while*/

  /*reposition the head to its new owner*/
  freelist_head->prev = initial_address_in_heap;

  /*Fill info for new head*/
  ((sf_free_header*)initial_address_in_heap)->header.alloc = 0;
  ((sf_free_header*)initial_address_in_heap)->header.requested_size = 0;/*4064 TOTAL payload*/
  ((sf_free_header*)initial_address_in_heap)->header.block_size = (PAGE_SIZE*temp)>>4;/*block size 4072 due to 24 padding for payload*/

  sf_footer *sb = initial_address_in_heap + ((PAGE_SIZE*temp)-8);
  sb->block_size = (PAGE_SIZE*temp)>>4;
  sb->alloc = 0;

 return coalesce(initial_address_in_heap);/*returns incremented area coaleses*/
}

static int extend_by(int block_size){
  int size = PAGE_SIZE, i;
  if(block_size <= size){
    return 1;
  }else{
    for(i = 1; size < block_size; i++, size += PAGE_SIZE)
    ;
    return i;
  }

}
/****************************For debugging **************************************************************/
void press_to_cont(){
Write("\r\nPress Enter to Continue\r\n",26,1);
	char test[1];
	Read(test, 1, 0);
	while(test[0] != '\n')
	Read(test, 1, 0);
	;
	
	Write("\r\n",2,1);
}


#define check_prim_contents(actual_value, expected_value, fmt_spec, name) do { \
    if (*(actual_value) != (expected_value)) { \
        error("Expected " name " to be " fmt_spec " but got " fmt_spec "\n", (expected_value), *(actual_value)); \
        error("Aborting...\n"); \
        assert(false); \
    } else { \
        success(name " retained the value of " fmt_spec " after assignment\n", (expected_value)); \
    } \
} while(0)

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
int print_num(void *num){
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
/***************************Testing **************************************************************/

int main(){
	char *message_blocks = "\r\nNumber of free blocks: ";
	
	Write("=== Test1: Allocation test ===\n",31,1);
	Write("\r",1,1);
	void *memory = sf_malloc(128);
	print_num(memory);
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
	memory = sf_malloc(50);
	print_num(memory);
	((char *)memory)[0] = 't';
	((char *)memory)[1] = 'e';
	((char *)memory)[2] = 's';
	((char *)memory)[3] = 't';
	((char *)memory)[4] = '2';
	
	Write(memory,5,1);
    sf_free(memory);
    Write("\r\ntest2 done.\r\n",13,1);
    press_to_cont();
      
}
