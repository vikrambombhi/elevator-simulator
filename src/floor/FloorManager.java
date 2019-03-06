package floor;

public class FloorManager {

	private static Thread floorSubsystems[];
	private static Thread faultSimulator;

	public FloorManager() {

	}

	public static void main(String args[]) {
		faultSimulator = new Thread(new FaultSimulator());
		faultSimulator.start();
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

			floorSubsystems[i] = new Thread(new FloorSubsystem(i));
			floorSubsystems[i].start();
		}

        for (Thread t : floorSubsystems) {
            try { t.join(); } catch (InterruptedException e) { }
        }
	}
}
