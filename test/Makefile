# use normal make for this Makefile
#
# Makefile for building user programs to run on top of Nachos
#
# Several things to be aware of:
#
#    Nachos assumes that the location of the program startup routine (the
# 	location the kernel jumps to when the program initially starts up)
#       is at location 0.  This means: start.o must be the first .o passed 
# 	to ld, in order for the routine "Start" to be loaded at location 0
#

# Change the following to point to where you have the MIPS cross-compiler
# installed.
GCCDIR = /usr/local/gcc-3.2.2_ppc_cross_mips-coff/

# Change the following to point to where you have Java executables installed.
JAVADIR = /usr/bin/

# Uncomment the following when running on Windows.
#JAVA= $(JAVADIR)java -classpath '..'

# Uncomment the following when running on Unix.
JAVA= $(JAVADIR)java -classpath '..'

CC = $(GCCDIR)gcc
AS = $(GCCDIR)as
LD = $(GCCDIR)ld
CPP = $(GCCDIR)cpp0

COFF2NOFF = $(JAVA) nachos.noff.Coff2Noff

LDFLAGS = -T script -N
ASFLAGS = -mips1
CPPFLAGS = -I$(INCDIR)

CFLAGS = -G 0 -I$(INCDIR) -B$(GCCDIR) -c -fno-builtin
SFLAGS = -G 0 -I$(INCDIR) -B$(GCCDIR) -c -S -fno-builtin

NACHOSDIR=..
INCDIR = $(NACHOSDIR)/test

.SUFFIXES:

all:

start.o: start.s $(INCDIR)/syscall.h
	$(CPP) $(CPPFLAGS) start.s > strt.s
	$(AS) $(ASFLAGS) -o start.o strt.s
	rm strt.s

halt.s: halt.c
	$(CC) $(SFLAGS) halt.c
halt.o: halt.c
	$(CC) $(CFLAGS) halt.c
halt: halt.o start.o
	$(LD) $(LDFLAGS) start.o halt.o -o halt.coff
	rm halt.o
	$(COFF2NOFF) halt.coff halt
	rm halt.coff

exec1.s: exec1.c
	$(CC) $(SFLAGS) exec1.c
exec1.o: exec1.c
	$(CC) $(CFLAGS) exec1.c
exec1: exec1.o start.o
	$(LD) $(LDFLAGS) start.o exec1.o -o exec1.coff
	rm exec1.o
	$(COFF2NOFF) exec1.coff exec1
	rm exec1.coff
	
# If you are using gmake, the following rules should enable you to
# compile any of the test programs.  Otherwise, you have to copy the
# above code for "halt", making appropriate changes, to compile other
# test programs.

%.s: %.c
	$(CC) $(SFLAGS) $< 
%.o: %.c
	$(CC) $(CFLAGS) $<
%: %.o start.o
	$(LD) $(LDFLAGS) start.o $< -o $*.coff
	rm $*.o
	$(COFF2NOFF) $*.coff $@
	rm $*.coff
