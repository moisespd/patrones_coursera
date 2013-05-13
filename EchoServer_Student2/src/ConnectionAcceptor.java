import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * Handles new client connections.
 * Plays role of Acceptor
 * */

 public class ConnectionAcceptor {
    protected Selector _selector;
    protected ServerSocketChannel _ssChannel;
//    protected ThreadPool _pool;

    /**
     * Creates a new ConnectionAcceptor
     * @param selector the Selector to which new SocketChannels will be registered
     * @param ssChannel the ServerSocketChannel which can accept new connections
     * @param pool the thread pool, which is needed by the new ConnectionReaders
     */
    public ConnectionAcceptor(Selector selector, ServerSocketChannel ssChannel) {
        _selector = selector;
        _ssChannel = ssChannel;
//        _pool = pool;
    }

    /**
     * Accepts a connection:
     * <UL>
     * <LI>Creates a SocketChannel for it
     * <LI>Creates a ConnectionReader for it
     * <LI>Registers the SocketChannel and the ConnectionReader to the Selector
     * </UL>
     * @throws IOException in case of an IOException during the acceptance of a new connection
     */
    public void accept() throws IOException {
        // Get a new channel for the connection request
        SocketChannel sChannel = _ssChannel.accept();

        // If serverSocketChannel is non-blocking, sChannel may be null
        if (sChannel != null) {
            SocketAddress address = sChannel.socket().getRemoteSocketAddress();
            sChannel.configureBlocking(false);
            sChannel.register(_selector, SelectionKey.OP_READ, new ConnectionReader(sChannel));
        }
    }
}
