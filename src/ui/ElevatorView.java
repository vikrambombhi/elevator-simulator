package ui;

import elevator.Elevator;
import java.awt.*;
import javax.swing.*;

public class ElevatorView extends JPanel {
	private JLabel id;
	private JLabel floor;
	private JLabel state;

	ElevatorView() {
		GridLayout grid = new GridLayout(3, 1);
		this.setLayout(grid);
		this.setBorder(BorderFactory.createLineBorder(Color.black));

		id = new JLabel();
		id.setMaximumSize(new Dimension(10, 10));
		this.add(id);

		floor = new JLabel();
		this.add(floor);

		state = new JLabel();
		this.add(state);
	}

	public void render(Elevator e) {
		id.setText("Elevator " + e.getId() + ":");
		floor.setText("Floor: " + e.getFloor());
		state.setText("State: " + e.getState());
	}
}
