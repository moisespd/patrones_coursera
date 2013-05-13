public class Student3 implements Runnable{

	private Object lock;
	private boolean state;


	public Student3(Object lock, boolean state){

		this.lock=lock;
		this.state=state;
	}


	public void PrintPing() throws InterruptedException {

		synchronized (lock){

			while(state){
				lock.wait();
			}



			System.out.println("Ping!");
			lock.notifyAll();
			state=true;

		}    

	}

	public void PrintPong() throws InterruptedException {

		synchronized (lock){

			while(!state){
				lock.wait();
			}



			System.out.println("Pong!");
			lock.notifyAll(); 
			state = false;

		}
	}



	@Override
	public void run() {
		try {


			PrintPing();
			PrintPong();




		} catch (InterruptedException ex) {

		}

	}

	public static void main(String[] args) {

		System.out.println("Ready...Set...Go!");
		try {
			Thread.sleep(1000);
		} catch (InterruptedException ex) {

		}
		Object monitor = new Object();
		Student3 r2 = new Student3(monitor,false);
		Thread t = new Thread(r2, "Ping");
		Thread t2 = new Thread(r2, "Pong");
		t.start();
		t2.start();
		try {
			t.join();
			t2.join();
		} catch (InterruptedException ex) {

		}

		System.out.append("Done!");


	}

}