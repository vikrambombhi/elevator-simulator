package ui;

import elevator.Elevator;
import java.awt.*;
import javax.swing.*;

public class ElevatorView extends JPanel {
    private JLabel id;
    private JLabel floor;
    private JLabel state;
    private JLabel dropOffs;
    private JLabel faultState;

    ElevatorView() {
        GridLayout grid = new GridLayout(5, 1);
        this.setLayout(grid);
        this.setBorder(BorderFactory.createLineBorder(Color.black));
        this.setMaximumSize(new Dimension(100, 100));

        id = new JLabel();
        this.add(id);
        floor = new JLabel();
        this.add(floor);
        state = new JLabel();
        this.add(state);
        dropOffs = new JLabel();
        this.add(dropOffs);
        faultState = new JLabel();
        this.add(faultState);
    }

    public void render(Elevator e) {
        id.setText("Elevator " + e.getId() + ":");
        floor.setText("Floor: " + e.getFloor());
        state.setText("State: " + e.getState());
        dropOffs.setText("Drop Offs: " + e.getButtons().toString());
        renderFault(e);
    }

    private void renderFault(Elevator e) {
        if (e.getHardFault()) {
            faultState.setText("Fault: Out of Order");
        } else if (e.getSoftFault()) {
            faultState.setText("Fault: Doors Stuck");
        } else {
            faultState.setText("Fault: -");
        }
    }
}
