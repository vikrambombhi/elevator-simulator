package floor;

public class Floor {
	
	public enum directionLampState {IDLE, UP, DOWN};
	
	//static model
	private int floorNum;
	private boolean isBotFloor;
	private boolean isTopFloor;
	
	//dynamic model
	private boolean upLamp;
	private boolean downLamp;

	private directionLampState[] directionLamps;

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
	
	public directionLampState getDirectionLamp(int elevatorShaft) {
		return directionLamps[elevatorShaft];
	}
	
	public boolean getUpLamp() {
		return upLamp;
	}
	
	public boolean getDownLamp() {
		return downLamp;
	}
}
