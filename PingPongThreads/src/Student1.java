import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
* This application is demonstrate how two threads run and display message alternatively.
*
*/
public class Student1 implements Runnable {

    private String message;
    private final Semaphore semaphore;
    private int times;

    public Student1(String message, Semaphore semaphore, int times) {
        this.message = message;
        this.semaphore = semaphore;
        this.times = times;
    }

    public void run() {
        for (int i = 0; i < times; i++) {
            try {
                semaphore.acquire();
                System.out.println(message);
                Thread.sleep(300);

            } catch (InterruptedException ex) {
                Logger.getLogger(Student1.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                semaphore.release();
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        //Semaphore with 1 permit
        Semaphore s1 = new Semaphore(1,true);
        
        //Create 2 threads
        Thread t = new Thread(new Student1("Ping!", s1, 3));
        Thread t2 = new Thread(new Student1("Pong!", s1, 3));
        
        System.out.print("Ready...");
        Thread.sleep(1000);
        System.out.print("Set...");
        Thread.sleep(1000);
        System.out.println("Go!\n");
        Thread.sleep(300);
        
        //Threads start
        t.start();
        t2.start();

        t.join();
        t2.join();
        
        System.out.println("Done!");
    }
}