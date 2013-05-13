import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

public class Server {
	public static void main(String[] args) throws IOException, InterruptedException {
		
		int port = 12345;
		if(args.length != 0) {
			try {
				port = Integer.parseInt(args[0]);
			} catch(NumberFormatException nfe) {
				// ignore, use default port
			}
		}
		final int serverPort = port;
		
		Thread serverThread = new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					SocketAddress endpoint = new InetSocketAddress(serverPort); 
					System.out.printf("Server has been started on port %d.\n", serverPort);
					Acceptor acceptor = new Acceptor(endpoint);
					Reactor.getInstance().open(11);
					acceptor.register(Reactor.getInstance(), EventTypes.accept);
					
					Reactor.getInstance().runEventLoop();
					
					System.out.println("Server has been stoped.");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		serverThread.start();
		
		System.in.read();
		Reactor.getInstance().close();
		
		serverThread.join();
	}
}
