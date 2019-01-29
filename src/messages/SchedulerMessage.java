package messages;

import java.io.Serializable;

// Any subsystem sending messages to the scheduler should use this message class
public class SchedulerMessage implements Message, Serializable {

	// floor to go to
	private int floor;

	/**
	 * 
	 */
	private static final long serialVersionUID = -3632886442858251411L;

	public SchedulerMessage() {
	}

	/**
	 * @return the floor
	 */
	public int getFloor() {
		return floor;
	}

	/**
	 * @param floor
	 *            the floor to set
	 */
	public void setFloor(int floor) {
		this.floor = floor;
	}

}
