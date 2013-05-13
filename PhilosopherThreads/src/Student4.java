import java.util.concurrent.*;


class MonitorObject
{
	int		MyNumber; // My resource number (1..5)
	int		MyOwner;  // Which thread/philosopher has acquired me (may be "NoOwner")
	Semaphore	MySem;	  // Semaphore used to ensure only one thread at a time has this resource.

	static final int NoOwner = 0;
	static final int WaitTime = 5;

	MonitorObject(int num) // Constructor
	{
		MyNumber = num;
		MyOwner = NoOwner;
		MySem = new Semaphore(1);
	}

	/* Synchronized acquire method. If the resource is not free, call "wait"
	 ** to allow another thread to call "release" without blocking at the
	 ** "synchronized" mutex.
	 */
	synchronized void acquire(int phil, boolean isLeft) throws InterruptedException {
		// While the resource is in use, call "wait" with a small timeout,
		// releasing // the object mutex and allowing "release" to be called.
		while (MyOwner != NoOwner)
			wait(WaitTime);
		// Acquire MySem to indicate that this resource is now in use.
		MySem.acquire();
		MyOwner = phil;
		if (isLeft)
			System.out.println("Philosopher " + phil + " picks up left chopstick.");
		else
			System.out.println("Philosopher " + phil + " picks up right chopstick.");
	}

	/* Synchronized release method. Note that the synchronized acquire method
	 ** above calls wait to allow this method to be entered if that method is
	 ** otherwise blocked.
	 */
	synchronized void release(int phil, boolean isLeft)
	{
		MyOwner = NoOwner;
		// Release MySem to free up this resource.
		MySem.release();
		// Call notify to wake other thread waiting on this resource.
		notify();
		if (isLeft)
			System.out.println("Philosopher " + phil + " puts down left chopstick.");
		else
			System.out.println("Philosopher " + phil + " puts down right chopstick.");
	}
} // end class MonitorObject


class PhilosopherThread extends Thread
{
	int MyNumber;
	MonitorObject[]	MyChopsticks;
	int	MyNumMouthfuls;

	static final int	ThinkTime = 5;	// Milliseconds (our philosphers
	// are quick thinkers!)

	// Constructor
	PhilosopherThread(int philNum, MonitorObject[] chopsticks, int numMouthfuls)
	{
		MyNumber = philNum;
		MyChopsticks = chopsticks;
		MyNumMouthfuls = numMouthfuls;
	}

	public void run()
	{
		int     chopLeft, chopRight;
		int     chopLower, chopHigher;
		boolean lowerIsLeft;
		int     numChopsticks;
		boolean	acquired;

		/*
		 ** Firstly, specify that the chopstick on the left of the philosopher
		 ** has the same number as the philosopher, and the one on the right is
		 ** one higher (but allowing for wraparound, so that the highest
		 ** philosopher (5) has chopstick 1 on his right).
		 */
		/*
		 ** We solve the deadlock problem by assigning priorities to the
		 ** different resources. In this case, the resources are the chopsticks.
		 ** They are numbered from 1 to 5, and we make each philosopher (thread)
		 ** pick up the lowest numbered chopstick first. This prevents deadlock,
		 ** because the highest-valued chopstick (5) will never be picked up
		 ** first, only second.
		 ** This assignment of different priorities to the different resources
		 ** was suggested by Dijkstra as a solution for the Dining Philosopher's
		 ** problem. It's a nice simple solution, so use it, and not make this
		 ** assignment more difficult than it needs to be.
		 */
		/*
		 ** Note that we are not using element 0 of the MyChopsticks array (we
		 ** use numbers 1..MAX purely for clarity with the problem description).
		 */
		numChopsticks = MyChopsticks.length - 1;
		// Find the number of the chopsticks to my left and right
		chopLeft = MyNumber;
		chopRight = MyNumber + 1;
		if (chopRight > numChopsticks)
			chopRight = 1;
		// Find out which chopsticks have the lowest and highest priorities
		if (chopLeft < chopRight)
		{
			chopLower = chopLeft;
			chopHigher = chopRight;
			lowerIsLeft = true;
		}
		else
		{
			chopLower = chopRight;
			chopHigher = chopLeft;
			lowerIsLeft = false;
		}
		// Think and eat!
		for (int mouthful = 1; mouthful <= MyNumMouthfuls; mouthful++)
		{
			/*
			 ** Think (sleep) for a small amount of time. This isn't really
			 ** required, but the problem description does mention a "thinking"
			 ** activity.
			 ** Note that we don't use a random amount of time here. Using
			 ** different random time intervals makes any deadlock *less*
			 ** likely, so adding a random interval here may help to mask any
			 ** problems in the implementation - we don't want any problems to
			 ** be masked, we want to find whether our solution is correct.
			 */
			try {
				sleep(ThinkTime);
			} catch (InterruptedException e) {
				System.out.println("*** sleep interrupted");
			};

			// Get first chopstick
			acquired = false;
			while (! acquired)
			{
				try {
					MyChopsticks[chopLower].acquire(MyNumber, lowerIsLeft);
					acquired = true;
				} catch (InterruptedException e) {
					System.out.println("*** acquire " + chopLower + " interrupted");
				};
			}
			// Get second chopstick
			acquired = false;
			while (! acquired)
			{
				try {
					MyChopsticks[chopHigher].acquire(MyNumber, !lowerIsLeft);
					acquired = true;
				} catch (InterruptedException e) {
					System.out.println("*** acquire " + chopHigher + " interrupted");
				};
			}

			// Eat
			System.out.println("Philosopher " + MyNumber + " eats.");

			// Put down chopsticks
			MyChopsticks[chopHigher].release(MyNumber, !lowerIsLeft);
			MyChopsticks[chopLower].release(MyNumber, lowerIsLeft);
		}
	}
} // end class PhilosopherThread


public class Student4
{
	final static int NumPhilosophers = 5;
	final static int NumChopsticks = 5;
	final static int NumMouthfuls = 5;

	public static void main(String[] args)
	{
		/* NOTE: We create one more PhilosopherThread/MonitorObject than we
		 **       need. This is because we want to index the threads/objects
		 **       from 1, not 0 (i.e. we refer to the first thread as thread 1).
		 **       This is slightly wasteful on resources, but simplifies the
		 **       solution. If this program was for a real-world application,
		 **       rather than just an exercise, this decision may have been
		 **       considered differently.
		 */
		PhilosopherThread philthread[] = new PhilosopherThread[NumPhilosophers + 1];
		MonitorObject chopsticks[] = new MonitorObject[NumChopsticks + 1];

		System.out.println("STUDENT 4: Dinner is starting!");

		for (int chop = 1; chop <= NumChopsticks; chop++)
			chopsticks[chop] = new MonitorObject(chop);

		for (int phil = 1; phil <= NumPhilosophers; phil++)
			philthread[phil] = new PhilosopherThread(phil, chopsticks, NumMouthfuls);

		for (int phil = 1; phil <= NumPhilosophers; phil++)
			philthread[phil].start();

		try {
			for (int phil = 1; phil <= NumPhilosophers; phil++)
				philthread[phil].join();
		} catch (Exception exc) {
			// Unexpected thread termination
			System.out.println("*** Thread interrupted");
		}
		finally {
			// Print output line to show that all threads have now terminated.
			System.out.println("Dinner is over!");
		}
	} // end main
} // end class Student4
