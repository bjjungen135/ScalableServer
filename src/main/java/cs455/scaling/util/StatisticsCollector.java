package cs455.scaling.util;

import java.nio.channels.SelectionKey;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.TimerTask;

public class StatisticsCollector extends TimerTask{
	
	private Integer numberClients;
	private Integer throughPut;
	private Double meanPerclientThroughput;
	private Double stdDevPerclientThroughput;
	private LocalDateTime now;
	private HashMap<SelectionKey, Integer> clients;
	
	public StatisticsCollector() {
		this.numberClients = 0;
		this.throughPut = 0;
		this.meanPerclientThroughput = 0.0;
		this.stdDevPerclientThroughput = 0.0;
		this.clients = new HashMap<>();
	}
	
	//Add the key to get the client
	public void addReadWriteKey(SelectionKey key) {
		if(!clients.containsKey(key))
			clients.put(key, 0);
	}

	//Increment number of clients
	public void addClient(SelectionKey key) {
		synchronized(numberClients) {
			numberClients++;
		}
	}
	
	//Increment throughput and track the client that sent the message
	public void sentMessage(SelectionKey key) {
		synchronized(throughPut) {
			throughPut++;
		}
		clients.replace(key, clients.get(key) + 1);
	}

	//Print out all the statistics when timer hits
	@Override
	public void run() {
		synchronized(this) {
			if(numberClients > 0) {
				int clientsSent = 0;
				int sumXi = 0;
				for(Map.Entry<SelectionKey, Integer> entry : clients.entrySet()) {
					clientsSent += entry.getValue();
				}
				Double temp = (double) numberClients;
				meanPerclientThroughput = clientsSent / temp;
				for(Map.Entry<SelectionKey, Integer> entry : clients.entrySet()) {
					sumXi += Math.pow((entry.getValue() - meanPerclientThroughput), 2);
					clients.replace(entry.getKey(), 0);
				}
				stdDevPerclientThroughput = Math.sqrt(sumXi / (numberClients));
			}
			now = LocalDateTime.now();
			System.out.printf("[%02d:%02d:%02d] Server Throughput: %d messages, Active Client Connections: %d, Mean Per-client Throughput:"
				+ " %.5f messages, Std. Dev. Of Per-client Throughput: %.5f messages\n", now.getHour(), now.getMinute(), now.getSecond(), throughPut, numberClients, meanPerclientThroughput, stdDevPerclientThroughput);
			System.out.print("\n");
			throughPut = 0;
			meanPerclientThroughput = 0.0;
			stdDevPerclientThroughput = 0.0;
		}
	}

}
