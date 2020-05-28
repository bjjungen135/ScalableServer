package cs455.scaling.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.concurrent.LinkedBlockingDeque;

import cs455.scaling.task.ReadHashWrite;
import cs455.scaling.task.Register;
import cs455.scaling.task.Task;
import cs455.scaling.task.Tasks;
import cs455.scaling.util.StatisticsCollector;

public class Server implements Runnable{
	
	private Selector selector;
	private ServerSocketChannel serverSocket;
	private int portnum, threadPoolSize, batchSize, batchTime;
	public LinkedBlockingDeque<List<Tasks>> taskQueue;
	public List<Tasks> registerList;
	public List<Tasks> readWriteList;
	private InetAddress ip;
	private StatisticsCollector stats;
	private Timer timer;
	private ServerBatch serverBatch;
	
	//Constructor
	private Server(int portnum, int threadPoolSize, int batchSize, int batchTime) {
		this.portnum = portnum;
		this.batchSize = batchSize;
		this.batchTime = batchTime;
		this.threadPoolSize = threadPoolSize;
	}
	
	//Initialize all variables
	private void initialize() throws IOException {
		ip = InetAddress.getLocalHost();
		selector = Selector.open();
		serverSocket = ServerSocketChannel.open();
		serverSocket.bind(new InetSocketAddress(ip.getHostName(), portnum));
		serverSocket.configureBlocking(false);
		serverSocket.register(selector, SelectionKey.OP_ACCEPT);
		taskQueue = new LinkedBlockingDeque<List<Tasks>>();
		stats = new StatisticsCollector();
		registerList = new ArrayList<>();
		readWriteList = new ArrayList<>();
		timer = new Timer();
		serverBatch = new ServerBatch(this);
	}
	
	//Starts the thread pool
	private void startThreadPool() {
		for(int i = 0; i < threadPoolSize; i++)
			new Thread(new Task(taskQueue)).start();
	}
	
	//Make a register task and then put it at the front of the queue
	private void register(Selector selector, ServerSocketChannel serverSocket, SelectionKey key) throws IOException, InterruptedException {
		Register register = new Register(selector, serverSocket, key, stats);
		registerList.add(register);
		taskQueue.offerFirst(deepCopy(registerList));
		registerList.clear();
	}
	
	//Reads, hashes, then writes to the client
	private void readWrite(SelectionKey key) throws InterruptedException {
		ReadHashWrite data = new ReadHashWrite(key, stats);
		readWriteList.add(data);
		if(readWriteList.size() == batchSize) {
			taskQueue.offer(deepCopy(readWriteList));
			readWriteList.clear();
		}
	}
	
	//Start the batch timer and the statistics printing
	private void startTimer() {
		timer.scheduleAtFixedRate(stats, 20000L, 20000L);
		timer.scheduleAtFixedRate(serverBatch, (long)(batchTime * 1000), (long)(batchTime * 1000));
	}
	
	//Copy tasks that need to be added to queue
	public synchronized List<Tasks> deepCopy(List<Tasks> list) {
		List<Tasks> deepCopy = new ArrayList<>();
		for(Tasks task : list) {
			deepCopy.add(task);
		}
		return deepCopy;
	}

	public static void main(String[] args) throws NumberFormatException, IOException, InterruptedException {
		Server server = new Server(Integer.parseInt(args[0]), Integer.parseInt(args[1]), Integer.parseInt(args[2]), Integer.parseInt(args[3]));
		server.initialize();
		Thread selectorThread = new Thread(server);
		selectorThread.start();
		server.startTimer();
		server.startThreadPool();
	}

	//Loop over keys to look at channels with activity
	@Override
	public void run() {
		ByteBuffer buffer;
		while(true) {
			try {
				selector.selectNow();
				Set<SelectionKey> selectedKeys = selector.selectedKeys();
				Iterator<SelectionKey> iter = selectedKeys.iterator();
				while(iter.hasNext()) {
					SelectionKey key = iter.next();
					if(key.isValid() == false) continue;
					if(key.isAcceptable() && key.attachment() == null) {
						key.attach(stats);
						register(selector, serverSocket, key);
					}
					if(key.isReadable() && key.attachment() == null) {
						key.attach(stats);
						buffer = ByteBuffer.allocate(8192);
						SocketChannel client = (SocketChannel) key.channel();
						int bytesRead = client.read(buffer);
						if(bytesRead == -1) {
							key.attach(null);
							continue;
						}
						else {
							readWrite(key);
						}
					}
					iter.remove();
				}
			}
			catch(IOException | InterruptedException e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
	}
	
}
