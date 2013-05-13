import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Reactor {
	private List<EventHandler> _listeners = new ArrayList<EventHandler>();

	public synchronized void register_handler(EventHandler concrete_event_handler) {
		_listeners.add(concrete_event_handler);
	}

	public synchronized void remove_handler(EventHandler concrete_event_handler) {
		_listeners.remove(concrete_event_handler);
	}
	
	public synchronized void _raise_new_connection_event() {
		Iterator<EventHandler> listeners = _listeners.iterator();
		while (listeners.hasNext()) {
			((EventHandler) listeners.next()).handle_new_connection_stablished();
		}
	}
	
	public synchronized void _raise_new_message_received() {
		Iterator<EventHandler> listeners = _listeners.iterator();
		while (listeners.hasNext()) {
			((EventHandler) listeners.next()).handle_new_message_received();
		}
	}
}
