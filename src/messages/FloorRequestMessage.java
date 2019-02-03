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

	private ArrayList<Integer> destinations;
	private int current;

	public FloorRequestMessage() {
		destinations = new ArrayList<Integer>();
	}

	public FloorRequestMessage(ArrayList<Integer> dests) {
		destinations = dests;
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

}
