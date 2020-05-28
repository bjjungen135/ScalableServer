package cs455.scaling.task;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;

public class Task implements Runnable{
	
	private LinkedBlockingDeque<List<Tasks>> taskQueue;
	private List<Tasks> tasks;
	
	public Task(LinkedBlockingDeque<List<Tasks>> taskQueue) {
		this.taskQueue = taskQueue;
		this.tasks = new ArrayList<>();
	}

	//Thread that will take the head of the queue and then execute all the tasks in the task list that was taken from queue
	@Override
	public void run() {
		try {
			while(true) {
				tasks = taskQueue.take();
				for(Tasks task : tasks)
					task.execute();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
