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
public class ElevatorSubSystem {
	// State is the possible states that the elevator can be in.
	public static String HOST = "127.0.0.1";
	public static short PORT = 4000;

	// TODO: support more than 1 elevator
	private Elevator elevator;
	// Communication sockets
	private DatagramSocket sendSocket, receiveSocket;

	public ElevatorSubSystem() {
		elevator = new Elevator(0);
		try {
			sendSocket = new DatagramSocket();
			receiveSocket = new DatagramSocket(PORT);
		} catch (SocketException se) {
			se.printStackTrace();
			System.exit(1);
		}
	}

	/*
	 * run starts the elevator subsystem and awaits for messages.
	 */
	public void run() {
		try {
			while (true) {
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
	 * handleMessage runs the corresponding action for the message type.
	 */
	private void handleMessage(Message m) {
		// State machine switch
		if (m instanceof ElevatorMessage) {
			// scheduler tells the elevator to move, stop, open or close.
			elevator.handleElevatorMessage((ElevatorMessage) m);

			if (isMoving(elevator)) {
				sendTravelMessage(elevator);
			}
		} else if (m instanceof FloorRequestMessage) {
			forwardFloorRequest((FloorRequestMessage) m);
		} else if (m instanceof FloorArrivalMessage) {
			FloorArrivalMessage message = (FloorArrivalMessage) m;
			elevator.setFloor(message.getFloor());
		}
	}

	private boolean isMoving(Elevator e) {
		return e.getState() == Elevator.State.MOVING_UP || e.getState() == Elevator.State.MOVING_DOWN;
	}

	private void sendTravelMessage(Elevator e) {
		FloorTravelMessage m = new FloorTravelMessage();
		m.setElevator(elevator.getId());
		m.setStartingFloor(e.getFloor());

		if (e.getState() == Elevator.State.MOVING_UP) {
			m.setDirection(Direction.UP);
		} else {
			m.setDirection(Direction.DOWN);
		}

		byte[] data = Message.serialize(m);
		DatagramPacket sendPacket = new DatagramPacket(data, data.length, SimulationVars.floorAddresses[e.getFloor()],
				SimulationVars.floorPorts[e.getFloor()]);
		Message.send(sendSocket, sendPacket);
	}

	private void forwardFloorRequest(FloorRequestMessage m) {
		byte[] data = Message.serialize(m);
		DatagramPacket sendPacket = new DatagramPacket(data, data.length, SimulationVars.schedulerAddress,
				SimulationVars.schedulerPort);
		Message.send(sendSocket, sendPacket);
	}

	public static void main(String args[]) {
		System.out.println("ElevatorSubSystem: Starting on port 4000");
		ElevatorSubSystem e = new ElevatorSubSystem();
		e.run();
	}
}
