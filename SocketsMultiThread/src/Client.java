/* ---------------------------------------------------------------------------------------
 * Client:
 * Simple client to comunicate with the server.
 * Whatever you write, is received by the server and echoed back to this client, which 
 * prints it on screen
 * 
 *  Type exit to finish the client
 --------------------------------------------------------------------------------------- */

import java.io.*;
import java.net.*;

public class Client {
	private int port_number;
	private String server_name;

	DataInputStream in;
	DataOutputStream out;
	Socket client_socket;
	
	// -----------------------------------------------------------------------------------
	public Client() {
		this("localhost", 5000);
	}
	// -----------------------------------------------------------------------------------
	public Client(String server_name) {
		this(server_name, 5000);
	}
	// -----------------------------------------------------------------------------------ç
	public Client(int port_number) {
		this("localhost", port_number);
	}
	// -----------------------------------------------------------------------------------
	public Client(String server_name, int port_number) {
		try {
			this.port_number = port_number;
			this.server_name = server_name;
			
			client_socket = new Socket(this.server_name , this.port_number);
			out = new DataOutputStream(client_socket.getOutputStream());
			in = new DataInputStream(client_socket.getInputStream());
			
		} catch( Exception e ) {
			System.out.println( e.getMessage() );
		}
	}
	// -----------------------------------------------------------------------------------
	public void start() {
		String userInput;
		BufferedReader stdIn = new BufferedReader(
                new InputStreamReader(System.in));

		try {
			while (true) {
				System.out.print("Enter your command (type exit to finish): ");
				userInput = stdIn.readLine();
			    out.writeUTF(userInput);
				String serverMessage = in.readUTF(); 
				System.out.println("echo: " + serverMessage);
				if (userInput.equals("exit"))
					break;
			}
	
			client_socket.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	// -----------------------------------------------------------------------------------
	public static void main( String[] args ) {
		int port_number = 5000;
		String host_name = "localhost";
		
		switch (args.length) {
		case 0:
			break;
		case 1:
			host_name = args[0];
			break;
		case 2:
			host_name = args[0];
			port_number = Integer.valueOf(args[1]);
			break;
		default:
			System.out.println("How to use:");
			System.out.println("\tjava Client <host> <port number>");
			System.out.println("\t\tdefault values are:");
			System.out.println("\t\thost: localhost");
			System.out.println("\t\tport: 5000");
			break;
		}
		Client client = new Client(host_name, port_number);
		client.start();
	}
}