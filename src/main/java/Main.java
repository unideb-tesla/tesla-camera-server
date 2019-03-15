public class Main {

	public static final String ADDRESS = "230.1.2.3";
	public static final int PORT = 9999;

	public static void main(String[] args) {

		StreamingServer streamingServer = new StreamingServer(ADDRESS, PORT);

		streamingServer.start();

	}

}
