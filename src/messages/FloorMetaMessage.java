package messages;

import java.io.Serializable;

//a message sent between threads of the floor subsystems...
//when a trip is simulated its destination is sent to the starting floor
//when a passenger get on an elevator the passengers destination floor is given notice
public class FloorMetaMessage implements Message, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2335896835400332649L;
	private int startingFloor;
	private int destinationFloor;
	private int elevator;
	
	public FloorMetaMessage(boolean fromRequestSimulator) {
		if(fromRequestSimulator) {
			elevator = -1;
		}
	}
	
	public int getStartingFloor() {
		return startingFloor;
	}
	
	public int getDestinationFloor() {
		return destinationFloor;
	}
	
	public int getElevator() {
		return elevator;
	}
	
	public void setStartingFloor(int s) {
		startingFloor = s;
	}
	
	public void setDestinationFloor(int d) {
		destinationFloor = d;
	}
	
	public void setElevator(int e) {
		elevator = e;
	}
}
