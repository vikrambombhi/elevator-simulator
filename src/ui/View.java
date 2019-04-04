package ui;

import javax.swing.*;

public class View extends JFrame {
	private ElevatorsView elevatorsView;

	View() {
		initGUI();

		elevatorsView = new ElevatorsView();
		this.add(elevatorsView);
	}

	private void initGUI() {
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setSize(300, 600);
	}

	public void display() {
		this.setVisible(true);
	}

    public ElevatorsView getElevatorsView() {
        return elevatorsView;
    }
}
