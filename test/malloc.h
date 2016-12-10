#ifndef FooH
#define FooH
#include "syscall.h"
/*
prototypes
*/
 
#define ALLOC_SIZE_BITS 4
#define BLOCK_SIZE_BITS 28
#define REQST_SIZE_BITS 32

#define SF_HEADER_SIZE ((ALLOC_SIZE_BITS + BLOCK_SIZE_BITS + REQST_SIZE_BITS) >> 3)
#define SF_FOOTER_SIZE SF_HEADER_SIZE

/*
            Format of a memory block
    +------------------------------------+
    |            64-bits wide            |
    +------------------------------------+

    +----------------+------------+------+    ------------
    | Requested Size | Block Size | 000a |    Header Block
    |     in bytes   |  in bytes  |      |
    |     32bits     |   28bits   | 4bits|
    +----------------+------------+------+    ------------
    |                                    |    Content of
    |         Payload and Padding        |    the payload
    |           (N Memory Rows)          |
    |                                    |
    |                                    |
    +---------------+-------------+------+    ------------
    |     Unused    | Block Size  | 000a |    Footer Block
    |               |  in bytes   |      |
    +------------------------------------+    ------------

*/

struct sf_header{
    int alloc : ALLOC_SIZE_BITS;
    int block_size : BLOCK_SIZE_BITS;
    int requested_size : REQST_SIZE_BITS;
};
typedef struct sf_header sf_header;

struct sf_free_header {
    sf_header header;
    struct sf_free_header *next;
    struct sf_free_header *prev;
};
typedef struct sf_free_header sf_free_header;

struct sf_footer {
    int alloc : ALLOC_SIZE_BITS;
    int block_size : BLOCK_SIZE_BITS;
    /* Other 32-bits are unused */
};
typedef struct sf_footer sf_footer;


 static void* find_fit(int size);
 static void place(void *bp, int asize, int size);
 static int extend_by(int block_size);
 static void* extend_heap(int amount);
 static void free_block(void* ptr);
 static void* coalesce(void* ptr);
 static int coalesce_case(void* ptr);
 int print_n(void* ptr);

/**
 * You should store the head of your free list in this variable.
 */
extern sf_free_header *freelist_head;

/* sfmm.c: Where you will define your functions for this assignment. */

/**
 * This is your implementation of malloc. It creates dynamic memory which
 * is aligned and padded properly for the underlying system. This memory
 * is uninitialized.
 * @param size The number of bytes requested to be allocated.
 * @return If successful, the pointer to a valid region of memory
 * to use is returned, else the value NULL is returned and the
 * ERRNO is set accordingly. If size is set to zero, then the
 * value NULL is returned.
 */
void* malloc(int size);

/**
 * Marks a dynamically allocated region as no longer in use.
 * @param ptr Address of memory returned by the function sf_malloc,
 * sf_realloc, or sf_calloc.
 */
void free(void *ptr);
#endif
