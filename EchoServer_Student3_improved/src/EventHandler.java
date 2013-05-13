import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.util.concurrent.atomic.AtomicBoolean;

// Base EventHandler class
public abstract class EventHandler {
	
	private Reactor reactor;
	private EventTypes eventTypes;
	private AtomicBoolean isClosed = new AtomicBoolean(false);
	
	public EventHandler() {
	}
	
	public void register(Reactor reactor, EventTypes eventTypes) throws IOException {
		if(reactor == null) {
			throw new NullPointerException();
		}
		if(this.isClosed.get()) {
			throw new IllegalStateException();
		}
		this.reactor = reactor;
		this.eventTypes = eventTypes;
		this.reactor.addHandler(this, eventTypes);
	}

	public void close() throws IOException {
		if(!isClosed.compareAndSet(false, true)) {
			return;
		}
		Reactor reactor = this.reactor;
		if(reactor != null) {
			this.reactor = null;
			reactor.removeHandler(this, this.eventTypes);
		}
	}
	
	public abstract int handleEvent();
	
	// Gets SelectableChannel for a select operations
	public abstract SelectableChannel getSelectableChannel();
	
	protected Reactor getReactor() {
		return this.reactor;
	}
	
	protected EventTypes getEventTypes() {
		return this.eventTypes;
	}
}
