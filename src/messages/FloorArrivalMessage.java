package messages;

import java.io.Serializable;

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
	
	public FloorArrivalMessage() {
	}
	
	public int getElevator() {
		return elevator;
	}
	
	public int getFloor() {
		return floor;
	}
	
	public Direction getDirection() {
		return direction;
	}
	
	public void setElevator(int e) {
		elevator = e;
	}
	
	public void setFloor(int f) {
		floor = f;
	}

	public void setDirection(Direction d) {
		direction = d;
	}
}
