package messages;

import java.io.Serializable;

// request an elevator to come pick you up
// you tell it what floor you are on, and what direction you want to go
// the scheduler listens for this
public class ElevatorRequestMessage implements Message, Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 2054027936721244415L;

	public enum Direction {
		UP, DOWN
	}

	private Direction direction;
	private int originFloor;

	public ElevatorRequestMessage() {
	}

	public ElevatorRequestMessage(Direction direction, int originFloor) {
		this.direction = direction;
		this.originFloor = originFloor;
	}

	/**
	 * @return the originFloor
	 */
	public int getOriginFloor() {
		return originFloor;
	}

	/**
	 * @param originFloor the originFloor to set
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
	 * @param direction the direction to set
	 */
	public void setDirection(Direction direction) {
		this.direction = direction;
	}

}
