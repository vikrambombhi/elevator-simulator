package elevator;

import elevator.Elevator;
import elevator.ElevatorQueue;

public class ElevatorShaft {
	private ElevatorQueue queue;
	private Elevator elevator;

	private int currentDestination;
	private long lastResponse;

	public ElevatorShaft(int id) {
		queue = new ElevatorQueue();
		elevator = new Elevator(id, null);
		lastResponse = System.currentTimeMillis();
	}

	/**
	 * @return the CurrentDestination
	 */
	public int getCurrentDestination() {
		return currentDestination;
	}

	/**
	 * @param CurrentDestination the CurrentDestination to set
	 */
	public void setCurrentDestination(int destination) {
		currentDestination = destination;
	}

	public long getLastResponse() {
		return lastResponse;
	}

	// Use this to mock last responses
	public void setLastResponse(long value) {
		lastResponse = value;
	}

	// Get queue to evaluate in tests
	public ElevatorQueue getQueue() {
		return queue;
	}

	public Elevator getElevator() {
		return elevator;
	}

	public void setElevator(Elevator e) {
		elevator = e;
	}
}
