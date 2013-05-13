
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;

public class Client implements Runnable {
	
	private final InetAddress server;
	private final int port;
	
	public Client(InetAddress server, int port) {
		this.server = server;
		this.port = port;
	}
	

	public void run() {
		BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
		DataOutputStream out;
		

		Socket socket = null;
		try {
			socket = new Socket(server, port); 
			out = new DataOutputStream(socket.getOutputStream());;
			
			while (true) {
				System.out.print("text to send> ");
				String message = stdIn.readLine();
				out.writeUTF(message);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
    	} finally {
    		try {
    			socket.close();
    		} catch (Exception e) {
    		}
    	}
	}
	
	
	public static void main(String args[]) {
		try {
			InetAddress server = InetAddress.getLocalHost();
			Client client = new Client(server, 8080);
			client.run();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
}
