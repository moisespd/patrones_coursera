import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * Handles messages from clients
 * creates Connector : plays role of connector in acceptor-connector pattern
 */
 
 class ConnectionReader {
    public static final int BUFFER_SIZE = 256;
    public static final char MESSAGE_END = ';';

    protected SocketChannel _sChannel;
    protected String _incomingData;
//    protected ThreadPool _pool;

    /**
     * Creates a new ConnectionReader object
     * @param sChannel the SocketChannel of the client
     * @param pool the ThreadPool to which new Tasks should be inserted
     */
    public ConnectionReader(SocketChannel sChannel) {
        _sChannel = sChannel;
//        _pool = pool;
        _incomingData = "";
    }

    /**
     * Reads messages from the client:
     * <UL>
     * <LI>Reads the entire SocketChannel's buffer
     * <LI>Separate the information into messges
     * <LI>For each message:
     * <UL>Creates a Task for the message
     * <LI>Inserts the Task to the ThreadPool
     * </UL>
     * </UL>
     * @throws IOException in case of an IOException during reading
     */
    public void read() throws IOException {
    	
        //ByteBuffer buf = ByteBuffer.allocateDirect(BUFFER_SIZE);
        ByteBuffer buf = ByteBuffer.allocate(BUFFER_SIZE);

        // Read the entire content of the socket
        while (true) {
            buf.clear();
            int numBytesRead = _sChannel.read(buf);

            // Closed channel
            if (numBytesRead == -1) {
                // No more bytes can be read from the channel
                _sChannel.close();
                break;
            }

            // Read the buffer
            if (numBytesRead > 0) {
                //read the data
                buf.flip();
                String str = new String(buf.array(), 0, numBytesRead);
                _incomingData = _incomingData + str;
            }

            //end of message
            if (numBytesRead < BUFFER_SIZE) {
                break;
            }
        }

        if(_sChannel.isOpen()){
              _sChannel.write(buf);

        }
    }
}