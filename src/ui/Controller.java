package ui;

import elevator.Elevator;
import elevator.Elevator.State;
import floor.Floor;
import floor.Floor.directionLampState;
import floor.FloorSubsystem;
import floor.SimulationVars;

public class Controller {
	private MainView mainView;

	public Controller() {
		mainView = new MainView();
		mainView.display();
	}

	public void updateElevator(Elevator elevator) {
        mainView.getElevatorsView().render(elevator);
	}

	public void updateFloor(FloorSubsystem floor) {
        //mainView.renderFloor(floor);
	}
}
