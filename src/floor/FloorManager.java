package floor;

public class FloorManager {

	private static Thread floorSubsystems[];
	
	public FloorManager() {
		
	}
	
	public static void main(String args[]) {
		floorSubsystems = new Thread[SimulationVars.numberOfFloors];
		
		for (int i = 0; i < SimulationVars.numberOfFloors; i ++) {
			boolean isBot = false;
			boolean isTop = false;

			if (i == 0) {
				isBot = true;
			}
			if (i == SimulationVars.numberOfFloors-1) {
				isTop = true;
			}
			
			floorSubsystems[i] = new Thread(new FloorSubsystem(i, isTop, isBot));
			floorSubsystems[i].start();
		}
	}
}
