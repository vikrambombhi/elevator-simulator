package ui;

import elevator.Elevator;
import javax.swing.*;

public class ElevatorView extends JPanel {
    private JLabel id;
    private JLabel floor;
    private JLabel state;

    ElevatorView() {
        id = new JLabel();
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
