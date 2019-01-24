SimulationVars - is a temp proof of concept class that holds the systems static variables

FloorSystem - runs as a Java Application and creates Floor objects according to static specs
			- simulates an elevator request on each floor at an interval
			
Floor 	- Models the requirements of the floors *mostly symbolic rather than functional*
		- Each floor has a DatagramSocket that it uses to send requests to the scheduler
		- Each floor has an ArrivalSensor which runs in a separate thread...
		
ArrivalSensor	- ArrivalSensor is a simple client with a DatagramSocket than handles arrival messages only
					- Starts by sending a 'hello' message to the scheduler
					- continually listens on its socket for incoming arrival messages
					- responds to these messages asking the scheduler to stop the elevator or not depending on if
						that arriving elevator will be able to serve one of it's pending requests
						
Notes: The scheduler will receive request messages and the floor which sent them will be documented in the message.
	This will not be true of arrival messages... When the ArrivalSensor sends the scheduler a hello message, it will
	contain its floor number and the scheduler will need to hold onto each floors port/IP as a variable so that it can 
	pass arrival messages from the elevator to the sensor in the future.
	The reason this design choice is made is to maintain maximum up time for the sensor (it shouldn't be unblocked
	when a new request is being sent, it might miss an elevator that it needs!).