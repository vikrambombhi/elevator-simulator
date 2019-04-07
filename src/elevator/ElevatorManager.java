package elevator;

import floor.SimulationVars;
import ui.Controller;

public class ElevatorManager implements Runnable{

	private ElevatorSubSystem[] elevatorSubSystems;
	private Thread[] elevatorThreads;

	public ElevatorManager(int numberOfElevators, Controller controller) {
		elevatorSubSystems = new ElevatorSubSystem[numberOfElevators];
		for (int i = 0; i < elevatorSubSystems.length; i++) {
			elevatorSubSystems[i] = new ElevatorSubSystem(i, controller);
		}
		elevatorThreads = new Thread[numberOfElevators];
	}

	public ElevatorSubSystem[] getElevatorSubsystems() {
		return elevatorSubSystems;
	}

	@Override
	public void run() {
		for (int i = 0; i < elevatorSubSystems.length; i++) {
			Thread t = new Thread(elevatorSubSystems[i]);
			t.start();
			elevatorThreads[i] = t;
		}
		for (Thread t : elevatorThreads) {
			try {
				t.join();
			} catch (InterruptedException e) {
			}
		}
	}

	public void close() {
		for (int i = 0; i < elevatorSubSystems.length; i++) {
			elevatorThreads[i].interrupt();
		}
	}
}
