package ui;

import elevator.ElevatorManager;
import floor.FloorManager;
import floor.SimulationVars;

public class Model {
	private Controller controller;
	private Thread elevatorManager;
	private Thread floorManager;

	public Model() {
		controller = new Controller();
		elevatorManager = new Thread(new ElevatorManager(SimulationVars.numberOfElevators, controller));
		floorManager = new Thread(new FloorManager(controller));
	}

	public void run() {
		elevatorManager.start();
		floorManager.start();
	}

	public static void main(String[] args) {
		Model m = new Model();
		m.run();
	}
}
