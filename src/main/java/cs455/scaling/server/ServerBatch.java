package cs455.scaling.server;

import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

import cs455.scaling.task.Tasks;

public class ServerBatch extends TimerTask{
	
	private Server server;
	
	public ServerBatch(Server server) {
		this.server = server;
	}

	//Given batch timeout, add the current jobs to the queue
	@Override
	public void run() {
		synchronized(server) {
			if(!server.readWriteList.isEmpty() && !server.registerList.isEmpty()) {
				List<Tasks> newList = new ArrayList<Tasks>(server.deepCopy(server.readWriteList));
				newList.addAll(server.deepCopy(server.registerList));
				server.taskQueue.offer(newList);
				server.readWriteList.clear();
				server.registerList.clear();
			}
			else if(!server.readWriteList.isEmpty() && server.registerList.isEmpty()) {
				server.taskQueue.offer(server.deepCopy(server.readWriteList));
				server.readWriteList.clear();
			}
			else if(server.readWriteList.isEmpty() && !server.registerList.isEmpty()) {
				server.taskQueue.offer(server.deepCopy(server.registerList));
				server.registerList.clear();
			}
		}
	}

}
