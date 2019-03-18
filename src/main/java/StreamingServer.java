import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.unideb.tesla.camera.dto.DisclosureSchedule;
import com.unideb.tesla.camera.dto.Packet;
import com.unideb.tesla.camera.dto.TeslaMessage;
import org.apache.commons.lang3.SerializationUtils;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

public class StreamingServer {

	public static final int INTERVAL_DURATION = 5 * 1000;    // 10 seconds
	public static final int CHAIN_LENGTH = 10;
	public static final int KEY_LENGTH_IN_BITS = 256;
	public static final int DISCLOSURE_DELAY = 2;

	private String address;
	private int port;
	private boolean isRunning;
	private String webappAddress;

	private KeyChain keyChain;

	private MulticastSocket multicastSocket;

	private Retrofit retrofit;
	private ChainService chainService;
	private DeviceService deviceService;

	private List<Device> deviceList;

	public StreamingServer(String address, int port, String webappAddress) {

		this.address = address;
		this.port = port;
		this.webappAddress = webappAddress;

	}

	public void start() {

		// initialize server
		try {
			initialize();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return;
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		// start running
		isRunning = true;

		while (isRunning) {

			try {
				run();
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
				isRunning = false;
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

	}

	public void stop() {
	}

	private void run() throws NoSuchAlgorithmException, IOException {

		// check if keychain is expired
		long now = Instant.now().toEpochMilli();

		if (keyChain.isExpired(now)) {

			// fetch chain settings
			Chain chain = chainService.getActiveChain().execute().body();
			keyChain = new KeyChain(chain.getIntervalDuration(), chain.getLength(), KEY_LENGTH_IN_BITS, chain.getDelay());

			// fetch devices
			deviceList = deviceService.getAll().execute().body();

			// regenerate keychain
			keyChain.generateKeychain();

			// send disclosure schedule
			broadcastDisclosureSchedule();

		}

		// get current interval index
		int intervalIndex = (int) Math.floor((Instant.now().toEpochMilli() - keyChain.getStartOfIntervals()[0]) * 1.0 / keyChain.getIntervalDuration());

		// collect devices to send message to
		List<String> targetDevices = deviceList.stream().filter(device -> (intervalIndex + 1) % device.getFrequency() == 0).map(Device::getMac).collect(Collectors.toList());

		// broadcast teslaMessage
		TeslaMessage teslaMessage = new TeslaMessage("Hello world!", targetDevices);
		try {
			broadcastMessage(teslaMessage);
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		}

		// sleep
		try {
			Thread.sleep(keyChain.getIntervalDuration());
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	private void initialize() throws NoSuchAlgorithmException, IOException {

		// initialize retrofit
		// gson for parsing data
		Gson gson = new GsonBuilder()
				.setLenient()
				.create();

		// create retrofit object
		retrofit = new Retrofit.Builder()
				.addConverterFactory(ScalarsConverterFactory.create())
				.addConverterFactory(GsonConverterFactory.create(gson))
				.baseUrl(webappAddress)
				.build();

		// create services
		chainService = retrofit.create(ChainService.class);
		deviceService = retrofit.create(DeviceService.class);

		// get chain data from REST API
		Chain chain = chainService.getActiveChain().execute().body();

		// get devices
		deviceList = deviceService.getAll().execute().body();

		// generate keychain
		// keyChain = new KeyChain(INTERVAL_DURATION, CHAIN_LENGTH, KEY_LENGTH_IN_BITS, DISCLOSURE_DELAY);
		keyChain = new KeyChain(chain.getIntervalDuration(), chain.getLength(), KEY_LENGTH_IN_BITS, chain.getDelay());
		keyChain.generateKeychain();

		// initialize UDP socket for broadcasting messages
		multicastSocket = new MulticastSocket();
		multicastSocket.setBroadcast(true);

		// TODO: remove this later!!!!!!!
		NetworkInterface networkInterface = NetworkInterface.getByName("wlan1");
		if (networkInterface != null) {
			multicastSocket.setNetworkInterface(networkInterface);
			System.out.println("TARGETING WLAN1");
		}

		// send the first disclosure schedule
		broadcastDisclosureSchedule();

	}

	private void broadcastDisclosureSchedule() throws IOException {

		// collect data for a disclosure schedule
		long now = Instant.now().toEpochMilli();
		int intervalIndex = (int) Math.floor((now - keyChain.getStartOfIntervals()[0]) * 1.0 / keyChain.getIntervalDuration());
		// OLD: int keyCommitmentIndex = intervalIndex - disclosureDelay;
		int keyCommitmentIndex = intervalIndex - keyChain.getDisclosureDelay();
		// OLD: byte[] keyCommitment = (keyCommitmentIndex >= 0) ? keyChain.getKeys()[keyCommitmentIndex] : null;
		byte[] keyCommitment = keyChain.getKeys()[keyCommitmentIndex];

		// construct disclosure delay
		DisclosureSchedule disclosureSchedule = new DisclosureSchedule(keyChain.getIntervalDuration(), keyChain.getStartOfIntervals()[intervalIndex], intervalIndex, keyChain.getLength(), keyChain.getDisclosureDelay(), keyCommitment);

		// serialize disclosure schedule to byte array
		byte[] disclosureScheduleBytes = SerializationUtils.serialize(disclosureSchedule);

		// broadcast bytes
		broadcastBytes(disclosureScheduleBytes);

	}

	private void broadcastMessage(TeslaMessage teslaMessage) throws NoSuchAlgorithmException, InvalidKeyException, IOException {

		// TODO: if we are in the last d intervals, don't send anything!

		// collect data for constructing a package
		long now = Instant.now().toEpochMilli();
		int currentIntervalIndex = (int) Math.floor((now - keyChain.getStartOfIntervals()[0]) * 1.0 / keyChain.getIntervalDuration());
		byte[] currentKey = keyChain.getKeys()[currentIntervalIndex];
		int disclosedKeyIndex = currentIntervalIndex - keyChain.getDisclosureDelay();
		byte[] disclosedKey = keyChain.getKeys()[disclosedKeyIndex];

		// compute MAC
		byte[] messageAsBytes = SerializationUtils.serialize(teslaMessage);
		byte[] mac = CryptoUtils.computeMac(messageAsBytes, currentKey);

		// construct package
		Packet packet = new Packet(teslaMessage, mac, disclosedKey);

		// serialize package
		byte[] packageBytes = SerializationUtils.serialize(packet);

		// broadcast bytes
		broadcastBytes(packageBytes);

	}

	private void broadcastBytes(byte[] bytes) throws IOException {

		// create datagram packet
		DatagramPacket datagramPacket = new DatagramPacket(bytes, bytes.length, InetAddress.getByName(address), port);

		// broadcast
		multicastSocket.send(datagramPacket);

	}

}