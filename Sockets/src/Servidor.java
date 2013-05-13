import java.io.* ;
import java.net.* ;
class Servidor {
	static final int PUERTO=5000;
	ServerSocket skServidor;
	
	private void esperarPorClientes() {
		while (true) {
			try {
				Socket skCliente = skServidor.accept(); // Crea objeto
				System.out.println("Entra un nuevo cliente");
				DataInputStream in = new DataInputStream(skCliente.getInputStream());
				System.out.println(in.readUTF());
				System.out.println("Sencillamente cerramos su conexión");
				skCliente.close();
			} 
			catch( Exception e ) {
				System.out.println( e.getMessage() );
			}
		}
	}
	
	private void atenderCliente() {
	}
	
	public Servidor( ) {
		try {
			skServidor = new ServerSocket(PUERTO);
			System.out.println("Escucho el puerto " + PUERTO );
		} catch( Exception e ) {
			System.out.println( e.getMessage() );
		}
	}
	public static void main( String[] arg ) {
		Servidor servidor = new Servidor();
		servidor.esperarPorClientes();
	}
}