import java.net.Socket;

public class MyAcceptor extends AcceptorBase {

	@Override
	public void stablishConnection(Socket client) {
		System.out.println("New connection stablished");
	}
}
