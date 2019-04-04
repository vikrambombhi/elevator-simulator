package ui;

import elevator.Elevator;
import elevator.Elevator.State;
import floor.Floor;
import floor.Floor.directionLampState;
import floor.FloorSubsystem;
import floor.SimulationVars;
import elevator.ElevatorManager;
import floor.FloorManager;

public class Controller {
	private View view;
	private Thread elevatorManager;
	private Thread floorManager;

	public Controller() {
		view = new View();
		elevatorManager = new Thread(new ElevatorManager(SimulationVars.numberOfElevators, this));
		floorManager = new Thread(new FloorManager(this));
	}

    public void run() {
		elevatorManager.start();
		floorManager.start();
		view.display();
    }

	public void updateElevator(Elevator elevator) {
        view.getElevatorsView().render(elevator);
	}

	public void updateFloor(FloorSubsystem floor) {
        view.getFloorsView().render(floor);
	}

	public static void main(String[] args) {
		Controller c = new Controller();
		c.run();
	}
}
