import java.io.*;
import java.net.*;
public class Cliente {
	static final int PUERTO=5000;
	static final String HOST = "localhost";
	
	public Cliente( ) {
		try{
			Socket skCliente = new Socket( HOST , PUERTO);
			System.out.println("Conexión establecida con el servidor");
			DataOutputStream out = new DataOutputStream(skCliente.getOutputStream());
			DataInputStream in = new DataInputStream(skCliente.getInputStream());
			
			String userInput;
			BufferedReader stdIn = new BufferedReader(
                    new InputStreamReader(System.in));
		
			while (true) {
				userInput = stdIn.readLine();
			    out.writeUTF(userInput);
			    String serverMessage = in.readUTF();
			    System.out.println(serverMessage);
				if (serverMessage.equals("exit"))
					break;
			}
		
			skCliente.close();
		} catch( Exception e ) {
			System.out.println( e.getMessage() );
		}
	}
	public static void main( String[] arg ) {
		new Cliente();
	}
}