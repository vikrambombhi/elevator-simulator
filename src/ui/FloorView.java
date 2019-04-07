package ui;

import floor.FloorSubsystem;
import floor.SimulationVars;
import floor.Floor;
import floor.Floor.directionLampState;
import java.awt.*;
import javax.swing.*;

public class FloorView extends JPanel {
    private JLabel floor;
    private JLabel numberOfUpPassengers;
    private JLabel numberOfDownPassengers;
    private JLabel lamps;

    FloorView() {
        GridLayout grid = new GridLayout(2, 3);
        this.setLayout(grid);
        this.setBorder(BorderFactory.createLineBorder(Color.black));

        floor = new JLabel();
        this.add(floor);
        numberOfUpPassengers = new JLabel();
        this.add(numberOfUpPassengers);
        numberOfDownPassengers = new JLabel();
        this.add(numberOfDownPassengers);
        lamps = new JLabel();
        this.add(lamps);
    }

    public void render(FloorSubsystem f) {
        floor.setText("Floor: " + f.getFloorNum());
        numberOfUpPassengers.setText("Up Req #: " + f.getUpRequests().size());
        numberOfDownPassengers.setText("Down Req #: " + f.getDownRequests().size());
        renderLamps(f);
    }

    private void renderLamps(FloorSubsystem f) {
        String lampsString = "[";
        for (int i = 0; i < SimulationVars.numberOfElevators; i++) {
            if (i != 0) {
                lampsString += ", ";
            }
            directionLampState state = f.getFloor().getDirectionLamp(i);
            if (state == directionLampState.DOWN) {
                lampsString += "▼";
            } else if (state == directionLampState.UP) {
                lampsString += "▲";
            } else {
                lampsString += "-";
            }
        }
        lampsString += "]";
        lamps.setText("Lamps: " + lampsString);
    }
}
