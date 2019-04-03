package elevator;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import messages.*;
import floor.ArrivalSensor;
import floor.SimulationVars;
import messages.ElevatorRequestMessage.Direction;
import messages.ResponseTimeMessage.Subsystem;
import ui.Controller;

/*
 * ElevatorSubSystem is the subsystem placed in each elevator.
 */
public class ElevatorSubSystem implements Runnable {
	private Elevator elevator;

	//each elevator has an arrival sensor
	private Thread arrivalSensor;
	
	// Communication sockets
	private DatagramSocket sendSocket, receiveSocket;
	
	private boolean hardFaultFlag = false;
	
	private boolean bExit = false;

	public ElevatorSubSystem(int id, Controller controller) {
		elevator = new Elevator(id, controller);
		try {
			sendSocket = new DatagramSocket();
			receiveSocket = new DatagramSocket(SimulationVars.elevatorPorts[id]);
		} catch (SocketException se) {
			se.printStackTrace();
			System.exit(1);
		}
	}

	/*
	 * run starts the elevator subsystem and awaits for messages.
	 */
	@Override
	public void run() {
		int id = elevator.getId();
		System.out.printf("ElevatorSubSystem %d: Starting on port %d\n", id, SimulationVars.elevatorPorts[id]);
		while (!bExit) {
				Message m = Message.deserialize(Message.receive(receiveSocket).getData());
				handleMessage(m);
		}
		close();
	}

	/*
	 * close closes the communication sockets of the elevator subsystem.
	 */
	public void close() {
		sendSocket.close();
		receiveSocket.close();
	}

	/*
	 * returns the elevator in the system. Used for tests.
	 */
	public Elevator getElevator() {
		return elevator;
	}

	/*
	 * handleMessage runs the corresponding action for the message type.
	 */
	public void handleMessage(Message m) {
		// State machine switch
		if (m instanceof ElevatorMessage) {
			// scheduler tells the elevator to move, stop, open or close.
			elevator.handleElevatorMessage((ElevatorMessage) m);

			if (elevator.isMoving()) {
				sendTravelMessage();
			}
		} else if (m instanceof FloorRequestMessage) {
			forwardFloorRequest((FloorRequestMessage) m);
		} else if (m instanceof FloorArrivalMessage) {
			FloorArrivalMessage message = (FloorArrivalMessage) m;
			elevator.setFloor(message.getFloor());
		} else if (m instanceof FaultMessage) {
			if (((FaultMessage) m).getHardFault()) {
				hardFaultFlag = true;
				elevator.setHardFault(true);
			} else {
                elevator.setSoftFault(true);
			}
		} else if (m instanceof TerminateMessage) {
			bExit = true;
		}
	}

	private void sendTravelMessage() {
		if (elevator.getState() == Elevator.State.MOVING_UP) {
			arrivalSensor = new Thread(new ArrivalSensor(elevator.getId(), elevator.getFloor(), elevator.getFloor()+1));
		} else {
			arrivalSensor = new Thread(new ArrivalSensor(elevator.getId(), elevator.getFloor(), elevator.getFloor()-1));
		}
		//if we arent simulating a hard fault
		if (!hardFaultFlag) {
			arrivalSensor.start();
		}
	}

	private void forwardFloorRequest(FloorRequestMessage m) {
		elevator.buttonPressed(m.getDestination());
		byte[] data = Message.serialize(m);
		DatagramPacket sendPacket = new DatagramPacket(data, data.length,
				SimulationVars.schedulerAddress, SimulationVars.schedulerPort);
		long initTime = System.nanoTime();
		Message.send(sendSocket, sendPacket);
		//wait for echo
		Message.receive(sendSocket);
		long elapsedTime = System.nanoTime() - initTime;
		//report response time
		ResponseTimeMessage r = new ResponseTimeMessage();
		r.setSubsystem(Subsystem.DestinationSender);
		r.setTime(elapsedTime);
		data = Message.serialize(r);
		DatagramPacket pack = new DatagramPacket(data, data.length, SimulationVars.floorSystemAddress, SimulationVars.timerPort);
		Message.send(sendSocket, pack);
	}
}
