// Handler event types
public enum EventTypes {
	none (0),
	accept (1),
	incoming (2),
	outgoing (4),
	timer (8);
	
	private final int value;
	EventTypes(int value) {
		this.value = value;
	}
	
	public int value() {
		return this.value;
	}		
}
