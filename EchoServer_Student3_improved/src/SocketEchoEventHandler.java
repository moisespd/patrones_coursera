import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SocketChannel;

// echo socket event handler
public final class SocketEchoEventHandler extends EventHandler {

	private final BufferedReader reader;
	private final SocketChannel channel;
	
	public SocketEchoEventHandler(SocketChannel channel) throws IOException {
		if(channel == null) {
			throw new NullPointerException();
		}
		this.channel = channel;
		this.reader = new BufferedReader(new InputStreamReader(this.channel.socket().getInputStream()));
	}
	
	@Override
	public int handleEvent() {
		try {
			String line = this.reader.readLine();
			if(!this.channel.isConnected() || line == null) {
				// connection has been close
				// so send code to remove handler from reactor
				return -1;
			}
			System.out.printf("Client %s: %s\n", this.channel.socket().getInetAddress().toString(), line);
			return 0;
		} catch (IOException e) {
			// Error occurred
			// log an exception (use stdout as a log) and send code to remove handler from reactor
			e.printStackTrace();
			return -1;
		}
	}

	@Override
	public SelectableChannel getSelectableChannel() {
		return this.channel;
	}
	
	@Override
	public void close() throws IOException {
		System.out.printf("Event > Client connection %s has been closed.\n", this.channel.socket().getInetAddress().toString());
		try {
			super.close();
		} finally {
			this.channel.close();
		}
	}
}
