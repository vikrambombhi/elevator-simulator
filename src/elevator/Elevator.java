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

	public enum State {
		MOVING_UP, MOVING_DOWN, STOPPED_DOORS_CLOSED, STOPPED_DOORS_OPENED
	}

	private int id;
	private int floor;
	private State state;

	// Simulates the physical components attached to the elevator.
	private Motor motor;
	private Door door;

	public Elevator(int id) {
		this.id = id;
		floor = 0;
		state = State.STOPPED_DOORS_CLOSED;
		motor = new Motor();
		door = new Door();
	}

	public int getId() {
		return id;
	}

	public int getFloor() {
		return floor;
	}

	public void setFloor(int floor) {
		this.floor = floor;
	}

	public State getState() {
		return state;
	}

	/*
	 * handleElevatorMessage handles commands for the elevator's physical
	 * components.
	 */
	public void handleElevatorMessage(ElevatorMessage m) {
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
		default:
			System.out.println("Elevator: ElevatorMessage type case not handled: " + m.getMessageType());
			System.exit(1);
		}

		System.out.println(this);
	}

	public String toString() {
		return "Elevator: " + id + "\n" + "\t Floor: " + floor + "\n" + "\t State: " + state + "\n" + "\t Motor: "
				+ motor.getDirection() + "\n" + "\t Door : " + door.getPosition() + "\n";
	}
}
