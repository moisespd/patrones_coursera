/**
 * class that plays the role of "Handler" in the Reactor Pattern.
 */
public class EchoServerHandler implements EventHandler {
	private SOCStream peer_stream_;
    public EchoServerHandler(SOCStream cs) {
    	peer_stream_ = cs;
    	Initiation_Dispatcher.getInstance().register_handler(this,EventType.READ_EVENT);
    }
    public int handle_event(EventType et)
    {
    	if(et == EventType.READ_EVENT)
    	{
			while(true)
			{
				StringBuffer s = new StringBuffer();
				try{
					peer_stream_.recv(s);//read client data a line at the time
					if(s.length()>0)
					{
						System.out.println("received from the client: "+s);
						s.insert(0,"server's response: ");
						peer_stream_.send(s);//echos back to the client a line at the time
					}

				}catch(Exception ex){
					try{
						peer_stream_.close();
					}catch(Exception ex1){}
					break;
				}
			}
    	}
    	return 0;
    }
	public EventHandler get_handle()
	{
		return this;
	}
}
