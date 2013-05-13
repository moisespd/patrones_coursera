import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;


public class MOI_Reactor {
	private ArrayList<MOI_EventHandler> handlers = new ArrayList<MOI_EventHandler>();
	private Selector selector = null;
	private ServerSocketChannel server = null;
	private int port = 8080;
	private MOI_Acceptor myAcceptor = null;

	public MOI_Reactor(int port, MOI_Acceptor acceptor) {
		this.port = port;
		this.myAcceptor = acceptor;
	}

	public Selector getSelector() {
		return this.selector;
	}
	
	public ServerSocketChannel getServer() {
		return this.server;
	}
	
	public void addHandler(MOI_EventHandler handler) {
		handlers.add(handler);
	}
	
	public void wait4events() {
		try {
			selector = Selector.open(); 
			server = ServerSocketChannel.open(); 
			server.socket().bind(new InetSocketAddress(port)); 
			server.configureBlocking(false); 
			server.register(selector, SelectionKey.OP_ACCEPT);
			System.out.println("Server started on port number " + this.port);
			
			while (true) {
    			selector.select(0);

    			for (Iterator<SelectionKey> i = selector.selectedKeys().iterator(); i.hasNext();) { 
    				SelectionKey key = i.next(); 
    				try { 
    					i.remove(); 
    					if (key.isConnectable()) { 
//    						((SocketChannel)key.channel()).finishConnect();
    						System.out.println("isConnectable!!!");
    					} 
    					if (key.isAcceptable()) {
    						myAcceptor.handleEvent(this, key);
    					} 
    					if (key.isReadable()) {
//    						System.out.println("Mensaje recibido: " + readIncomingMessage(key));
    						System.out.println("Un cliente que envía un mensaje");
						} 
    				} 
    				catch (Exception ioe) {
    					ioe.printStackTrace();
//    					resetKey(key);
//    					disconnected(key); 
    				} 
    			} 
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
