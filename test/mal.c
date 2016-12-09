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


/**
 * You should store the head of your free list in this variable.
 * Doing so will make it accessible via the extern statement in sfmm.h
 * which will allow you to pass the address to sf_snapshot in a different file.
 */

/* #ifdef NEXT*/
/* static void *rover;           Next fit rover */
/* #endif*/
 /* static void print_blocks(void);*/
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
  init_address = heap_limit = heap_start;
  int k = ((int)init_address & 0xF);
/*ALIGNMENT*/
  if(k == 0){
      heap_limit += 8;
      heap_start = heap_limit;
  }else{
    if(k == 8){
      /*Do nothing*/
    }else if(k > 8){
    heap_limit+=((16-k)+8);
    heap_start = heap_limit;
    }else if(k < 8){
    heap_limit+=(8-k);
    heap_start = heap_limit;
    }
  }
/*END ALIGNMENT*/
init_address = heap_start;
/* printf("%p\n", init_address);*/
  if(freelist_head == 0){
    heap_start = init_address;/*keep check of the start*/
    
      freelist_head = init_address;
      freelist_head->header.alloc = 0;
      freelist_head->header.requested_size = 0;/*4064 TOTAL payload*/
      freelist_head->header.block_size = PAGE_SIZE>>4;/*block size 4072 due to 24 padding for payload*/
      freelist_head->next = 0;
      freelist_head->prev = 0;
      sf_footer *sb = heap_limit-8;
      sb->block_size = PAGE_SIZE>>4;
      sb->alloc = 0;

      /* #ifdef NEXT*/
      /*   rover = freelist_head;*/
      /* #endif */
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
    /* void* saved = ptr;*/
    /* sf_blockprint(freelist_head);*/
    /* printf("INSIDE FREEE %p\n", ptr);*/
    /* sf_blockprint(ptr);*/
    free_block(ptr);
    /* printf("FREE BLOCK%p\n", ptr);*/

    /* print_blocks();*/
    coalesce(ptr);
    /* printf("DONE COLESEESE\n");*/
    /* sf_blockprint(freelist_head);*/
    /* print_blocks();*/
}


/****************************************HELPERS************************************************************/

static void* coalesce(void* ptr){
  /* printf("%s\n","INSIDE COALESCE" );*/
  void* before = 0;
  int block_size;
  sf_footer* sb;
  if(ptr != heap_start){
    before = ptr-8;
    before = before-(((sf_footer *)before)->block_size<<4)+8;
  }else{
    before = ptr;
  }
  void* after = ptr + (((sf_free_header*)ptr)->header.block_size<<4);


  switch(coalesce_case(ptr)){

    case 2:/*The one aftr is free*/
    /* printf("Case 2\n");*/

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

    /* printf("Case 3%p\n", freelist_head);*/
    /* print_blocks();*/
    if(((sf_free_header*)after)->prev != 0 && ((sf_free_header*)after)->prev->next != 0)
      ((sf_free_header*)after)->prev->next = ((sf_free_header*)after)->next;/*Make my next its next*/
    if(((sf_free_header*)after)->next!= 0 && ((sf_free_header*)after)->next->prev != 0)
      ((sf_free_header*)after)->next->prev = ((sf_free_header*)after)->prev;/*Make my prev its prev*/
/* printf("%s\n", "SEF?");*/
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

      /* printf("Case 4\n");*/
      /* print_blocks();*/
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
    /* #ifdef NEXT                                                        */
    /* Make sure the rover isn't pointing into the free block */
    /* that we just coalesced */
    /*       rover = ptr;*/
    /**/
    /* #endif*/
    /* printf("Case 1\n");*/

    break;
  }/*END switch*/
  /*TAKE CARE OF HEAD*/
  if(freelist_head == ((sf_free_header*)before)){
    /* printf("%s\n", "equal to before");*/
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
    /* printf("%s\n", "equal to after");*/
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
  /* printf("%s\n", "default");*/
  /* sf_blockprint(ptr);*/
  if(freelist_head != 0){
    /* printf("%s\n", "Not null");*/
    /* print_blocks();*/
    /* sf_blockprint(ptr);*/
    freelist_head->prev = ptr;
    /* sf_blockprint(freelist_head->prev);*/
    /* freelist_head = ptr;*/
    ((sf_free_header*)ptr)->next = freelist_head;
    /* printf("%p\n", freelist_head);*/
    ((sf_free_header*)ptr)->prev = 0;
    freelist_head = ptr;
    /* printf("%s\n", "BEFORE");*/
    /* sf_blockprint(freelist_head);*/
    /* printf("The free block next prev \n", freelist_head->next->prev);*/
}else{
    freelist_head = ptr;
    freelist_head->next = 0;
    freelist_head->prev = 0;
}
#endif
/* printf("AFTER its done \n");*/
/* print_blocks();*/

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

int main(){
	Write("=== Test1: Allocation test ===\n\r",31,1);
	void *memory = sf_malloc(92);
    sf_free(memory);
    press_to_cont();
}
