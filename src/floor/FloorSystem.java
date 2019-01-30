package floor;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import floor.Floor.directionLampState;
import messages.ElevatorRequestMessage;
import messages.ElevatorRequestMessage.Direction;
import messages.FloorArrivalMessage;
import messages.FloorTravelMessage;
import messages.Message;

public class FloorSystem {

	DatagramSocket receiveSocket;
	
	//model of the system
	Floor[] floors;
	
	//this has to be a thread because it has to sleep while the rest of the simulation is active
	Thread[] arrivalSensors;
	
	//this has to be a thread because it has to sleep while the rest of the simulation is active
	Thread requestSimulator;
	
	//this class holds all the information for each active trip
	TripTracker tripTracker;
	
	public FloorSystem() {
		//set up socket to listen
		try {
			receiveSocket = new DatagramSocket(4004);
		} catch (SocketException se) {
			se.printStackTrace();
			System.exit(1);
		}
		
		//create floor and sensor arrays
		floors = new Floor[SimulationVars.numberOfFloors];
		arrivalSensors = new Thread[SimulationVars.numberOfElevators];
		
		//populate the floor array with valid objects
		for (int f = 0; f < SimulationVars.numberOfFloors; f++) {
			boolean isTop = false;
			boolean isBot = false;
			if (f == 0){
				isBot = true;
			}  if (f == SimulationVars.numberOfFloors-1) {
				isTop = true;
			} 
			floors[f] = new Floor(f, isBot, isTop);
		}
		
		System.out.println("Floor System: Modelling building with "+SimulationVars.numberOfFloors+" floors and "+SimulationVars.numberOfElevators+" elevators");
		
		tripTracker = new TripTracker();
		
		requestSimulator = new Thread(new RequestSimulator());
		System.out.println("Floor System: Beginning simulation");
		requestSimulator.start();
		listenAndDispatch();
	}
	
	public void listenAndDispatch() {	
		
		//listen for incoming messages
		byte[] data = new byte[100];
		Message m = Message.deserialize(Message.receive(receiveSocket, data).getData());
		
		//if request message
		if(m instanceof ElevatorRequestMessage) {
			int floorToUpdate = ((ElevatorRequestMessage) m).getFloor();
			if (((ElevatorRequestMessage) m).getDirection() == Direction.UP) {
				floors[floorToUpdate].setUpLamp(true);
				tripTracker.addTrip(floorToUpdate, Direction.UP);
				System.out.println("Floor System: Passenger requesting elevator UP from floor "+floorToUpdate);
			} else {
				floors[floorToUpdate].setDownLamp(true);
				tripTracker.addTrip(floorToUpdate, Direction.DOWN);
				System.out.println("Floor System: Passenger requesting elevator DOWN from floor "+floorToUpdate);
			}
			
			
		}
		
		//if approaching floor message
		if(m instanceof FloorTravelMessage) {
			int elevatorToUpdate = ((FloorTravelMessage) m).getElevator();
			int startingFloor = ((FloorTravelMessage) m).getStartingFloor();
			int floorToArriveTo = startingFloor;
			if (((FloorTravelMessage) m).getDirection() == FloorTravelMessage.Direction.UP) {
				floorToArriveTo++;
				System.out.println("Floor System: Elevator "+elevatorToUpdate+" travelling UP from floor "+startingFloor+" to floor "+floorToArriveTo);
				
				//update direction lamps
				for (int i = 0; i < SimulationVars.numberOfElevators; i++) {
					floors[i].setDirectionLamp(elevatorToUpdate, directionLampState.UP);
				}
			} else {
				floorToArriveTo--;
				System.out.println("Floor System: Elevator "+elevatorToUpdate+" travelling DOWN from floor "+startingFloor+" to floor "+floorToArriveTo);
				
				//update direction lamps
				for (int i = 0; i < SimulationVars.numberOfElevators; i++) {
					floors[i].setDirectionLamp(elevatorToUpdate, directionLampState.DOWN);
				}
			}
			//arm the respective sensor to fire
			arrivalSensors[elevatorToUpdate] = new Thread(new ArrivalSensor(elevatorToUpdate, startingFloor, floorToArriveTo));
			arrivalSensors[elevatorToUpdate].start();
		}
			
		//if arrived message
		if (m instanceof FloorArrivalMessage) {
				int arrivingFloor = ((FloorArrivalMessage) m).getFloor();
				int arrivingElevator = ((FloorArrivalMessage) m).getElevator();
				messages.FloorArrivalMessage.Direction direction = ((FloorArrivalMessage) m).getDirection();
				System.out.println("Floor System: Elevator arrived at a floor "+arrivingFloor);
				
				//if a passenger is getting on going UP
				if(floors[arrivingFloor].getUpLamp() && direction == messages.FloorArrivalMessage.Direction.UP) {
					
					//simulate the passenger pressing their destination
					tripTracker.sendTripDestination(arrivingElevator, arrivingFloor, Direction.UP);
					
					//modify model
					floors[arrivingFloor].setUpLamp(false);
					System.out.println("Floor System: Passenger going UP stepped into elevator "+arrivingElevator);
					
				//if a passenger is getting on going DOWN
				} else if (floors[arrivingFloor].getDownLamp() && direction == messages.FloorArrivalMessage.Direction.DOWN) {
					
					//simulate the passenger pressing their destination
					tripTracker.sendTripDestination(arrivingElevator, arrivingFloor, Direction.DOWN);
					
					//modify model
					floors[arrivingFloor].setDownLamp(false);
					System.out.println("Floor System: Passenger going DOWN stepped into elevator "+arrivingElevator);
				}
		}
		//keep listening forever...
		listenAndDispatch();
	}
}