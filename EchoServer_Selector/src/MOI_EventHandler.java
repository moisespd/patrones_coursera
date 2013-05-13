import java.io.IOException;
import java.nio.channels.SelectionKey;


public abstract class MOI_EventHandler {
	private EventType etype;

	public MOI_EventHandler(EventType.EventTypes etype) {
		this.etype = new EventType(etype);		
	}
	
	public void handleEvent(MOI_Reactor reactor, SelectionKey key) throws IOException {
		System.out.println("Handling event " + etype.toString());
	}
}
