import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.concurrent.ConcurrentLinkedQueue;
/*
 * Define the event types that the EchoServer will handle
 */
enum EventType { ACCEPT, READ }

/**
 * Simple reactor interface to register handlers and runEventLoop
 * Has no unregister/remove for event handlers for this specific assigment
 */
interface IReactor {
    void runEventLoop() throws IOException;

    void registerHandler(EventType eventType, ServiceHandler eventHandler);
    void unregisterHandler(EventType eventType, ServiceHandler eventHandler);
}

class ServiceHandlerThread extends Thread {
    private ServiceHandler sh = null;
    public ServiceHandlerThread(ServiceHandler sh) {
        this.sh = sh;
    }
    
    public ServiceHandler getServiceHandler() {
        return this.sh;
    }
    
    @Override
    public void run() {
        super.run(); //To change body of generated methods, choose Tools | Templates.
        
        if(sh != null) { 
            try { while (!sh.handleEvent()); }
            catch(Exception e) { System.out.println(e); }
            finally { 
                try { sh.close(); }
                catch(Exception e) { System.out.println("Cannot close Service Handler!"); }
            }
        }
    }
}

class ConcreteReactor implements IReactor {

    private final EnumMap<EventType, ArrayList<ServiceHandler>> handlers = new EnumMap<>(EventType.class);
    private ArrayList<ServiceHandlerThread> eventHandlerThreadPool = new ArrayList<ServiceHandlerThread>();
    
    @Override
    public void runEventLoop() throws IOException {
        while (true) {
            handleEvent(EventType.ACCEPT);
            handleEvent(EventType.READ);
        }
    }

    private void handleEvent(EventType eventType) throws IOException {
        ArrayList<ServiceHandler> eventHandlers = handlers.get(eventType);
        if (eventHandlers == null)
            return;
        
        for(ServiceHandler sh : eventHandlers) {
            Boolean found = false;
            for(ServiceHandlerThread sht : eventHandlerThreadPool) {
                if(sht.getServiceHandler() == sh) {
                    found = true;
                    break;
                }
            }
            if(!found) {
                ServiceHandlerThread sht = new ServiceHandlerThread(sh);
                eventHandlerThreadPool.add(sht);
                sht.start();
            }
        }
    }

    @Override
    public void registerHandler(EventType eventType, ServiceHandler eventHandler) {
        ArrayList<ServiceHandler> oldEventHandlers = handlers.get(eventType);
        if (oldEventHandlers == null)
            oldEventHandlers = new ArrayList<ServiceHandler>();
        oldEventHandlers.add(eventHandler);
        
        handlers.put(eventType, oldEventHandlers);
    }
    
    @Override
    public void unregisterHandler(EventType eventType, ServiceHandler eventHandler) {
        ArrayList<ServiceHandler> oldEventHandlers = handlers.get(eventType);
        if (oldEventHandlers == null)
            return;
        
        oldEventHandlers.remove(eventHandler);

        try { eventHandler.close(); }
        catch(Exception e) { System.out.println("Cannot close ServiceHandler"); }
        
        handlers.put(eventType, oldEventHandlers);
    }    
}



/**
 * Class that handles event.
 */
abstract class ServiceHandler {
    private IReactor reactor;
    private final EventType eventType;

    protected ServiceHandler(EventType eventType) {
        this.eventType = eventType;
    }

    public void open(IReactor reactor) throws IOException {
        assert reactor != null;
        this.reactor = reactor;
        this.reactor.registerHandler(eventType, this);
    }

    public abstract void close() throws IOException;

    /**
     * Handle event of {@linkplain #eventType} and returns true if handling is completed. If false is returned, this
     * method must be invoked again.
     *
     * @return true if handling event is finished, false otherwise.
     * @throws IOException when it is impossible to handle event
     */
    public abstract boolean handleEvent() throws IOException;

    protected final IReactor getReactor() {
        return reactor;
    }
}
/**
 * Accepts clients on specified port or default 8080
 */
class ConcreteAcceptor extends ServiceHandler {
    private ServerSocket serverSocket;
    private int port = 8080;

    public ConcreteAcceptor() {
        super(EventType.ACCEPT);
    }

    @Override
    public boolean handleEvent() throws IOException {
        Socket socket = serverSocket.accept();
        final EchoServerHandler echoServerHandler = new EchoServerHandler(new StreamWrapperFacade(socket));
        echoServerHandler.open(getReactor());
        System.out.println("Client connected!");
        return false;
    }

    @Override
    public void open(IReactor reactor) throws IOException {
        super.open(reactor);
        serverSocket = new ServerSocket(port);
    }

    @Override
    public void close() throws IOException {
        // does nothing, server socket is closed on process exit, accepted sockets must be closed by other handlers
    }
    
    //setter for socket port
    public void setPort(int port) {
        this.port = port;
    }
}


/*
 * Encapsulation of data for the task
 * + What message to process
 * + Where to send the echo
 */
class TaskInfo {
    public TaskInfo(StreamWrapperFacade stream, String message) {
        this.stream = stream;
        this.message = message;
    }
    
    private String message; 
    private StreamWrapperFacade stream;

    public StreamWrapperFacade getStream() {
        return stream;
    }
    public String getMessage() {
        return message;
    }
}

class EchoTask extends Thread {
    
    private void trytosleep() {
        try { Thread.sleep(1); }
        catch(Exception e) { System.out.println("Cannot sleep" + this.getName()); }
    }
    
    @Override
    public void run() {
        super.run(); //To change body of generated methods, choose Tools | Templates.
        while(true) {
            TaskInfo ti = QueueWrapperFacadeSingleton.Instance().get();
            if(ti == null) {
                trytosleep();
                continue;
            }
            if(ti.getMessage() != null) {
                try { 
                    ti.getStream().write(this.getId()+":");
                    ti.getStream().write(ti.getMessage()); 
                }
                catch(Exception e) {
                    System.out.println("Cannot echo back to " + ti.getStream().getStreamID());
                }
            }
            trytosleep();
        }
    }
}

class HalfSyncPool extends Thread {

    private ArrayList<Thread> threadPool;
    
    public HalfSyncPool() {
        int cores = Runtime.getRuntime().availableProcessors();
        //initialize the threadpool with at least 2 threads and max the number of cores
        this.threadPool = new ArrayList<Thread>();
        
        for(int i = 0; i < Math.max(2, cores); i++) {
            EchoTask worker = new EchoTask();
            worker.setName("W" + i);
            worker.start();
        }
    }
}

/**
 * Echoes each line back to the client. This handler must be replaced with new one after close.
 */
class EchoServerHandler extends ServiceHandler {
    private final StreamWrapperFacade stream;

    public EchoServerHandler(StreamWrapperFacade stream) {
        super(EventType.READ);
        this.stream = stream;
    }

    @Override
    public boolean handleEvent() throws IOException {
        try {
            String echo = stream.read();
            if (echo != null) {
                //Just for in-application output
                System.out.println("Client " + stream.getStreamID() + " says: " + echo);
                //stream.write(echo);

                // Adding data to queue
                TaskInfo ti = new TaskInfo(stream, echo);
                QueueWrapperFacadeSingleton.Instance().put(ti);

                return false;
            } 
        }
        catch(Exception e) { return false; }
        return true;
    }

    @Override
    public void open(IReactor reactor) throws IOException {
        super.open(reactor);
        stream.open();
    }

    @Override
    public void close() throws IOException {
        stream.close();
    }
}

/*
 * Socket Wrapper Facade as Read/Write String Stream 
 */
class StreamWrapperFacade {
    BufferedReader in;
    BufferedWriter out;
    private final Socket socket;

    public StreamWrapperFacade(Socket socket) throws IOException {
        assert socket != null;
        this.socket = socket;
    }
    public void open() throws IOException {
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
    }
    public String read() throws IOException {
        return in.readLine();
    }
    public void write(String obj) throws IOException {
        out.write(obj);
        out.newLine();
        out.flush();
    }
    public String getStreamID() {
        Integer id = this.socket.getPort();
        return id.toString();
    }
    public void close() throws IOException {
        socket.close();
        in.close();
        out.close();
    }
}


/*
 * Wrapper Facade for global queue as a singleton
 */
class QueueWrapperFacadeSingleton {    
    private static QueueWrapperFacadeSingleton instance = null; 
    private ConcurrentLinkedQueue<TaskInfo> eventMessages;
    
    public static QueueWrapperFacadeSingleton Instance() {
        if(instance == null) 
            instance = new QueueWrapperFacadeSingleton();
        return instance;
    }
    
    protected QueueWrapperFacadeSingleton() {
        this.eventMessages = new ConcurrentLinkedQueue<TaskInfo>();
    }

    public void put(TaskInfo ti) {
        this.eventMessages.add(ti);
    }
    
    public TaskInfo get() {
        return this.eventMessages.poll();
    }
}

public class Program {
    
    //Global Concurrent Message Queue
    
    /*
     * Args process function
     * You can pass any number of param, only first is taken in consideration
     */
    private static int processArgs(String[] args) {
        int port = 8080;
        Boolean toprintusage = (args.length == 0);

        if(!toprintusage) {
            try { port = Integer.parseInt(args[0]); }
            catch(Exception e) { toprintusage = true; } //reusing same variable
        }
        if(!toprintusage) 
            return port;
        
        System.out.println("You have to specify the port number!");
        System.out.println("Usage: java Program.class <port>");
        System.out.println("Using default port 8080 ...");
        return port;
    }
    
    public static void main(String[] args) throws IOException {
        int port = processArgs(args);
        
        final IReactor acceptReadReactor = new ConcreteReactor();
        final ConcreteAcceptor echoServerAcceptor = new ConcreteAcceptor();
        echoServerAcceptor.setPort(port);
        echoServerAcceptor.open(acceptReadReactor);

        HalfSyncPool workerPool = new HalfSyncPool();
        
        acceptReadReactor.runEventLoop();
    }
}