package elevator;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.HashSet;
import java.util.Set;

import messages.*;
import ui.Controller;

/*
 * Elevator is the subsystem placed in each elevator.
 */
public class Elevator {
	
	Controller controller;

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
	private boolean hardFault = false;
	
	private Set<Integer> pressedButtons = new HashSet<Integer>();

	// Simulates the physical components attached to the elevator.
	private Motor motor;
	private Door door;

	public Elevator(int id, Controller c) {
		controller = c;
		this.id = id;
		floor = 0;
		state = State.STOPPED_DOORS_CLOSED;
		motor = new Motor();
		door = new Door();
		
		if (controller != null) {
			controller.updateElevator(this);
		}
	}

	public int getId() {
		return id;
	}

	public int getFloor() {
		return floor;
	}

	public void setFloor(int floor) {
		this.floor = floor;
		pressedButtons.remove(floor);
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

	public void setSoftFault(boolean fault) {
		this.pendingFault = fault;
		System.out.println("fault pending");
	}
	
	public void setHardFault(boolean fault) {
		hardFault = fault;
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

			

			//if there are no passengers left, we can close the door
			//just so its not sitting there idle with an open door
			if (pressedButtons.isEmpty()) {
				door.close();
    			state = State.STOPPED_DOORS_CLOSED;
			}
			
			//entered possible fault state
			if(pendingFault && state == State.STOPPED_DOORS_OPENED) {
				activeFault = true;
				pendingFault = false;
				System.out.println("fault set");
			}
			break;

		case GOUP:
            if (this.activeFault == false) {
            	//close doors if they're open
            	if (state == State.STOPPED_DOORS_OPENED) {
                    door.close();
        			state = State.STOPPED_DOORS_CLOSED;
            	}
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
            	//close doors if they're open
            	if (state == State.STOPPED_DOORS_OPENED) {
                    door.close();
        			state = State.STOPPED_DOORS_CLOSED;
            	}
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
		if (controller != null) {
			controller.updateElevator(this);
		}
	}
	
	public boolean getSoftFault() {
		return activeFault;
	}
	
	public boolean getHardFault() {
		return hardFault;
	}
	
	public void buttonPressed(int i) {
		pressedButtons.add(i);
	}
	
	public Set getButtons() {
		return pressedButtons;
	}

	public String toString() {
		return "Elevator: " + id + "\n" + "\t Floor: " + floor + "\n" + "\t State: " + state + "\n" + "\t Motor: "
				+ motor.getDirection() + "\n" + "\t Door : " + door.getPosition() + "\n";
	}
}
