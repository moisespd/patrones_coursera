import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Student2 {

	/**
	 * @param args
	 */
	private class Philosopher implements Runnable {
		private Chopstick l, r;
		private int ident;
		private int byteseatten;

		//Chopsticks are distinct resources so each
		//philosopher knows which she can use.
		public Philosopher(Chopstick left, Chopstick right, int i) {
			l = left;
			r = right;
			ident = i;
			byteseatten = 0;
		}

		public int getIdent() {
			return ident;
		}

		public void run() {
			//Philospher repeatedly tries to get her chopsticks
			//and eats until 5 bytes
			while (byteseatten < 5) {
				l.get(this,"left");
				r.get(this,"right");
				eat();
				r.release();
				l.release();
			
			}
			//System.out.println("Philosopher "+this.ident+" is full!");
		}

		private void eat() {
			//System.out.println("Philsopher " + ident + " eats.");
			byteseatten++;
		}

	}

	private class Chopstick {
		private int ident;
		private final Lock lock = new ReentrantLock();

		public Chopstick(int i) {
			ident = i;
		}
		
		public synchronized void release(){
				//System.out.println("Chopstick "+this.ident+" released");
				lock.unlock();
		}		
		
		public synchronized void get(Philosopher p,String side) {
			if (lock.tryLock()) {
				try {
					System.out.println("Philosopher " + p.getIdent()
							+ " picks up "+side+" chopstick ");
				} catch (Exception e){
					e.printStackTrace();
				} finally {}
			} else {
				//System.out.println("telling philospher" + ident + "to wait");
				try {
					p.wait(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalMonitorStateException e){
					e.printStackTrace();
				}
			}
		}
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Student2 p = new Student2();
		p.work();
	}

	private void work() {
		Philosopher p1, p2, p3, p4, p5;
		Chopstick cs1 = new Chopstick(1);
		Chopstick cs2 = new Chopstick(2);
		Chopstick cs3 = new Chopstick(3);
		Chopstick cs4 = new Chopstick(4);
		Chopstick cs5 = new Chopstick(5);
		Thread th1 = new Thread(new Philosopher(cs1, cs2, 1));
		Thread th2 = new Thread(new Philosopher(cs2, cs3, 2));
		Thread th3 = new Thread(new Philosopher(cs3, cs4, 3));
		Thread th4 = new Thread(new Philosopher(cs4, cs5, 4));
		Thread th5 = new Thread(new Philosopher(cs5, cs1, 5));
		System.out.println("Dinner is starting!");
		th1.start();
		th2.start();
		th3.start();
		th4.start();
		th5.start();
		try {
			th1.join();
			th2.join();
			th3.join();
			th4.join();
			th5.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Dinner is over!");
	}
}