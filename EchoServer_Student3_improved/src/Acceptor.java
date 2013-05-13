import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.SelectableChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public final class Acceptor extends EventHandler {

	private final ServerSocketChannel listener;
	
	public Acceptor(SocketAddress endpoint) throws IOException {
		if(endpoint == null) {
			throw new NullPointerException();
		}
		listener = ServerSocketChannel.open();
		// Java SocketChannels required use non-blocking sockets with select
		// also neither POSIX nor Windows required that
		listener.configureBlocking(false);
		listener.socket().bind(endpoint);
	}
	
	@Override
	public int handleEvent() {
		try {
			SocketChannel channel = listener.accept();
			// Java SocketChannels required use non-blocking sockets with select
			// also neither POSIX nor Windows required that
			channel.configureBlocking(false);
			SocketEchoEventHandler handler = new SocketEchoEventHandler(channel);
			handler.register(this.getReactor(), EventTypes.incoming);
			System.out.printf("Event > Client %s has been connected.\n", channel.socket().getInetAddress().toString());
			
			return 0;
		} catch(IllegalStateException ise) {
			System.out.println("Event > Error occured during handle connection.");
			// TODO Auto-generated catch block
			ise.printStackTrace();
			return 0;
		} catch (IOException e) {
			System.out.println("Event > Error occured during connection setup.");
			// TODO Auto-generated catch block
			e.printStackTrace();
			return -1;
		}
	}

	@Override
	public SelectableChannel getSelectableChannel() {
		return this.listener;
	}
	
	@Override
	public void close() throws IOException {
		try {
			super.close();
		} finally {
			this.listener.close();
		}
	}
}
