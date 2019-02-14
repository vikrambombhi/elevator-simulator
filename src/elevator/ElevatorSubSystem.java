package elevator;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import messages.*;
import scheduler.Scheduler;
import floor.SimulationVars;
import messages.ElevatorRequestMessage.Direction;

/*
 * ElevatorSubSystem is the subsystem placed in each elevator.
 */
public class ElevatorSubSystem implements Runnable {
	private Elevator elevator;
	// Communication sockets
	private DatagramSocket sendSocket, receiveSocket;

	public ElevatorSubSystem(int id) {
		elevator = new Elevator(id);
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
		try {
			while (!Thread.currentThread().isInterrupted()) {
				DatagramPacket receivePacket = Message.receive(receiveSocket);
				Message m = Message.deserialize(receivePacket.getData());
				handleMessage(m);
			}
		} catch (Exception e) {
			System.out.println("ElevatorSubSystem quiting.");
			e.printStackTrace();
			close();
		}
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
		}
	}

	private void sendTravelMessage() {
		FloorTravelMessage m = new FloorTravelMessage();
		m.setElevator(elevator.getId());
		m.setStartingFloor(elevator.getFloor());

		int diff = 1;
		if (elevator.getState() == Elevator.State.MOVING_UP) {
			m.setDirection(Direction.UP);
		} else {
			m.setDirection(Direction.DOWN);
			diff *= -1;
		}

		int nextFloor = elevator.getFloor()+diff;
		byte[] data = Message.serialize(m);
		// Message the next floor
		DatagramPacket sendPacket = new DatagramPacket(data, data.length,
                SimulationVars.floorAddresses[nextFloor], SimulationVars.floorPorts[nextFloor]);
		Message.send(sendSocket, sendPacket);
	}

	private void forwardFloorRequest(FloorRequestMessage m) {
		byte[] data = Message.serialize(m);
		DatagramPacket sendPacket = new DatagramPacket(data, data.length,
                SimulationVars.schedulerAddress, SimulationVars.schedulerPort);
		Message.send(sendSocket, sendPacket);
	}
}
