package messages;

import java.io.Serializable;

// the arrival sensors use this to tell the scheduler when an elevator arrives at a floor
public class ArrivalMessage implements Message, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7521529484689926656L;

	private int floor;

	public ArrivalMessage() {

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
