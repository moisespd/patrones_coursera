// Coursera_Q1: Two threads communicate with eachother to alteratively print out "Ping" and "Pong"
public class Student4__Coursera_Q1 {
	public static void main(String args[])
	{
		// Serves as both as a utility print object and the lock
		Student4__Printer printer = new Student4__Printer(6);
		
		printer.printMsg("Ready... Set... Go!\n");
		
		// PingThread
		Student4__PingPongThread pingThread = new Student4__PingPongThread(printer, "Ping!");
		
		// Just because you create threads in a certain order doesn't mean they will run in the same order.
		//
		// This synchronized block locks onto Student4 as soon as it is avaialable (after PingThread is done with it)
		// Then it releases the lock using wait() therefore allowing pongThread to synchronized properly to Student4 when
		// it is being ccreated.
		//
		// This ensures synchronized block ensures pingThread is always ran first (Without this block or a similar solution
		// sometimes pingThread will start sometimes pongThread will start with no set routine.
		synchronized (printer)
		{
			try
			{
				printer.wait();
			} 
			catch (InterruptedException e)
			{				
				e.printStackTrace();
			}
		}
		
		// PongThread
		Student4__PingPongThread pongThread = new Student4__PingPongThread(printer, "Pong!");	
	}
}
