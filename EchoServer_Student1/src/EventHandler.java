enum EventType
{
	ACCEPT_EVENT,
	READ_EVENT,
}

interface EventHandler {
    int handle_event(EventType et);
    EventHandler get_handle();
}
