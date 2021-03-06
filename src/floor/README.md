FloorSubsystem:
	One node per floor - listens on a port listed in SimulationVars.floorPorts. Accepts FloorMetaMessages,
	FloorTravelMessages and FloorArrivalMessages. Stores a list of elevator requests and their destinations so 
	this information can be sent to the elevator that serves the requests (separate lists for up and down).
	FloorSubsystem also shares information between floors regarding passenger end points to verify completed trips
			
Floor:
	The lamps in the static model of the requirements
	
ArrivalSensor:
	A new ArrivalSensor is spawned into a new thread every time an elevator indicates it is moving towards a floor on its 	path.
	Construct an ArrivalSensor with the elevator it will callback to, the floor the elevator is starting on and the floor the
	elevator will eventually reach. 
		-calling start() on a thread armed with an arrival sensor will sleep for the duration of the elevators travel and 	eventually wake up and fire an ElevatorArrivalMessage to the scheduler, the respective floorSubsystem and 
	the elevator itself.
		
RequestSimulator:
	A thread that sleeps between firing off ElevatorRequestMessage to the scheduler and FloorMetaMessages to 
	the floorSubystem of origin.
		- supports input file requests from input.txt
			
DestinationSender:
	Takes a list of passengers and their destinations and shoots them off to the respective elevator. This is run
	in a separate thread to not bog down the FloorSubsystem socket.
			
SimulationVars:
	static variables used throughout the floor subsystem. I've stored variables in here that I believe would be useful for other 
	subsystems to know as well.
	
File:
	handles all input file reading and parses lines in the file to requests and meta messages for the RequestSimulator to utilize. 
