// Student4 class - will be the object that both threads lock / synchronize onto
class Student4__Printer {
	int numberOfMessages;
	int messageCount;
	
	// Constructor allowing user to choose how many messages are displayed
	Student4__Printer(int numberOfMessages)
	{		
		this.numberOfMessages = numberOfMessages;
		this.messageCount = 0;
	}
	
	// If more messages are to be printed, print and increment messageCount
	void printMsg(String msg)
	{
		if (messageCount <= numberOfMessages)
		{
			System.out.println(msg);			
			++messageCount;
		}
		// Else threads have reached count and therefore completed
		else
		{
			System.out.println("Done!");   // Print "Done!"
			System.exit(0);   // Exit program
		}
	}
}


