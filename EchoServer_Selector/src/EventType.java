
public class EventType {
	public enum EventTypes {
		NewConnectionRequest,
		MessageReceivedFromClient,
		ConnectionClosedFromClient
	}

	private EventTypes etype;
	
	public EventType(EventTypes eType) {
		this.etype = eType;
	}
	
	public String toString() {
		String str = "";
		
		switch (this.etype) {
			case NewConnectionRequest:
				str = "NewConnectionRequest";
				break;
			case MessageReceivedFromClient:
				str = "MessageReceivedFromClient";
				break;
			case ConnectionClosedFromClient:
				str = "ConnectionClosedFromClient";
				break;
		}
		
		return str;
	}
}