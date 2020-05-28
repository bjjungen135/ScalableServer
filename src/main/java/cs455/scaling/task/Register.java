package cs455.scaling.task;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import cs455.scaling.util.StatisticsCollector;

public class Register implements Tasks{
	
	private Selector selector;
	private ServerSocketChannel serverSocket;
	private SelectionKey key;
	private StatisticsCollector stats;
	
	public Register(Selector selector, ServerSocketChannel serverSocket, SelectionKey key, StatisticsCollector stats) {
		this.selector = selector;
		this.serverSocket = serverSocket;
		this.key = key;
		this.stats = stats;
	}

	//Accept the channel into the selector and set the interest as OP_READ to show it's ready to be read from
	@Override
	public void execute() {
		try {
			synchronized(serverSocket) {
				SocketChannel client = serverSocket.accept();
				if(client == null) return;
				client.configureBlocking(false);
				client.register(selector, SelectionKey.OP_READ);
			}
			stats.addClient(key);
			key.attach(null);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
