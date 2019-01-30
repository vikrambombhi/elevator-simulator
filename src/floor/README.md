FloorSystem:
	Receives ALL messages for the subsystem (port 4004) and dispatches them to the class that can correctly react 
	and/or send a response. Supports messages of type...
		-ElevatorRequestMessage: sent by the RequestSimulater when a elevatorRequestMessage
			is sent to the scheduler... just keeps the model and TripTracker up to date.
		-FloorTravelMessage: sent by the Elevator to indicate it has begun traveling. FloorSystem will arm a new
			ArrivalSensor and start the thread it is stored in. The rest is handled by ArrivalSensor.
		-FloorArrivalMessage: sent by the ArrivalSensor when an elevator finally reaches a floor. The FloorSystem
			checks its model to see if there are passengers interested in getting on and if so it notifies TripTracker
			to give the elevator the destination
			
Floor:
	The lamps in the static model of the requirements
	
ArrivalSensor:
	A new ArrivalSensor is spawned into a new thread every time an elevator indicates it is moving towards a floor on its path.
	Construct an ArrivalSensor with the elevator it will callback to, the floor the elevator is starting on and the floor the
	elevator will eventually reach. 
		-calling start() on a thread armed with an arrival sensor will sleep for the duration of the elevators travel and eventually
		wake up and fire an ElevatorArrivalMessage to the scheduler, the floorSystem and the elevator itself.
		
RequestSimulator:
	A thread that sleeps between firing off ElevatorRequestMessage to the scheduler and the floorSystem.
		-in future iterations it will rely on an input file but for now it has a hard coded sequences of
			requests.
			
TripTracker:
	Maintains the model of active passengers who are either waiting for their elevator or waiting to reach their destination.
	When the floorSystems notices that an elevator arrives that will serve a waiting passenger it notifies TripTracker to update
	that passengers trip status as well as send a FloorRequestMessage to the arriving elevator - indicating that passengers destination.
		-in this current PR, TripTracker is a very shallow facade that pretends to know/care about where the passengers want
			to go
			
SimulationVars:
	static variables used throughout the floor subsystem. I've stored variables in here that I believe would be useful for other 
	subsystems to know as well.
