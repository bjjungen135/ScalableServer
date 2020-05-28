To run the program you will issue these command after untar
gradle build
java -cp build/libs/'name of direcotry'.jar cs455.scaling.server.Server 'portnumber' 'thread_pool_size' 'batch_size' 'batch_time'
java -cp build/libs/'name of direcotry'.jar cs455.scaling.client.Client 'hostname' 'portnumber' 'message_rate'

All classes are in the folder
src/main/java/cs455/scaling/client
src/main/java/cs455/scaling/server
src/main/java/cs455/scaling/task
src/main/java/cs455/scaling/util

Class Descriptions -

Client.java - This class will create a client that connects to a server and will generate random byte arrays and then hash them, store that hash, send the array to the server and then sleep a specified amount of time and repeat.

ClientPrintStats.java - This class will print the clients stats as a timertask every 20 seconds.

ClientServerThread.java - This class is setup to listen for incoming messages from the server that the client is connected to.

Server.java - This class initializes the server with the thread pool and then will also manage the batch size. It will recieve registration requests, and then read requests. It will then put those into a queue for the worker threads to work on.

ServerBatch.java - This class is a timer task and given batch timeout it will add all unadded jobs in the queue for the worker threads.

ReadHashWrite.java - This class is using the tasks interface, so the work is done in the execute function, but it reads from a channel, hashes the message, then sends the hash back to the client that sent the message.

Register.java - This class also uses the tasks interface, and it's execute method will register a channel to the selector with the OP_READ status showing the server that the channel is now ready to be read from.

Task.java - This is the worker thread class, that takes a linked blocking dequeue and will take the head of the queue and work on the task that was on the queue.

Tasks.java - Interface for the tasks that needed to be done.

Hashing.java - This class is only for hashing, it takes in a byte array and gives back a string that is 40 bytes long.

StatisticsCollector.java - This class collects all the data on the server and will print out the throughput, clients connected, mean per client throughput, and the standard deviation of mean per client throughput.
