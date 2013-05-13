import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Reactor {
	private int limit;
	private int count;
	private Selector selector;
	private EventHandler[] handlers;
	private EventTypes[] filterHandlers;
	private final Object syncRoot = new Object();
	private Map<SelectionKey, Integer> handlerMapper;
	
	public final int EXIT_EVENT = Integer.MIN_VALUE;
	
	private volatile static Reactor instance;
	private final static Object instanceLock = new Object();
	
	private Reactor() {
		
	}
	
	public static Reactor getInstance() {
		if(instance == null) {
			synchronized (instanceLock) {
				if(instance == null) {
					instance = new Reactor();
				}
			}
		}
		return instance;
	}
	
	public void open(int limit) throws IOException {
		if(limit <= 0) {
			throw new IllegalArgumentException();
		}
		synchronized(this.syncRoot) {
			if(this.handlers != null) {
				throw new IllegalStateException(); 
			}
			this.count = 0;
			this.limit = limit;
			this.handlers = new EventHandler[limit];
			this.filterHandlers = new EventTypes[limit];
		}
	}
	
	public void addHandler(EventHandler handler, EventTypes eventTypes) throws IOException {
		if(handler == null) {
			throw new NullPointerException();
		}
		synchronized(this.syncRoot) {
			if(this.handlers == null) {
				throw new IllegalStateException();
			}
			if(this.count >= this.limit) {
				throw new IllegalStateException();
			}
			
			this.handlers[this.count] = handler;
			this.filterHandlers[this.count++] = eventTypes;
			
			this.handlerMapper = null;
			Selector select = this.selector;
			if(selector != null) {
				this.selector = null;
				select.close();
			}
		}
	}
	
	public boolean removeHandler(EventHandler handler, EventTypes eventTypes) throws IOException {
		if(handler == null) {
			throw new NullPointerException();
		}
		synchronized(this.syncRoot) {
			if(this.handlers == null) {
				throw new IllegalStateException();
			}
			return removeHandlerImpl(handler, eventTypes);
		}
	}
	
	public void close() throws IOException {
		synchronized (this.syncRoot) {
			for(int i = this.count; i-- > 0;) {
				removeHandlerImpl(this.handlers[i], this.filterHandlers[i]);
			}
			this.handlers = null;
			this.selector = null;
			this.handlerMapper = null;
			this.filterHandlers = null;
		}
	}
	
	public int waitEvent() throws IOException {
		return waitEvent(0);
	}
	
	public int waitEvent(long timeout) throws IOException {
		Selector select;
		synchronized (this.syncRoot) {
			if(this.handlers == null || this.count == 0) {
				return EXIT_EVENT;
			}
			select = getSelector();
		}
		int result = select.select(timeout);
		synchronized (this.syncRoot) {
			if(this.handlerMapper != null) {
				// need to store selected keys in a separate collection
				// because handling readable selections is a bit tricky in Java NIO
				List<SelectionKey> selected = new ArrayList<SelectionKey>();
				selected.addAll(select.selectedKeys());
				for(int i = 0; i < selected.size(); ++i) {
					SelectionKey key = selected.get(i);
			        int idx = this.handlerMapper.get(key);
			        if(key.isReadable()) {
			        	// special handling for readable selections
				        handleReadableSelection(select, selected, key, idx);
			        } else if(this.handlers[idx].handleEvent() < 0) {
						this.handlers[idx].close();
			        }
				}
				
				return result;
			}
		}
		return -1;
	}
	
	public void runEventLoop() {
		try {
			while(waitEvent() != EXIT_EVENT);
		} catch(IOException ioe) {
			// TODO Auto-generated catch block
			ioe.printStackTrace();
		}
	}
	
	// Gets a selector with registered handlers for select  
	private Selector getSelector() throws IOException {
		Selector select;
		select = this.selector;
		if(selector == null) {
			select = this.selector = Selector.open();
			this.handlerMapper = new HashMap<SelectionKey, Integer>();
			for(int i = 0; i < this.count; ++i) {
				SelectionKey key = this.handlers[i].getSelectableChannel().register(
											select,
											EventTypesToSelectionOpsMapper.map(this.filterHandlers[i]));
				this.handlerMapper.put(key, i);
			}
		}
		return select;
	}

	// Read operation required blocking work with a socket, so readable event should be
	// handled in a special way
	private void handleReadableSelection(Selector select, List<SelectionKey> selected,
										 SelectionKey key, int idx) throws IOException {
		SelectableChannel channel = key.channel();
		this.handlerMapper.remove(key);
		key.cancel();
		int opResult = 0;
		channel.configureBlocking(true);
		try {
			if((opResult = this.handlers[idx].handleEvent()) < 0) {
				this.handlers[idx].close();
			}
		} finally {
			if(opResult >= 0) {
				channel.configureBlocking(false);
				// Java NIO hack: it isn't possible to register channel
				// which SelectionKey was canceled until a new select operation is executed
				if(select.selectNow() != 0) {
					// added a new events to handle if there are exist
					selected.addAll(select.selectedKeys());
				}
				SelectionKey newKey = channel.register(
											select,
											EventTypesToSelectionOpsMapper.map(this.filterHandlers[idx]));
				this.handlerMapper.put(newKey, idx);
			}
		}
	}
	
	private boolean removeHandlerImpl(EventHandler handler, EventTypes eventTypes) throws IOException {
		for(int i = this.handlers.length; i-- > 0;) {
			if(this.handlers[i] == handler && this.filterHandlers[i] == eventTypes) {
				this.handlers[i] = this.handlers[--this.count];
				this.filterHandlers[i] = this.filterHandlers[this.count];
				this.handlers[this.count] = null;
				this.handlerMapper = null;
				Selector selector = this.selector;
				if(selector != null) {
					this.selector = null;
					selector.close();
				}
				handler.close();
				
				return true;
			}
		}
		return false;
	}
}
