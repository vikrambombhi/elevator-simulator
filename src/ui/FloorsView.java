package ui;

import floor.FloorSubsystem;
import floor.SimulationVars;
import floor.Floor;
import floor.Floor.directionLampState;
import java.awt.*;
import javax.swing.*;

public class FloorsView extends JPanel {
	private FloorView[] floorViews;

    FloorsView() {
		GridLayout grid = new GridLayout(SimulationVars.numberOfFloors, 1);
		this.setLayout(grid);

		floorViews = new FloorView[SimulationVars.numberOfFloors];
		for (int i = 0; i < SimulationVars.numberOfFloors; i++) {
			floorViews[i] = new FloorView();
			this.add(floorViews[i]);
		}
	}

	public void render(FloorSubsystem f) {
		int index = f.getFloorNum();
		floorViews[index].render(f);
	}
}
