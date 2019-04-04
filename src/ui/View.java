package ui;

import javax.swing.*;
import java.awt.*;

public class View extends JFrame {
	private ElevatorsView elevatorsView;
	private FloorsView floorsView;

	View() {
		initGUI();

		elevatorsView = new ElevatorsView();
		this.add(elevatorsView);
		floorsView = new FloorsView();
		this.add(floorsView);
	}

	private void initGUI() {
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setSize(1000, 1000);
		this.setLayout(new GridLayout(1, 2));
	}

	public void display() {
		this.setVisible(true);
	}

    public ElevatorsView getElevatorsView() {
        return elevatorsView;
    }

    public FloorsView getFloorsView() {
        return floorsView;
    }
}
