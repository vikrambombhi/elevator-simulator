package messages;

import java.io.Serializable;

//the elevator sends a floor travel message to the floor subsystem when it begins to move... awaits response for arrival
public class FloorTravelMessage implements Message, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8346973549456679527L;

	public enum Direction {
		UP, DOWN;
	}
	
	private int startingFloor;
	private Direction direction;
	private int elevator;
	
	public FloorTravelMessage() {
	}
	
	public Direction getDirection() {
		return direction;
	}
	
	public int getStartingFloor() {
		return startingFloor;
	}
	
	public int getElevator() {
		return elevator;
	}
	
	public void setDirection(Direction d) {
		direction = d;
	}
	
	public void setStartingFloor(int f) {
		startingFloor = f;
	}
	
	public void setElevator(int e) {
		elevator = e;
	}
}
