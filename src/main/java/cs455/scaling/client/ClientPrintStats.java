package cs455.scaling.client;

import java.time.LocalDateTime;
import java.util.TimerTask;

public class ClientPrintStats  extends TimerTask{
	
	private Integer sent, received;
	private LocalDateTime now;
	
	public ClientPrintStats() {
		this.sent = 0;
		this.received = 0;
	}
	
	public void sentMessage() {
		sent ++;
	}
	
	public void receivedMessage() {
		received++;
	}

	//Print the statistics
	@Override
	public void run() {
		synchronized(this) {
			now = LocalDateTime.now();
			System.out.printf("[%02d:%02d:%02d] Total Sent Count: %d, Total Received Count: %d\n", now.getHour(), now.getMinute(), now.getSecond(), sent, received);
			System.out.print("\n");
			sent = 0;
			received = 0;
		}
	}

}
