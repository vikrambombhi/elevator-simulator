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
	public enum State {
		MOVING_UP, MOVING_DOWN, STOPPED_DOORS_CLOSED, STOPPED_DOORS_OPENED
	}

	private int id;
	private int floor;
	private State state;
	//pending fault holds that a soft fault will happen in the future
	//active fault becomes true when pending fault is true and a soft fault can be simulated (stopped doors open)
	private boolean pendingFault = false;
	private boolean activeFault = false;

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

	public void setState(State s) {
		state = s;
	}

	public boolean isMoving() {
		return state == State.MOVING_UP || state == State.MOVING_DOWN;
	}

	public void setFault(boolean fault) {
		this.pendingFault = fault;
		System.out.println("fault pending");
	}

	/*
	 * handleElevatorMessage handles commands for the elevator's physical
	 * components.
	 */
	public void handleElevatorMessage(ElevatorMessage m) {
		//System.out.println("Elevator: Handling message of type " + m.getMessageType());
		switch (m.getMessageType()) {
		case STOP:
			assert (state == State.MOVING_UP || state == State.MOVING_DOWN);
			motor.stop();
            door.open();
			state = State.STOPPED_DOORS_OPENED;

            // Delay open/close door
            try { Thread.sleep(100); } catch (InterruptedException e) { }

            door.close();
			state = State.STOPPED_DOORS_CLOSED;
			
			//entered possible fault state
			if(pendingFault) {
				activeFault = true;
				pendingFault = false;
				System.out.println("fault set");
			}
			
			break;

		case GOUP:
            if (this.activeFault == false) {
                assert (state == State.STOPPED_DOORS_CLOSED);
                motor.move(Motor.Direction.UP);
                state = State.MOVING_UP;
            } else {
				activeFault = false;
				System.out.println("fault cleared");
            }
			break;

		case GODOWN:
            if (this.activeFault == false) {
                assert (state == State.STOPPED_DOORS_CLOSED);
                motor.move(Motor.Direction.DOWN);
                state = State.MOVING_DOWN;
            } else {
            	activeFault = false;
				System.out.println("fault cleared");
            }
			break;
		default:
			System.out.println("Elevator: ElevatorMessage type case not handled: " + m.getMessageType());
			System.exit(1);
		}

		//System.out.println(this);
	}

	public String toString() {
		return "Elevator: " + id + "\n" + "\t Floor: " + floor + "\n" + "\t State: " + state + "\n" + "\t Motor: "
				+ motor.getDirection() + "\n" + "\t Door : " + door.getPosition() + "\n";
	}
}
