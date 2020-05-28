#!/bin/bash
for i in {1..100}
do
	java -cp build/libs/cs455.scaling.jar cs455.scaling.client.Client madison 5003 2 > /dev/null &
done
while [[ $command != 'done' ]]
do
if [[ $command == 'jobs' ]]
then
	jobs
fi
	read command
done
kill $(jobs -p)
