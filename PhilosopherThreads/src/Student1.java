import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class Philosopher implements Runnable {
	private int id = 1;
	private int currDiningRound = 0;
	private final int diningRounds = 5;

	private Lock leftStick;
	private Lock rightStick;

	public Philosopher(int id, Lock leftStick, Lock rightStick) {
		this.leftStick = leftStick;
		this.rightStick = rightStick;
		this.id = id + 1;
	}

	public void run() {
		while (currDiningRound < diningRounds) {
			System.out.println("Philosopher " + id + " tries to eat");
			if (rightStick.tryLock()) {
				System.out.println("Philosopher " + id + " picks up right chopstick.");
				try {
					if (leftStick.tryLock(1L, TimeUnit.SECONDS)) {
						System.out.println("Philosopher " + id + " picks up left chopstick.");
						try {
							System.out.println("Philosopher " + id + " eats.");
							Random randomise = new Random();
							Thread.sleep(randomise.nextInt(100));
							++currDiningRound;
						} catch (InterruptedException e) {
						} finally {
							leftStick.unlock();
							System.out.println("Philosopher " + id + " drops left chopstick.");
						}
					}
				} catch (InterruptedException e) {
				} finally {
					rightStick.unlock();
					System.out.println("Philosopher " + id + " drops right chopstick.");
				}
			}
		}
	}
}

class Student1 {
	public static void main(String[] args) throws Exception {

		System.out.println("Dinner is starting!");

		final int NUMBER_OF_THREADS_TO_START = 5;

		ReentrantLock[] sticks = new ReentrantLock[NUMBER_OF_THREADS_TO_START];

		for (int i = 0; i < sticks.length; i++)
			sticks[i] = new ReentrantLock();

		Thread[] threads = new Thread[NUMBER_OF_THREADS_TO_START];

		for (int i = 0; i < threads.length; i++) {
			// non-negative id of the left stick
			int prev = (threads.length + i - 1) % threads.length;

			threads[i] = new Thread(new Philosopher(i, sticks[prev], sticks[i]));
			threads[i].start();
		}

		try {
			for (Thread thread : threads)
				thread.join();

		} catch (InterruptedException e) {
		}

		// Now we have a single main thread again
		System.out.println("Dinner is over!");
	}
}