import java.nio.channels.SelectionKey;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;


// Mapper from Handler event types to SelectionKey operations
public final class EventTypesToSelectionOpsMapper {
	private final Map<EventTypes, Integer> mapper;
	private final static Object syncRoot = new Object(); 
	private static volatile EventTypesToSelectionOpsMapper instance;
	
	private EventTypesToSelectionOpsMapper() {
		mapper = new HashMap<EventTypes, Integer>();
		mapper.put(EventTypes.accept, SelectionKey.OP_ACCEPT);
		mapper.put(EventTypes.incoming, SelectionKey.OP_READ);
		mapper.put(EventTypes.outgoing, SelectionKey.OP_WRITE);
	}
	
	private int mapImpl(EventTypes eventTypes) {
		int result = 0;
		for(Entry<EventTypes, Integer> entry : this.mapper.entrySet()) {
			if((entry.getKey().value() & eventTypes.value()) == eventTypes.value()) {
				result |= entry.getValue();
			}
		}
		return result;
	}
	
	private static EventTypesToSelectionOpsMapper getInstance() {
		if(instance == null) {
			synchronized(syncRoot) {
				if(instance == null) {
					instance = new EventTypesToSelectionOpsMapper();
				}
			}
		}
		return instance;
	}
	
	public static int map(EventTypes eventTypes) {
		return getInstance().mapImpl(eventTypes);
	}
}
