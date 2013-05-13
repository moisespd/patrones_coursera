import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class MOI_Acceptor extends MOI_EventHandler {

	public MOI_Acceptor() {
		super(EventType.EventTypes.NewConnectionRequest);
	}
	
	@Override
	public void handleEvent(MOI_Reactor reactor, SelectionKey key) throws IOException {
		super.handleEvent(reactor, key);
		
		SocketChannel client = reactor.getServer().accept(); 
		client.configureBlocking(false); 
		client.socket().setTcpNoDelay(true);
		SelectionKey clientKey = client.register(reactor.getSelector(), SelectionKey.OP_READ);
//		connection(clientKey);
		System.out.println("Un cliente que se quiere conectar");
		
	}
	

}
