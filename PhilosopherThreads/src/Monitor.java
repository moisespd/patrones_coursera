/* -----------------------------------------------------------------------------
 * NPHILOSOPHERS:
 * 		Number of philosophers and chopsticks
 * NMOUTHFUL: 
 * 		Number of times each philosopher has to eat to finish his dinner 
 * chopsticks:
 * 		chopsticks[i] = -1 means chopstick number i is not being used   
 * 		chopsticks[i] = X means chopstick number i is actually being used by philosopher number X
----------------------------------------------------------------------------- */   

public class Monitor {
	private static final int NPHILOSOPHERS = 5;
	private static final int NMOUTHFUL = 5;
	private int[] chopsticks = new int[NPHILOSOPHERS];

	// -------------------------------------------------------------------------
	public Monitor() {
		for (int i = 0; i < NPHILOSOPHERS; i++) {
			chopsticks[i] = -1;
		}
	}
	// -------------------------------------------------------------------------
	// pickupChopstick:
	//		Philosopher p requests chopstick number <chopstick>
	// If philosopher p has the left chopstick in his left hand, and he is asking for his right 
	// chopstick, wich is being used, so he has to wait, but first, he releases the chopstick he 
	// has in his left hand, so at least another philosopher can eat 
	// -------------------------------------------------------------------------
	public synchronized void pickupChopstick(Philosopher p, int chopstick) {
		try {
			if (chopsticks[chopstick] != -1) {
				if (chopsticks[p.getMyLeft()] == p.getMyName())
					putdownChopstick(p, p.getMyLeft());

				if (chopsticks[p.getMyRight()] == p.getMyName())
					putdownChopstick(p, p.getMyRight());
				
				wait();
			}
		
			chopsticks[chopstick] = p.getMyName();
			if (chopstick == p.getMyLeft())
				System.out.println("Philosopher " + p.getMyName() + " picks up left chopstick");
			else 
				System.out.println("Philosopher " + p.getMyName() + " picks up right chopstick");
			
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	// -------------------------------------------------------------------------
	// putdownChopstick
	// -------------------------------------------------------------------------
	public synchronized void putdownChopstick(Philosopher p, int chopstick) {
		chopsticks[chopstick] = -1;
		
		if (chopstick == p.getMyLeft())
			System.out.println("Philosopher " + p.getMyName() + " puts down left chopstick");
		else 
			System.out.println("Philosopher " + p.getMyName() + " puts down right chopstick");
		
		notifyAll();
	}
	
	// -------------------------------------------------------------------------
	// -------------------------------------------------------------------------
	// -------------------------------------------------------------------------
	public class Philosopher extends Thread {
		private int myname;
		private int myleft, myright;
		private Monitor m;
		private int mouthful;
		
		public Philosopher(int name, Monitor m) {
			this.myname = name;
			this.m = m;
			this.myleft = myname;
			this.myright = (myname + 1) % Monitor.NPHILOSOPHERS;
			this.mouthful = 0;
		}

		public int getMyName() {
			return myname;
		}
		
		public int getMyLeft() {
			return myleft;
		}
		
		public int getMyRight() {
			return myright;
		}
		
		public void zleep() {
			try {
				sleep(1000);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		public void run() {
			while (this.mouthful < Monitor.NMOUTHFUL)
				eat();
		}
		
		public void eat() {
			m.pickupChopstick(this, myleft);

			m.pickupChopstick(this, myright);

			this.mouthful++;
			System.out.println("Philosopher " + myname + " eats");
			
			m.putdownChopstick(this, myright);
			m.putdownChopstick(this, myleft);
		}
	}
	// -------------------------------------------------------------------------
	// -------------------------------------------------------------------------
	// -------------------------------------------------------------------------

	public static void main(String... arg) {
		Monitor m = new Monitor();
		Philosopher[] philosophers = new Philosopher[Monitor.NPHILOSOPHERS];

		for (int i = 0; i < Monitor.NPHILOSOPHERS; i++) {
			philosophers[i] = m.new Philosopher(i, m);
			philosophers[i].start();
		}
		
		try {
			for (int i = 0; i < Monitor.NPHILOSOPHERS; i++) {
				philosophers[i].join();
			}
			System.out.println("Dinner is over!");
		}
		catch (InterruptedException e) {
			System.out.println("ERROR");
			e.printStackTrace();
		}
	}
	// -------------------------------------------------------------------------
	// -------------------------------------------------------------------------
	// -------------------------------------------------------------------------
}


