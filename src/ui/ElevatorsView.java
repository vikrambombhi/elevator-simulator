package ui;

import elevator.Elevator;
import floor.SimulationVars;
import java.awt.*;
import javax.swing.*;

public class ElevatorsView extends JPanel {
	private ElevatorView[] elevatorViews;

    ElevatorsView() {
		GridLayout grid = new GridLayout(SimulationVars.numberOfElevators, 1);
		this.setLayout(grid);

		elevatorViews = new ElevatorView[SimulationVars.numberOfElevators];
		for (int i = 0; i < SimulationVars.numberOfElevators; i++) {
			elevatorViews[i] = new ElevatorView();
			this.add(elevatorViews[i]);
		}
	}

	public void render(Elevator e) {
		int index = e.getId();
		elevatorViews[index].render(e);
	}
}
