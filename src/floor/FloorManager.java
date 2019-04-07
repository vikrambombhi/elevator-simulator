package floor;

import ui.Controller;

public class FloorManager implements Runnable {

	private static Thread floorSubsystems[];
	private static Thread faultSimulator;
	private static Thread responseTimer;

	public FloorManager(Controller controller) {
		floorSubsystems = new Thread[SimulationVars.numberOfFloors];
		faultSimulator = new Thread(new FaultSimulator());
		responseTimer = new Thread(new ResponseTimer());
		for (int i = 0; i < SimulationVars.numberOfFloors; i++) {
			floorSubsystems[i] = new Thread(new FloorSubsystem(i, controller));
		}
	}
	
	@Override
	public void run() {
		faultSimulator.start();
		responseTimer.start();
		for (int i = 0; i < SimulationVars.numberOfFloors; i++) { floorSubsystems[i].start();}
	}
}
