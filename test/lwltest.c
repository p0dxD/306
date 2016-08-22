/*
 * C source for which gcc generates unaligned lwl/swl instructions.
 * Based on: http://www.linux-mips.org/wiki/Alignment
 */


struct foo {
  unsigned char bar[8];
};

void dummy(unsigned char c) {
  c = c;
}

main(int argc, char *argv[])
{
  struct foo x = {0, 1, 2, 3, 4, 5, 6, 7};
  char s[] = "abcdef";
  int i;


  for(i = 0; i <= 7; i++)
    dummy(x.bar[i]);
}
