// PingPong thread
class Student4__PingPongThread extends Thread
{
	Student4__Printer printer;
	String message;	
	
	// Constructor which takes in message - "Ping" or "Pong"
	public Student4__PingPongThread(Student4__Printer printer, String message)
	{
		this.printer = printer;
		this.message = message;
		this.start();
	}

	@Override
	public void run()
	{
		while(true)
		{
			synchronized (printer)
			{					
				// Print message whether it is Ping or Pong
				printer.printMsg(message);
				
				// Notify
				printer.notify();
				
				// Wait
				try
				{
					printer.wait();
				} 
				catch (InterruptedException e)
				{				
					e.printStackTrace();
				}
			}
		}
	}
	
	
}
