import java.util.LinkedList;
import java.util.Queue;


/**
 * class that plays the role of "Dispatcher" in the Reactor Pattern.
 */
public class Initiation_Dispatcher {
    //class needed to group the handler with its associated operation in a single object to be stored in a queue
    class EventAction
    {
		private EventHandler eh;
		private EventType et;
		public EventAction(EventHandler eh, EventType et)
		{
			this.eh = eh;
			this.et = et;
		}
		public EventHandler getHandler()
		{
			return this.eh;
		}
		public EventType getEventType()
		{
			return this.et;
		}
    }

    //Queue that holds all the handlers
    Queue queue;

    //singleton for the dispatcher object
    private static Initiation_Dispatcher disp;
    public static Initiation_Dispatcher getInstance()
    {
    	if(disp==null)
    		disp = new Initiation_Dispatcher();
    	return disp;
    }

	//dispatcher initialization
    public Initiation_Dispatcher()
    {
		queue = new LinkedList<EventAction>();
    }

    //register a handler in the queue
    int register_handler(EventHandler eh,EventType et)
    {
    	queue.add(new EventAction(eh,et));
    	return 0;
    }
    //remove a handler not implemented
	int remove_handler(EventHandler eh,EventType et)
	{
		return 0;
	}

	//Dequeue all the events that have been registered in the queue
	int handleEvents()
	{
		while(queue.size()>0)
		{
			EventAction ea = (EventAction)queue.poll();
			EventHandler eh = ea.getHandler();
			EventType et = ea.getEventType();
			eh.handle_event(et);
		}
		return 0;
	}
}
