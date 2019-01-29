package messages;

import java.io.Serializable;

// This is for elevators sending messages to the scheduler
public class FloorRequestMessage implements Message, Serializable {

	// floor to go to
	private int destination, current;

	/**
	 * 
	 */
	private static final long serialVersionUID = -3632886442858251411L;

	public FloorRequestMessage() {
	}

	/**
	 * @return the floor
	 */
	public int getFloor() {
		return destination;
	}

	/**
	 * @param floor
	 *            the floor to set
	 */
	public void setFloor(int floor) {
		this.destination = floor;
	}

	/**
	 * @return the current
	 */
	public int getCurrent() {
		return current;
	}

	/**
	 * @param current
	 *            the current to set
	 */
	public void setCurrent(int current) {
		this.current = current;
	}

}
