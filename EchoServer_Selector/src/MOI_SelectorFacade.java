import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;


public class MOI_SelectorFacade {
	private static final int BUFFER_SIZE = 1024;
	private static final int BUFFER_HEADER_SIZE = 2;

	private Selector selector;
	
	public ServerSocketChannel open_and_init(int port) throws IOException {
		ServerSocketChannel server = null;
		
		selector = Selector.open();
		server = ServerSocketChannel.open(); 
		server.socket().bind(new InetSocketAddress(port)); 
		server.configureBlocking(false); 
		server.register(selector, SelectionKey.OP_ACCEPT);

		return server;
	}
	
	public void select(long timeout) throws IOException {
		selector.select(timeout);	
	}
	
	private long bytesToLength(byte[] bytes) {
		if (bytes.length != BUFFER_HEADER_SIZE) {
			throw new IllegalStateException("Wrong number of bytes, must be " + BUFFER_HEADER_SIZE);
		}
		return ((long)(bytes[0] & 0xff) << 8) + (long)(bytes[1] & 0xff);
	}

    private String bb_to_str(ByteBuffer buffer){
    	Charset charset = Charset.forName("UTF-8");
    	CharsetDecoder decoder = charset.newDecoder();
    	
    	String data = "";
    	try{
    		int old_position = buffer.position();
    		data = decoder.decode(buffer).toString();
    		// reset buffer's position to its original so it is not altered:
    		buffer.position(old_position);  
    	}catch (Exception e){
    		e.printStackTrace();
    		return "";
    	}
    	return data;
    }    

    private String readIncomingMessage(SelectionKey key) throws IOException {
    	SocketChannel cliente = (SocketChannel)key.channel();
    	String message;
    	
    	ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
    	buffer.clear();
    	
    	if (cliente.read(buffer) == -1) {
    		throw new IOException("Read on closed key");
    	}
    	
    	buffer.flip(); 
    	
    	ByteBuffer msg = readMessage(key, buffer);
    	message = bb_to_str(msg);

    	return message;
    }

    private ByteBuffer readMessage(SelectionKey key, ByteBuffer readBuffer) {
    	int bytesToRead; 
    	
    	if (readBuffer.remaining() > BUFFER_HEADER_SIZE) { // must have at least enough bytes to read the size of the message    		
     		byte[] lengthBytes = new byte[BUFFER_HEADER_SIZE];
    		readBuffer.get(lengthBytes);
			bytesToRead = (int)bytesToLength(lengthBytes);
    		if ((readBuffer.limit() - readBuffer.position()) < bytesToRead) { 
    			// Not enough data - prepare for writing again 
    			if (readBuffer.limit() == readBuffer.capacity()) {
    	    		// message may be longer than buffer => resize buffer to message size
    				int oldCapacity = readBuffer.capacity();
    				ByteBuffer tmp = ByteBuffer.allocate(bytesToRead + BUFFER_HEADER_SIZE);
    				readBuffer.position(0);
    				tmp.put(readBuffer);
    				readBuffer = tmp;   				
    				readBuffer.position(oldCapacity); 
	    			readBuffer.limit(readBuffer.capacity()); 
    	
    	    		return null;
    	    	} else {
    	    		// rest for writing
	    			readBuffer.position(readBuffer.limit()); 
	    			readBuffer.limit(readBuffer.capacity()); 
	    			return null; 
    	    	}
    		} 
    	} else { 
    		// Not enough data - prepare for writing again 
    		readBuffer.position(readBuffer.limit()); 
    		readBuffer.limit(readBuffer.capacity()); 
    		return null; 
    	} 
    	
    	byte[] resultMessage = new byte[bytesToRead];
    	readBuffer.get(resultMessage, 0, bytesToRead); 
    	// remove read message from buffer
    	int remaining = readBuffer.remaining();
    	readBuffer.limit(readBuffer.capacity());
    	readBuffer.compact();
    	readBuffer.position(0);
    	readBuffer.limit(remaining);
    	
    	return ByteBuffer.wrap(resultMessage);
    } 

}
