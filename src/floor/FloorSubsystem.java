package floor;

import java.net.DatagramSocket;
import java.net.SocketException;

import floor.Floor.directionLampState;
import messages.ElevatorRequestMessage;
import messages.FloorArrivalMessage;
import messages.FloorMetaMessage;
import messages.FloorTravelMessage;
import messages.Message;
import messages.ElevatorRequestMessage.Direction;

import java.util.List;
import java.util.Stack;

public class FloorSubsystem implements Runnable {

	DatagramSocket receiveSocket;

	private Floor floor;

	private int floorNum;

	//each elevator need and arrival sensor
	private Thread[] arrivalSensors;

	private Thread requestSimulator;

	//to send the destination of passengers to each elevator
	private Thread[] destinationSenders;

	//list of expected passengers and the elevators they will be on
	//ourPassengers[n] will hold the number of passengers that will get off on this floor when elevator n arrives
	private int[] ourPassengers;

	//list of passengers going up - we store their destination
	private List<Integer> goingUp;

	//list of passengers going up - we store their destination
	private List<Integer> goingDown;

	public FloorSubsystem(int num) {

		floorNum = num;

		//set up socket to listen
		try {
			receiveSocket = new DatagramSocket(SimulationVars.floorPorts[num]);
		} catch (SocketException se) {
			se.printStackTrace();
			System.exit(1);
		}

		//create floor model and sensor arrays
		floor = new Floor(num);
		arrivalSensors = new Thread[SimulationVars.numberOfElevators];
		destinationSenders = new Thread[SimulationVars.numberOfElevators];

		//set up passenger models
		ourPassengers = new int[SimulationVars.numberOfElevators];
		for (int i = 0; i < SimulationVars.numberOfElevators; i++) {
			ourPassengers[i] = 0;
		}

		goingUp = new Stack<Integer>();
		goingDown = new Stack<Integer>();

		requestSimulator = new Thread(new RequestSimulator(floorNum));
		System.out.println("Floor "+num+": Ready");

	}

	@Override
	public void run() {
		requestSimulator.start();
		System.out.println("Floor "+floorNum+": Started");

		while(true) {
			//listen for incoming messages
			Message m = Message.deserialize(Message.receive(receiveSocket).getData());

			//if request message
			if(m instanceof FloorMetaMessage) {
				if (((FloorMetaMessage) m).getElevator() == -1) {
					requestMessage((FloorMetaMessage) m);
				} else {
					metaMessage((FloorMetaMessage) m);
				}
			}

			//if approaching floor message
			else if(m instanceof FloorTravelMessage) {
				travelMessage((FloorTravelMessage) m);
			}

			//if arrived message
			else if (m instanceof FloorArrivalMessage) {
				arrivalMessage((FloorArrivalMessage) m);
			}
		}
	}

	//request message from the simulator...
	public void requestMessage(FloorMetaMessage m) {
		if(m.getStartingFloor() < m.getDestinationFloor()) {
			floor.setUpLamp(true);
			goingUp.add(m.getDestinationFloor());
		} else {
			floor.setDownLamp(true);
			goingDown.add(m.getDestinationFloor());
		}
	}

	//another floor letting us know passengers are on their way to our floor
	public void metaMessage(FloorMetaMessage m) {
		ourPassengers[m.getElevator()]++;
	}

	public void travelMessage(FloorTravelMessage m) {
		int elevatorToUpdate = m.getElevator();
		int startingFloor = m.getStartingFloor();
		if (m.getDirection() == Direction.UP) {
			System.out.printf("Floor %d: Elevator %d travelling UP from floor %d to floor %d\n", floorNum, elevatorToUpdate, startingFloor, floorNum);

			//update direction lamp
			floor.setDirectionLamp(elevatorToUpdate, directionLampState.UP);

		} else {
			System.out.printf("Floor %d: Elevator %d travelling DOWN from floor %d to floor %d\n", floorNum, elevatorToUpdate, startingFloor, floorNum);

			//update direction lamp
			floor.setDirectionLamp(elevatorToUpdate, directionLampState.DOWN);
		}
		//arm the respective sensor to fire
		arrivalSensors[elevatorToUpdate] = new Thread(new ArrivalSensor(elevatorToUpdate, startingFloor, floorNum));
		arrivalSensors[elevatorToUpdate].start();
	}

	public void arrivalMessage(FloorArrivalMessage m) {
		int arrivingElevator = m.getElevator();
		Direction direction = m.getDirection();
		System.out.printf("Floor %d: Elevator %d arrived at floor %d\n", floorNum, arrivingElevator, floorNum);

		//check to see if anyone is getting off here
		if (ourPassengers[m.getElevator()] != 0) {
			System.out.printf("Floor %d: %d passenger(s) stepped out of elevator %d and arrived at their destination\n", floorNum, ourPassengers[m.getElevator()], m.getElevator());
			ourPassengers[m.getElevator()] = 0;
		}

		//if a passenger is getting on going UP
		if(floor.getUpLamp() && (direction == null || direction == Direction.UP || floorNum == 0)) {
			System.out.printf("Floor %d: %d passenger(s) going UP stepped into elevator %d\n", floorNum, goingUp.size(), arrivingElevator);
			//simulate the passenger pressing their destination
			destinationSenders[m.getElevator()] = new Thread(new DestinationSender(floorNum, m.getElevator(), goingUp));
			destinationSenders[m.getElevator()].start();
			goingUp = new Stack<Integer>();

			//modify model
			floor.setUpLamp(false);


		//if a passenger is getting on going DOWN
		} else if (floor.getDownLamp() && (direction == null || direction == Direction.DOWN || floorNum == SimulationVars.numberOfFloors-1)) {
			System.out.printf("Floor %d: %d passenger(s) going DOWN stepped into elevator %d\n", floorNum, goingDown.size(), arrivingElevator);

			//simulate the passenger pressing their destination
			destinationSenders[m.getElevator()] = new Thread(new DestinationSender(floorNum, m.getElevator(), goingDown));
			destinationSenders[m.getElevator()].start();
			goingDown = new Stack<Integer>();

			//modify model
			floor.setDownLamp(false);
		}
	}

	public int getFloorNum() {
		return floorNum;
	}

	public Floor getFloor() {
		return floor;
	}

	public List<Integer> getUpRequests(){
		return goingUp;
	}

	public List<Integer> getDownRequests(){
		return goingDown;
	}

	public int[] getPassengers() {
		return ourPassengers;
	}

	public void tearDown() {
		//clean up
		receiveSocket.close();
	}
}
