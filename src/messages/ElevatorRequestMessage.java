package messages;

import java.io.Serializable;

public class ElevatorRequestMessage implements Message, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2054027936721244415L;

	public enum Direction {
		UP, DOWN
	}

	private Direction direction;
	private int floor;

	public ElevatorRequestMessage() {
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

	/**
	 * @return the direction
	 */
	public Direction getDirection() {
		return direction;
	}

	/**
	 * @param direction
	 *            the direction to set
	 */
	public void setDirection(Direction direction) {
		this.direction = direction;
	}

}
