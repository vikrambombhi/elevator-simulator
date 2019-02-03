package messages;

import java.io.Serializable;
import java.util.ArrayList;

// this is sent by the elevator to the elevator subsystem to the scheduler
// this requests a floor. After this message is received by the scheduler,
// the scheduler will send the elevator in a direction
public class FloorRequestMessage implements Message, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5307796681383023119L;

	private int current, destination;

	public FloorRequestMessage() {
	}

	/**
	 * @return the current
	 */
	public int getCurrent() {
		return current;
	}

	/**
	 * @param current the current to set
	 */
	public void setCurrent(int current) {
		this.current = current;
	}

	/**
	 * @return the destination
	 */
	public int getDestination() {
		return destination;
	}

	/**
	 * @param destination the destination to set
	 */
	public void setDestination(int destination) {
		this.destination = destination;
	}

}
