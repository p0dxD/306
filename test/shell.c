#include "syscall.h"

int
main()
{
    SpaceId newProc;
    OpenFileId input = ConsoleInput;
    OpenFileId output = ConsoleOutput;
    char prompt[2], ch, buffer[60];
    int i;

    prompt[0] = '-';
    prompt[1] = '-';

    while(1) {
	Write(prompt, 2, output);
	//Write("shell: ", 6, output);
	i = 0;
	
	do {
	  Read(&buffer[i], 1, input); 
	  Write(&buffer[i], 1, output);

	} while( buffer[i++] != '\n' );
	Write("\r", 1, output);
	buffer[--i] = '\0';

	if( i > 0 ) {
	    if ((buffer[0] == 'e') &&
		(buffer[1] == 'x') &&
		(buffer[2] == 'i') &&
		(buffer[3] == 't') &&
		(buffer[4] == '\0'))
		break;
	    newProc = Exec(buffer);
	    Join(newProc);
	    //Write("After", 5, 1);
	    
	}
    }
    Halt();
}

