package elevator;

import floor.SimulationVars;

public class ElevatorManager {

	private ElevatorSubSystem[] elevatorSubSystems;
	private Thread[] elevatorThreads;

	ElevatorManager(int numberOfElevators) {
		elevatorSubSystems = new ElevatorSubSystem[numberOfElevators];
		for (int i = 0; i < elevatorSubSystems.length; i ++) {
			elevatorSubSystems[i] = new ElevatorSubSystem(i);
		}
	}

	public ElevatorSubSystem[] getElevatorSubsystems() {
		return elevatorSubSystems;
	}

	public void run() {
		elevatorThreads = new Thread[elevatorSubSystems.length];
		for (int i = 0; i < elevatorSubSystems.length; i ++) {
			Thread t = new Thread(elevatorSubSystems[i]);
			t.start();
			elevatorThreads[i] = t;
		}
		for (Thread t : elevatorThreads) {
			try { t.join(); } catch (InterruptedException e) { }
		}
	}

	public void close() {
		for (int i = 0; i < elevatorSubSystems.length; i ++) {
			elevatorThreads[i].interrupt();
		}
	}

	public static void main(String args[]) {
		ElevatorManager em = new ElevatorManager(SimulationVars.numberOfElevators);
		em.run();
	}
}
