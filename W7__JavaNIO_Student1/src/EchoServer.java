

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * An echo server using blocking IO. This server will serve several clients in
 * parallel. However, due to the blocking IO, it will use as many threads as
 * there are clients running in parallel. And due to the HalfSync/HalfAsync
 * pattern, it will also use one thread for each request. In a real server, we
 * would use NIO, but NIO already implements the Acceptor pattern, so it is less
 * interesting. (By the way, we should better use Netty, but there would be near
 * to nothing to do!)
 */
public class EchoServer {

  /**
   * The port to listen to is configurable here.
   */
  private static final int DEFAULT_PORT = 1234;

  /**
   * The main method, starting the serverSocketChannel.
   * 
   * @param args
   *          the array of parameters. The first parameter, if present, is the
   *          port. Otherwise, we use the default port above.
   * @throws IOException
   *           There is no exception handling in this example, so all exceptions
   *           are only printed to the console.
   */
  public static void main(String[] args) throws IOException {
    int port = DEFAULT_PORT;
    if (args.length > 0) {
      try {
        port = Integer.parseInt(args[0]);
      } catch (final NumberFormatException e) {
        System.out.println("Error parsing parameter " + args[0] + " for port number.");
        System.out.println("Please use a valid port number, or no argument to use the default port " + DEFAULT_PORT);
        return;
      }
    }
    System.out.println("Starting the ECHO server. Type Ctrl + C to stop the server.");
    /*
     * We do not use an explicit WrapperFacade because the Java ServerSocket
     * class is a WrapperFacade around the socket implementation details.
     */
    final Reactor reactor = new Reactor(new ServerSocket(port));
    reactor.startEventLoop();
  }

  /**
   * The reactor is minimal. It will block on the serverSocket.accept() method
   * until an incoming connection from a client arrives.
   */
  private static class Reactor {

    /**
     * The thread pool used to handle incoming connections.
     */
    ExecutorService service = Executors.newCachedThreadPool();

    /**
     * We store the serverSocketChannel socketChannel. We could have store the
     * wrapper facade instead.
     */
    private final ServerSocket serverSocket;

    /**
     * The constructor.
     * 
     * @param wrapperFacade
     *          The wrapper facade allowing to access the serverSocketChannel
     *          socketChannel.
     * @throws IOException
     *           We re throw all exceptions to simplify the code.
     */
    public Reactor(ServerSocket serverSocket) throws IOException {
      super();
      this.serverSocket = serverSocket;
    }

    /**
     * This method is the event loop of the reactor. It implements the HALF SYNC
     * part of the HalfSync/HalfAsync pattern. Instead of running the acceptor
     * in a fully synchronous way, it just starts the Acceptor thread and then
     * loop back to wait for a new incoming connection.
     * <p>
     * It is important to note that this is only a demo program implementing the
     * HalfSync/HalfAsync pattern. For production code, we would use the NIO or
     * NIO2 implementations of Java which both implements the pattern.
     * <p>
     * To work around the limitation of the "one thread  per connection" model,
     * we use a thread pool to handle incoming connections.
     */
    public void startEventLoop() {
      try {
        while (!Thread.interrupted()) {
          final Acceptor acceptor = new Acceptor(this.serverSocket.accept());
          service.submit(acceptor);
        }
      } catch (final IOException e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * The acceptor. In this implementation, the thread running the Acceptor is
   * "borrowed" to run the event handler. It would be very easy to start a new
   * thread.
   */
  private static class Acceptor implements Runnable {

    /**
     * The client socketChannel. Unlike the serverSocketChannel socketChannel,
     * which last for the life of the serverSocketChannel, the
     * serverSocketChannel socketChannel uses a randomly chosen available port
     * and last for the life of the client connection.
     */
    private final Socket socket;

    /**
     * The constructor, storing the client socketChannel.
     * 
     * @param socketChannel
     */
    public Acceptor(Socket socket) {
      super();
      this.socket = socket;
    }

    /**
     * The Acceptor thread will stop when the processEvent method of the created
     * EchoServerHandler will return, ending the connection with a specific
     * client. This will not However stop the serverSocketChannel which will
     * continue serving other clients or waiting for incoming new ones.
     */
    @Override
    public void run() {
      try {
        new EchoServerHandler(this.socket).processEvents();
      } catch (final IOException e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * The event handler. The processEvent method is the HalfSync part of the
   * HalfSync/HalfAsync pattern.
   */
  private static class EchoServerHandler {

    /**
     * Store the socketChannel, allowing to close it when we are finished with
     * the client connection.
     */
    private final Socket socket;

    /**
     * The writer allowing to echo the messages to the client.
     */
    private final PrintWriter writer;

    /**
     * The reader allowing to read the messages from the client.
     */
    private final BufferedReader reader;

    /**
     * A flag that will be set upon reception of a special message ("end") from
     * the client. This is not necessary, because we could just wait for the
     * client to close the client socketChannel and handle the corresponding
     * exception.
     */
    private boolean terminationRequested;

    /**
     * Construct the handler.
     */
    public EchoServerHandler(Socket socket) throws IOException {
      this.socket = socket;
      this.reader = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
      this.writer = new PrintWriter(socket.getOutputStream());
      System.out.println("Server listening to client events on port: " + socket.getPort());
    }

    /**
     * This is the HalSync part of the HalfSync/HalfAsync pattern. Instead of
     * processing an event before reading a new one, this method will create a
     * new task for each incoming event and submit the task to a pool for
     * asynchronous execution (the HalfAsync part of the pattern).
     */
    public void processEvents() {
      try {
        String inputLine;
        while (!this.terminationRequested && (inputLine = this.reader.readLine()) != null) {
          if ("end".equals(inputLine)) {
            this.terminationRequested = true;
          } else {
            /*
             * Print a message in the server console to show which thread is doing what.
             */
            System.out.println("Event caught in thread " + Thread.currentThread().getName());
            new Thread(new HalfAsyncTask(this.writer, inputLine)).start();
          }
        }
        System.out.println("Server closing client socketChannel on port: " + this.socket.getPort());
        this.reader.close();
        this.writer.close();
        this.socket.close();
      } catch (final IOException e) {
        throw new IllegalStateException("Exception in handler event loop", e);
      }
    }
  }

  /**
   * The Async part of the HalfSync/HalfAsync pattern. In a so minimal example,
   * it make no much sense, but this is the principle. If the task was
   * potentially long running, it would not block the EventHandler thread.
   */
  private static class HalfAsyncTask implements Runnable {

    /**
     * The writer allowing to echo the messages to the client.
     */
    private final PrintWriter writer;
    private final String message;

    /**
     * The constructor
     */
    public HalfAsyncTask(PrintWriter writer, String message) {
      super();
      this.message = message;
      this.writer = writer;
    }

    /**
     * The method doing the Job. It will also print a log message in the server
     * console to show which thread is doing the job.
     */
    @Override
    public void run() {
      System.out.println("Event processed in thread " + Thread.currentThread().getName());
      this.writer.println(this.message);
      this.writer.flush();
    }

  }

}