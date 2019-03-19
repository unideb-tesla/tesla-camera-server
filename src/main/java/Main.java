public class Main {

	public static void main(String[] args) {

		if(args.length != 3){

			System.out.println("You must provide: broadcast address, broadcast port and webapp address as command line arguments!");
			return;

		}

		String broadcastAddress = args[0];
		int broadcastPort = Integer.parseInt(args[1]);
		String webappAddress = args[2];

		StreamingServer streamingServer = new StreamingServer(broadcastAddress, broadcastPort, webappAddress);

		streamingServer.start();

	}

}
