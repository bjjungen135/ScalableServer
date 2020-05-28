package cs455.scaling.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import cs455.scaling.util.Hashing;

public class Client extends TimerTask{
	
	private String serverHost;
	private int serverPort;
	private int messageRate;
	public SocketChannel channel;
	private ByteBuffer writeBuffer;
	public ByteBuffer readBuffer;
	private byte[] message;
	private Random random;
	private String hashedMessage;
	private HashMap<String, Integer> storedHashes;
	private Timer timer;
	private ClientServerThread clientServerThread;
	private ClientPrintStats clientPrintStats;
	
	//Constructor
	private Client(String serverHost, int serverPort, int messageRate) {
		this.serverHost = serverHost;
		this.serverPort = serverPort;
		this.messageRate = messageRate;
		this.message = new byte[8192];
		this.random = new Random();
		this.storedHashes = new HashMap<>();
		this.timer = new Timer();
		this.clientServerThread = new ClientServerThread(this);
		this.clientPrintStats = new ClientPrintStats();
	}
	
	//Allocate all space for buffers and start connection
	private void initialize() throws IOException {
		writeBuffer = ByteBuffer.allocate(8192);
		readBuffer = ByteBuffer.allocate(40);
		readBuffer.flip();
		channel = SocketChannel.open(new InetSocketAddress(serverHost, serverPort));
		new Thread(clientServerThread).start();
	}
	
	//Creates the random byte array
	private void generateRandomByteArray() {
		random.nextBytes(message);
	}
	
	//Calls the hash method and stores the value as a key
	private void calculateHash() throws NoSuchAlgorithmException {
		hashedMessage = Hashing.SHA1FromBytes(message);
		if(storedHashes.containsKey(hashedMessage))
			storedHashes.put(hashedMessage, storedHashes.get(hashedMessage) + 1);
		else
			storedHashes.put(hashedMessage, 1);	
	}
	
	//Starts the timer tasks for the client, sending and printing
	private void startTimer() {
		timer.scheduleAtFixedRate(this, 0L, (long)(messageRate * 1000));
		timer.scheduleAtFixedRate(clientPrintStats, 20000L, 20000L);
	}
	
	//Sends message to server
	private void sendMessage() throws IOException, NoSuchAlgorithmException {
		this.generateRandomByteArray();
		this.calculateHash();
		writeBuffer = ByteBuffer.wrap(message);
		while(writeBuffer.hasRemaining())
			channel.write(writeBuffer);
		clientPrintStats.sentMessage();
	}
	
	//Reads the message from the server and handles it accordingly
	public void receivedMessage(SocketChannel channel, ByteBuffer readBuffer) throws IOException {
		int bytesRead = 0;
		while(readBuffer.hasRemaining() && bytesRead != -1)
			bytesRead = channel.read(readBuffer);
		readBuffer.position(0);
		readBuffer.limit(40);
		byte[] response = new byte[40];
		readBuffer.get(response, 0, 40);
		readBuffer.clear();
		String temp = new String(response);
		if(storedHashes.containsKey(temp)) {
			storedHashes.put(temp, storedHashes.get(temp) - 1);
			if(storedHashes.get(temp) == 0)
				storedHashes.remove(temp);
			clientPrintStats.receivedMessage();
		}
	}
	
	//Main
	public static void main(String[] args) throws IOException, NoSuchAlgorithmException, InterruptedException {
		Client client = new Client(args[0], Integer.parseInt(args[1]), Integer.parseInt(args[2]));
		client.initialize();
		client.startTimer();
	}

	//Listens for messages from the server
	@Override
	public void run() {
		try {
			this.sendMessage();
		} catch (IOException | NoSuchAlgorithmException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

}
