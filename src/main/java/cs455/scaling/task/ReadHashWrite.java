package cs455.scaling.task;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.security.NoSuchAlgorithmException;

import cs455.scaling.util.Hashing;
import cs455.scaling.util.StatisticsCollector;

public class ReadHashWrite implements Tasks{
	
	private SelectionKey key;
	private SocketChannel client;
	private ByteBuffer readBuffer, writeBuffer;
	private StatisticsCollector stats;

	public ReadHashWrite(SelectionKey key, StatisticsCollector stats) {
		this.key = key;
		this.client = (SocketChannel) key.channel();
		this.readBuffer = ByteBuffer.allocate(8192);
		this.writeBuffer = ByteBuffer.allocate(40);
		this.stats = stats;
		stats.addReadWriteKey(key);
		key.attach(null);
	}

	//Read from the buffer, then hash what was read, then send the result back to the client
	@Override
	public void execute() {
		try {
			int bytesRead = 0;
			while(readBuffer.hasRemaining() && bytesRead != -1)
				bytesRead = client.read(readBuffer);
			String hashedMessage = Hashing.SHA1FromBytes(readBuffer.array());
			writeBuffer = ByteBuffer.wrap(hashedMessage.getBytes());
			while(writeBuffer.hasRemaining())
				client.write(writeBuffer);
			writeBuffer.clear();
			stats.sentMessage(key);
		} catch (NoSuchAlgorithmException | IOException e) {
			e.printStackTrace();
		}
	}

}
