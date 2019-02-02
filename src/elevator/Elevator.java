package elevator;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import messages.*;

/*
 * Elevator is the subsystem placed in each elevator.
 */
public class Elevator {
	// State is the possible states that the elevator can be in.
	public static String HOST = "127.0.0.1";
	public static short PORT = 4000;

	enum State {
		MOVING_UP, MOVING_DOWN, STOPPED_DOORS_CLOSED, STOPPED_DOORS_OPENED
	}

	private State state;

	// Simulates the physical components attached to the elevator.
	private Motor motor;
	private Door door;

	// Communication sockets
	private DatagramSocket sendSocket, receiveSocket;

	public Elevator() {
		state = State.STOPPED_DOORS_CLOSED;
		motor = new Motor();
		door = new Door();

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
				System.out.println("Elevator: Listening for messages");
				DatagramPacket receivePacket = Message.receive(receiveSocket);
				Message m = Message.deserialize(receivePacket.getData());
				handleMessage(m);
			}
		} catch (Exception e) {
			System.out.println("Elevator quiting.");
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
			handleElevatorMessage((ElevatorMessage) m);
		}
		// TODO: handle elevator floor being requested and routed to the scheduler
	}

	/*
	 * handleElevatorMessage handles commands for the elevator's physical
	 * components.
	 */
	private void handleElevatorMessage(ElevatorMessage m) {
		System.out.println("Elevator: Handling message of type " + m.getMessageType());
		switch (m.getMessageType()) {
		case STOP:
			assert (state == State.MOVING_UP || state == State.MOVING_DOWN);
			motor.stop();
			state = State.STOPPED_DOORS_CLOSED;
			break;

		case GOUP:
			assert (state == State.STOPPED_DOORS_CLOSED);
			motor.move(Motor.Direction.UP);
			state = State.MOVING_UP;
			break;

		case GODOWN:
			assert (state == State.STOPPED_DOORS_CLOSED);
			motor.move(Motor.Direction.DOWN);
			state = State.MOVING_DOWN;
			break;

		case OPENDOOR:
			assert (state == State.STOPPED_DOORS_CLOSED);
			door.open();
			state = State.STOPPED_DOORS_OPENED;
			break;

		case CLOSEDOOR:
			assert (state == State.STOPPED_DOORS_OPENED);
			door.close();
			state = State.STOPPED_DOORS_CLOSED;
			break;
		default:
			System.out.println("Elevator: ElevatorMessage type case not handled: " + m.getMessageType());
			System.exit(1);
		}
	}

	public static void main(String args[]) {
		System.out.println("Elevator: Starting on port 4000");
		Elevator e = new Elevator();
		e.run();
	}
}
