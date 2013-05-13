public class Reactor {
    public static final int DEFAULT_PORT = 10000;
	static int app_port;
    public static void main(String[] args) {

		parseArgs(args);

    	EchoAcceptor ea = new EchoAcceptor(app_port);
		for(;;)
		{
			ea.init();
			Initiation_Dispatcher.getInstance().handleEvents();
		}
    }
    private static void parseArgs(String[] args)
	{
		app_port = DEFAULT_PORT;
		if(args.length>0)
		{
			try{
				app_port = Integer.parseInt(args[0]);
			}
			catch(NumberFormatException e){
			}
		}
	}
}
