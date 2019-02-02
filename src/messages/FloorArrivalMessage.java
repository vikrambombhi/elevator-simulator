package messages;

import java.io.Serializable;
import java.util.ArrayList;

import messages.ElevatorRequestMessage.Direction;

//this message is sent from the arrival Sensor to all concerning subsystems when an elevator reaches its destination
public class FloorArrivalMessage implements Message, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4085784915939113686L;

	private int elevator;
	private int floor;
	private Direction direction;
	private ArrayList<Integer> destinations;

	public FloorArrivalMessage() {
		destinations = new ArrayList<Integer>();
	}

	public FloorArrivalMessage(int fl, int el, Direction dir, ArrayList<Integer> dests) {
		floor = fl;
		elevator = el;
		direction = dir;
		destinations = dests;
	}

	/**
	 * @return the elevator
	 */
	public int getElevator() {
		return elevator;
	}

	/**
	 * @param elevator the elevator to set
	 */
	public void setElevator(int elevator) {
		this.elevator = elevator;
	}

	/**
	 * @return the floor
	 */
	public int getFloor() {
		return floor;
	}

	/**
	 * @param floor the floor to set
	 */
	public void setFloor(int floor) {
		this.floor = floor;
	}

	/**
	 * @return the direction
	 */
	public Direction getDirection() {
		return direction;
	}

	/**
	 * @param direction the direction to set
	 */
	public void setDirection(Direction direction) {
		this.direction = direction;
	}

	/**
	 * @return the destinations
	 */
	public ArrayList<Integer> getDestinations() {
		return destinations;
	}

	/**
	 * @param destinations the destinations to set
	 */
	public void setDestinations(ArrayList<Integer> destinations) {
		this.destinations = destinations;
	}
}