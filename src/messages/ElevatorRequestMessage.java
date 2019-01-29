package messages;

import java.io.Serializable;

// request an elevator when you press an up or down button
public class ElevatorRequestMessage implements Message, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2054027936721244415L;

	public enum Direction {
		UP, DOWN
	}

	private Direction direction;
	private int destinationFloor;
    private int originFloor;

	public ElevatorRequestMessage() {}

	public ElevatorRequestMessage(Direction direction, int destinationFloor, int originFloor) {
        this.direction = direction;
        this.destinationFloor = destinationFloor;
        this.originFloor = originFloor;
    }

	/**
	 * @return the destinationFloor
	 */
	public int getDesitationFloor() {
		return destinationFloor;
	}

	/**
	 * @param destinationFloor
	 *            the destinationFloor to set
	 */
	public void setDestinationFloor(int destinationFloor) {
		this.destinationFloor = destinationFloor;
	}

	/**
	 * @return the originFloor
	 */
	public int getOriginFloor() {
		return originFloor;
	}

	/**
	 * @param originFloor
	 *            the originFloor to set
	 */
	public void setOriginFloor(int originFloor) {
		this.originFloor = originFloor;
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
