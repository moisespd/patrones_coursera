/* -----------------------------------------------------------------------------
 * MAX: 
 * 		Number of wished ping-pong strings 
 * counter: 
 * 		Actual number of ping-pong shown
 * ponged:
 * 		false means no pong done, so turn to do pong   
 * 		true means pong already done, so turn to do ping
----------------------------------------------------------------------------- */   
public class PingPong {
	private final int MAX = 6;				
	private volatile int counter = 0;			 
	private volatile boolean ponged = false;

	
	public synchronized void ping() {
		try {
			if (ponged) {
				counter++;
				System.out.println("Ping");
				ponged = false;
			}
			wait();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public synchronized void pong() {
		if (!ponged) {
			counter++;
			System.out.println("Pong");
			ponged = true;
		}
		notifyAll();
	}

	public static void main(String... arg) {
		final PingPong pingpong = new PingPong();

		Thread ping = new Thread() {
			public void run() {
				while (pingpong.counter < pingpong.MAX)
					pingpong.ping();
			}
		};
		
		Thread pong = new Thread() {
			public void run() {
				while (pingpong.counter < pingpong.MAX)
					pingpong.pong();
			}
		};
		
		ping.start();
		pong.start();
		
		try {
			ping.join();
			pong.join();
			System.out.println("Done!");
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
