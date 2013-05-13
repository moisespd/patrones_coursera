import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Server for Programming Assignment #3 of
 * "Pattern-Oriented Software Architectures for Concurrent and Networked Software"
 * by Douglas C. Schmidt.
 */
public class Program {

  /**
   * The reactor from the pattern of the same name. Is instantiated with a port
   * to listen on and an {@link Acceptor} to which to delegate accepting socket
   * connections by calling {@link Acceptor#handleInput(ServerSocket)}.
   * 
   * Uses the {@link ServerSocket} Java-native wrapper facade under the covers.
   */
  static class Reactor {
    private final ServerSocket ssock;
    private final Acceptor acc;

    public Reactor(int port, Acceptor acceptor) throws IOException {
      this.acc = acceptor;
      this.ssock = new ServerSocket(port);
    }

    /**
     * Run the event loop for this {@link Reactor}, listening on the given port
     * and delegating connection acceptance to the given {@link Acceptor}. Never
     * returns.
     */
    public void runEventLoop() throws IOException {
      while (true) {
        acc.handleInput(ssock);
      }
    }
  }

  /**
   * The acceptor from the pattern of the same name.
   * 
   * In {@link #handleInput(ServerSocket)} accepts incoming socket accept events
   * via the Java-native wrapper facade, then delegates to a {@link SvcHandler}
   * for data transfer aver that socket.
   * 
   * Implements {@link #handleInput(ServerSocket)} as a template method, calling
   * out to {@link #makeSvcHandler()} to instantiate the {@link SvcHandler}
   * instance, {@link #acceptSvcHandler(SvcHandler, Socket)} to pass a newly
   * established {@link Socket} to a {@link SvcHandler}, and
   * {@link #activateSvcHandler(SvcHandler)} to instruct the {@link SvcHandler}
   * to transfer data over that socket. All of these methods can be overridden
   * by sub-classes.
   */
  static class Acceptor {
    /**
     * Accept incoming socket events from the given {@link ServerSocket} and
     * delegate processing over the newly accepted {@link Socket} to a
     * {@link SvcHandler} by calling, in that order, {@link #makeSvcHandler()},
     * {@link #acceptSvcHandler(SvcHandler, Socket)} and
     * {@link #activateSvcHandler(SvcHandler)}.
     */
    public void handleInput(ServerSocket ssock) throws IOException {
      final Socket sock = ssock.accept();
      final SvcHandler handler = makeSvcHandler();
      acceptSvcHandler(handler, sock);
      activateSvcHandler(handler);
    }

    /**
     * Instantiate a concrete {@link EchoSvcHandler}, can be overridden by
     * sub-classes.
     */
    protected SvcHandler makeSvcHandler() throws IOException {
      return new EchoSvcHandler();
    }

    protected void acceptSvcHandler(SvcHandler handler, Socket socket) {
      handler.peer(socket);
    }

    protected void activateSvcHandler(SvcHandler handler) throws IOException {
      handler.open();
      try {
        handler.handleInput();
      } finally {
        handler.handleClose();
      }
    }
  }

  /**
   * A base class for service handlers from the pattern of the same name.
   * Sub-classes are instantiated and invoked by the {@link Acceptor} to
   * transfer data over a {@link Socket}.
   */
  abstract static class SvcHandler {
    protected Socket sock;

    /**
     * Make this {@link SvcHandler} accept the given {@link Socket} for
     * communication with the peer.
     */
    public void peer(Socket socket) {
      this.sock = socket;
    }

    /**
     * Open data transfer streams over the {@link Socket} previously passed to
     * {@link #peer(Socket)}.
     */
    abstract public void open() throws IOException;

    /**
     * Handle input from the peer after first {@link #peer(Socket)} and then
     * {@link #open()} have been called.
     */
    abstract public void handleInput() throws IOException;

    /**
     * Close all handles for communication with the peers. Called after
     * {@link #handleInput()}.
     */
    abstract public void handleClose() throws IOException;
  }

  /**
   * A {@link SvcHandler} that implements {@link #handleInput()} by echoing the
   * data received over the {@link Socket} to the console. Doesn't write to the
   * peer so only opens an {@link InputStream} over the {@link Socket}.
   */
  static class EchoSvcHandler extends SvcHandler {
    private BufferedReader in;

    /**
     * Open an {@link InputStream} for reading from the peer and wrap it in a
     * {@link BufferedReader} so that input can be read line-by-line.
     */
    public void open() throws IOException {
      try {
        this.in = new BufferedReader(new InputStreamReader(
            sock.getInputStream(), "UTF-8"));
      } catch (UnsupportedEncodingException e) {
        // can't happen because UTF-8 is a required charset
        throw new IllegalStateException(e);
      }
    }

    /**
     * Echo data received from the peer to the console in a line-by-line
     * fashion.
     */
    public void handleInput() throws IOException {
      // read and echo one line at a time, using a BufferedReader reading from
      // the socket
      String line;
      while ((line = in.readLine()) != null) {
        System.out.println(line);
      }
    }

    /**
     * Close the {@link BufferedReader} over the {@link InputStream} from the
     * peer.
     */
    public void handleClose() throws IOException {
      in.close();
    }
  }

  private static final int DEFAULT_PORT = 2345;

  /**
   * Program main, accepts integer port number as optional parameter (defaulting
   * to port 2345). Instantiates {@link Acceptor}, then instantiates
   * {@link Reactor} for that {@link Acceptor} and the given port, then runs the
   * {@link Reactor}'s event loop, which never returns.
   */
  public static void main(String[] args) throws IOException {
    final int port = args.length == 0 ? DEFAULT_PORT : Integer
        .parseInt(args[0]);
    System.err.println("Listening on port " + port + " (CTRL-C to end)");

    final Acceptor acceptor = new Acceptor();
    final Reactor reactor = new Reactor(port, acceptor);
    reactor.runEventLoop();
  }
}