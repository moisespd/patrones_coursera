import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Main class to kick off the dining - contains the main method that must be run.
 */
public class Student3 {

	/**
	 * Monitor object class controls access to the chopsticks and notifies
	 * philosophers when there is a change to the chopstick state. 
	 */
	static class MonitorObject {
		private Set<Philosopher> waitingPhilosophers;
		private boolean[] chopstickAvailability;

		// Note: the number of chopsticks and philosophers are always assumed to be the same
		public MonitorObject(int numChopsticks) {
			this.waitingPhilosophers = new HashSet<Philosopher>(numChopsticks);
			this.chopstickAvailability = new boolean[numChopsticks];
			// All chopsticks are initially available
			for(int i = 0; i < numChopsticks; i++)
				this.chopstickAvailability[i] = true;
		}
		
		// A philosopher only takes a chopstick when both of those that he needs are available,
		// thereby avoiding deadlock that can occur if everyone takes just one.
		public synchronized boolean acquireChopsticks(Philosopher philosopher) {
			// If both chopsticks are available, grab them and return true
			if (areNeededChopsticksAvailable(philosopher)) {
				this.chopstickAvailability[philosopher.getFirstNeededChopstickNumber()] = false;
				System.out.println("Philosopher " + philosopher.getPhilosopherNumber() + " picks up left chopstick.");
				this.chopstickAvailability[philosopher.getSecondNeededChopstickNumber()] = false;
				System.out.println("Philosopher " + philosopher.getPhilosopherNumber() + " picks up right chopstick.");
				return true;
			}
			
			// If both chopsticks were not available, add this philosopher to the list
			// of those waiting and who will be notified when any chopsticks are returned
			this.waitingPhilosophers.add(philosopher);
			return false;
		}
		
		public synchronized void returnChopsticks(Philosopher philosopher) {
			// Make the two chopsticks available again
			this.chopstickAvailability[philosopher.getFirstNeededChopstickNumber()] = true;
			System.out.println("Philosopher " + philosopher.getPhilosopherNumber() + " puts down left chopstick.");
			this.chopstickAvailability[philosopher.getSecondNeededChopstickNumber()] = true;
			System.out.println("Philosopher " + philosopher.getPhilosopherNumber() + " puts down right chopstick.");
			
			// Notify all waiting philosophers of the change (on their own objects)
			// and clear the waiting list (as all of them will now have been woken)
			for (Philosopher waitingPhilosopher : waitingPhilosophers) {
				waitingPhilosopher.wake();
			}
			this.waitingPhilosophers.clear();
		}
		
		// Utility method checks that both chopsticks are in an available state
		private boolean areNeededChopsticksAvailable(Philosopher philosopher) {
			return (this.chopstickAvailability[philosopher.getFirstNeededChopstickNumber()] && 
					this.chopstickAvailability[philosopher.getSecondNeededChopstickNumber()]);
		}
	}
	
	/**
	 * Philosopher class designed to run in its own thread and models a
	 * philosopher's behaviour.
	 */
	static class Philosopher implements Runnable {
		private int philosopherNumber;
		private boolean lastPhilosopher; // A philosopher needs to know if he is "last" so that his right chopstick is then number zero
		private int timesLeftToEat;
		private MonitorObject monitor;
		
		public Philosopher(int philosopherNumber, boolean lastPhilosopher, int timesToEat, MonitorObject monitor) {
			this.philosopherNumber = philosopherNumber;
			this.lastPhilosopher = lastPhilosopher;
			this.timesLeftToEat = timesToEat;
			this.monitor = monitor;
		}
		
		public int getPhilosopherNumber() {
			return this.philosopherNumber;
		}
		
		public int getFirstNeededChopstickNumber() {
			return philosopherNumber - 1;
		}
		
		public int getSecondNeededChopstickNumber() {
			if (this.lastPhilosopher) {
				return 0;
			}
			return philosopherNumber;
		}
		
		public void eat() {
			System.out.println("Philosopher " + this.philosopherNumber + " eats.");
		}
		
		// Utility method allowing notify to be called within a method which is
		// synchronised on THIS philosopher's lock not the monitor object's lock
		public synchronized void wake() {
			notify();
		}
		
		@Override
		public synchronized void run() {
			while (this.timesLeftToEat > 0) {
				// Try to acquire needed chopsticks and wait if not available
				while (!this.monitor.acquireChopsticks(this)) {
					try {
						wait();
					} catch (InterruptedException e) {}
				}
				
				// Once chopsticks available, eat, return them and decrement count
				eat();
				monitor.returnChopsticks(this);
				this.timesLeftToEat--;
				
				// Pause briefly before rushing back to eat - to be polite
				// (not needed for deadlock prevention, test with these lines removed
				// if you don't believe me!)
				try {
					wait(100);
				} catch (InterruptedException e) {}
			}
		}
	}
	
	/**
	 * The main method sets the whole dining experience going.
	 */
	public static void main(String[] args) {
		int numPhilsophers = 5;  // also the number of chopsticks
		int timesToEat = 5;
		List<Thread> philosopherThreads = new ArrayList<Thread>();
		
		System.out.println("Dinner is starting!");
		System.out.println();
		
		// Create the monitor object that controls access to chopsticks
		MonitorObject monitor = new MonitorObject(numPhilsophers);
		
		// Create a thread for each philosopher and start them all
		for (int i = 1; i <= numPhilsophers; i++) {
			Thread t = new Thread(new Philosopher(i, 
												  i == numPhilsophers ? true : false,
												  timesToEat, 
												  monitor));
			t.start();
			philosopherThreads.add(t);
		}

		// Join all the threads so the main program thread waits for all 
		// the eating to be done 
		for (Thread t : philosopherThreads) {
			try {
				t.join();
			} catch (InterruptedException e) {}
		}
		
		System.out.println();
		System.out.println("Dinner is over!");
	}

}