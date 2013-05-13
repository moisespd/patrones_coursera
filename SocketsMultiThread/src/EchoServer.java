import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ServerSocket;
import java.net.Socket;



/* ---------------------------------------------------------------------------------------
 * EventHandler:
 * 	interface with all the possible events than Reactor class reacts to
 *  Reactor class will need a concrete EventHandler implementation
 *  Reactor class calls these hook methods when any of these events occur 
 *   
 * 	Here are all the possible events are: 
 * 		- new connection stablished: connectionStablished_handler
 * 		- connection finished: connectionFinished_handler
 * 		- message received from client: messageReceived_handler
 --------------------------------------------------------------------------------------- */
abstract class EventHandler {
	public void connectionStablished_handler(Socket s) {
		System.out.println("New connection stablished!");
	}
	
	public void connectionFinished_handler(Socket s) {
		System.out.println("Connection finished!");
	}
		
	public void messageReceived_handler(Socket s, String message) {
		System.out.println("Message received: " + message);
	}
}

/* ---------------------------------------------------------------------------------------
 * EchoEventHandler:
 * 	Concrete EventHandler interface implementation
 *   
 * 	Here are all the event handlers: 
 * 		- connectionStablished_handler: we do not override this 
 * 		- connectionFinished_handler: we do not override this
 * 		- messageReceived_handler: we override this, and simply sends back the message received to the client
 --------------------------------------------------------------------------------------- */

class EchoEventHandler extends EventHandler {
	// -----------------------------------------------------------------------------------
	@Override
	public void messageReceived_handler(Socket s, String message) {
		try {
			DataOutputStream out = new DataOutputStream(s.getOutputStream());
			out.writeUTF(message);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	// -----------------------------------------------------------------------------------

}


/* ---------------------------------------------------------------------------------------
 * Reactor:
 * 	This class is responsible of reporting all the events that occur in the server
 *  Reactor class will need a concrete EventHandler implementation to call
 *  its hooks methods when any of these events occur 
 *   
 * 	Here are all the possible events are: 
 * 		- new connection stablished: connectionStablished_handler
 * 		- connection finished: connectionFinished_handler
 * 		- message received from client: messageReceived_handler
 --------------------------------------------------------------------------------------- */

class Reactor extends Thread {
	ServerSocket server_socket;
	Socket client_socket;
	EventHandler event_handler;
	
	// -----------------------------------------------------------------------------------
	public Reactor(Socket s, EventHandler event_handler) {
		this.client_socket = s;
		this.event_handler = event_handler;
		this.start();
	}
	
	// -----------------------------------------------------------------------------------
	@Override
	public void run() {
		event_handler.connectionStablished_handler(client_socket);
	    try {
		    while (true) {
				DataInputStream in = new DataInputStream(client_socket.getInputStream());
				String message = in.readUTF();

				event_handler.messageReceived_handler(client_socket, message);
				if (message.equals("exit")) {
					break;
				}
		    }
		    event_handler.connectionFinished_handler(client_socket);
		    client_socket.close();
	    }
	    catch (Exception e) {
	    	System.out.println("Reactor.java: run()");
	    	e.printStackTrace();
	    }
	    	
	}
	// -----------------------------------------------------------------------------------
}

/* ---------------------------------------------------------------------------------------
 * ServerFacade:
 * 	A single class wich makes it easy to implement a socket server
 *  exposes out a run method
 *  The user has to set a concrete EventHandler class when creating a new instance of it
--------------------------------------------------------------------------------------- */
class ServerFacade {
	private int port_number;
	private ServerSocket server_socket;
	private EventHandler event_handler;
	
	// -----------------------------------------------------------------------------------
	public ServerFacade(EventHandler event_handler) {
		this(event_handler, 5000);
	}
	// -----------------------------------------------------------------------------------
	public ServerFacade(EventHandler event_handler, int port_number) {
		try {
			this.port_number = port_number;
			this.event_handler = event_handler;
			server_socket = new ServerSocket(this.port_number);
			System.out.println("Listening to port number " + this.port_number + "...");
		} catch( Exception e ) {
	    	System.out.println("Server.java: Server()");
			System.out.println( e.getMessage() );
		}
	}
	// -----------------------------------------------------------------------------------
	public void run() {
		try {
			while (true) {
				System.out.println("Waiting for new connection...");
				new Reactor(server_socket.accept(), this.event_handler);
			}
		}
		catch (Exception e) {
	    	System.out.println("Server.java: run()");
			e.printStackTrace();
		}
	}
	// -----------------------------------------------------------------------------------
}

/* ---------------------------------------------------------------------------------------
 * EchoServer:
 * 	Code for the Server
 * 	By using the ServerFacade it is really easy to make it work:
 *  1. You just have to pass your own concrete EventHandler class when you create a new instance of it
 *  2. Call its run method and everything works 
 *  3. Capable of working with multiple clients with no programming effort, because it is all resolved in 
 *  	ServerFacade
 --------------------------------------------------------------------------------------- */

public class EchoServer {
	public static void main( String[] args ) {
		int port_number = 5000;
		
		switch (args.length) {
		case 0:
			break;
		case 1:
			port_number = Integer.valueOf(args[0]);
			break;
		default:
			System.out.println("How to use:");
			System.out.println("\tjava EchoServer <port number>");
			System.out.println("\t\tdefault values are:");
			System.out.println("\t\tport: 5000");
			break;
		}
		
		EchoEventHandler event_handler = new EchoEventHandler();
		ServerFacade server = new ServerFacade(event_handler, port_number);
		server.run();
	}
}
