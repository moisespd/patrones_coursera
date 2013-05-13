import java.io.IOException;
import java.net.SocketException;

/**
 * class that plays the role of "Acceptor" in the Acceptor-Connector Pattern.
 */
public class EchoAcceptor implements EventHandler {
    private SOCKAcceptor acceptor_;
    
    public EchoAcceptor(int port) {
    	try{
       		acceptor_ = new SOCKAcceptor(port);
       	}
       	catch(IOException ex){
       		System.out.println("The port '" + port + "' couldn't be opened to accept connections");
       	}
    }
    public void init()
    {
		Initiation_Dispatcher.getInstance().register_handler(this,EventType.ACCEPT_EVENT);
    }
	public int handle_event(EventType et)
	{
		if(et!=EventType.ACCEPT_EVENT)
			return -1;

		SOCStream newConnection = new SOCStream ();
		try{
			acceptor_.accept(newConnection);
		}
		catch(SocketException ex){
			System.out.println("It is not possible to listen connections");
		}
		catch(IOException ex1){
			System.out.println("An I/O error has occurred while trying to listen connections");
		}
		EchoServerHandler handler = new EchoServerHandler(newConnection);

		return 0;
	}
	public EventHandler get_handle()
	{
		return this;
	}
}
