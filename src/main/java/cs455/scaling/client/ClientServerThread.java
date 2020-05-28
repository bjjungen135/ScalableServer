package cs455.scaling.client;

import java.io.IOException;

public class ClientServerThread implements Runnable{
	
	private Client client;
	
	public ClientServerThread(Client client) {
		this.client = client;
	}

	//Listening for messages
	@Override
	public void run() {
		while(true) {
			int bytesRead;
			try {
				bytesRead = client.channel.read(client.readBuffer);
				if(bytesRead == -1)
					continue;
				else
					client.receivedMessage(client.channel, client.readBuffer);
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
	}
	
	
}
