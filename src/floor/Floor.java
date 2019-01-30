package floor;

public class Floor {
	
	//static model
	int floorNum;
	boolean isBotFloor;
	boolean isTopFloor;
	
	//dynamic model
	boolean upLamp;
	boolean downLamp;
	enum directionLampState {IDLE, UP, DOWN};
	directionLampState[] directionLamps;

	public Floor(int floor, boolean isBot, boolean isTop) {
		floorNum = floor;
		isBotFloor = isBot;
		isTopFloor = isTop;
		
		upLamp = false;
		downLamp = false;
		directionLamps = new directionLampState[SimulationVars.numberOfElevators];
		for (int i = 0; i < SimulationVars.numberOfElevators; i++) {
			directionLamps[i] = directionLampState.IDLE;
		}
	}
	
	public void setUpLamp(boolean b) {
		upLamp = b;
	}
	
	public void setDownLamp(boolean b) {
		downLamp = b;
	}
	
	public void setDirectionLamp(int elevatorShaft, directionLampState state) {
		directionLamps[elevatorShaft] = state;
	}
	
	public boolean getUpLamp() {
		return upLamp;
	}
	
	public boolean getDownLamp() {
		return downLamp;
	}
}
