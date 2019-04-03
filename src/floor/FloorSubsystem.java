package floor;

import java.net.DatagramSocket;
import java.net.SocketException;

import floor.Floor.directionLampState;
import messages.ElevatorRequestMessage;
import messages.FloorArrivalMessage;
import messages.FloorMetaMessage;
import messages.FloorTravelMessage;
import messages.Message;
import messages.TerminateMessage;
import ui.Controller;
import messages.ElevatorRequestMessage.Direction;

import java.util.List;
import java.util.Stack;

public class FloorSubsystem implements Runnable {
	
	Controller controller;

	DatagramSocket receiveSocket;

	private Floor floor;

	private int floorNum;
	
	private boolean bExit = false;

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

	public FloorSubsystem(int num, Controller c) {
		
		controller = c;

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
		
		//give the ui a starting state
		if (controller != null) {
			controller.updateFloor(this);
		}

		while(!bExit) {
			//listen for incoming messages
			Message m = Message.deserialize(Message.receive(receiveSocket).getData());

			//if request message
			if(m instanceof FloorMetaMessage) {
				if (((FloorMetaMessage) m).getElevator() == -1) {
					requestMessage((FloorMetaMessage) m);
				} else {
					metaMessage((FloorMetaMessage) m);
				}

			//if arrived message
			} else if (m instanceof FloorArrivalMessage) {
				arrivalMessage((FloorArrivalMessage) m);
			
			//if terminate message
			} else if (m instanceof TerminateMessage) {
				terminateMessage((TerminateMessage) m);
			}
			
			//update UI after every message
			if (controller != null) {
				controller.updateFloor(this);
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

	public void arrivalMessage(FloorArrivalMessage m) {
		int arrivingElevator = m.getElevator();
		Direction direction = m.getDirection();
		System.out.printf("Floor %d: Elevator %d arrived at floor %d\n", floorNum, arrivingElevator, floorNum);
		
		//set floor lamps
		if(direction == Direction.UP) {
			floor.setDirectionLamp(arrivingElevator, directionLampState.UP);
		} else if (direction == Direction.DOWN) {
			floor.setDirectionLamp(arrivingElevator, directionLampState.DOWN);
		}
		
		//check to see if anyone is getting off here
		if (ourPassengers[m.getElevator()] != 0) {
			System.out.printf("Floor %d: %d passenger(s) stepped out of elevator %d and arrived at their destination\n", floorNum, ourPassengers[m.getElevator()], m.getElevator());
			ourPassengers[m.getElevator()] = 0;
		}

		//if a passenger is getting on going UP
		if((!goingUp.isEmpty()) && (direction == Direction.UP)) {
			System.out.printf("Floor %d: %d passenger(s) going UP stepped into elevator %d\n", floorNum, goingUp.size(), arrivingElevator);
			//simulate the passenger pressing their destination
			destinationSenders[m.getElevator()] = new Thread(new DestinationSender(floorNum, m.getElevator(), goingUp));
			destinationSenders[m.getElevator()].start();
			goingUp = new Stack<Integer>();

			//modify model
			floor.setUpLamp(false);


		//if a passenger is getting on going DOWN
		} else if ((!goingDown.isEmpty()) && (direction == Direction.DOWN)) {
			System.out.printf("Floor %d: %d passenger(s) going DOWN stepped into elevator %d\n", floorNum, goingDown.size(), arrivingElevator);

			//simulate the passenger pressing their destination
			destinationSenders[m.getElevator()] = new Thread(new DestinationSender(floorNum, m.getElevator(), goingDown));
			destinationSenders[m.getElevator()].start();
			goingDown = new Stack<Integer>();

			//modify model
			floor.setDownLamp(false);
		}
	}
	
	public void terminateMessage(TerminateMessage m) {
		int failedPickUps = goingDown.size() + goingUp.size();
		int failedDropOffs = 0;
		for (int i = 0; i < SimulationVars.numberOfElevators; i++) {
			failedDropOffs += ourPassengers[i];
		}
		System.out.printf("Floor %d: %d never picked up, %d never arrived.\n", floorNum, failedPickUps, failedDropOffs);
		bExit = true;
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
