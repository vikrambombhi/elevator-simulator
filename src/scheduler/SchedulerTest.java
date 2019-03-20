package scheduler;

import org.junit.Test;

import elevator.Elevator;
import elevator.Elevator.State;
import elevator.ElevatorQueue;
import junit.framework.TestCase;
import messages.ElevatorRequestMessage;
import messages.ElevatorRequestMessage.Direction;

public class SchedulerTest extends TestCase {
	@Test
	public void testHandleUnresponsiveElevators() {
		SchedulerMessenger m = new SchedulerMessenger();
		Scheduler s = new Scheduler(m);
        FaultDetector detector = new FaultDetector(s, m);

		s.queuePickUp(Direction.UP, 3);

		ElevatorQueue q = s.getElevatorShaft(0).getQueue();
		ElevatorQueue q1 = s.getElevatorShaft(1).getQueue();
		ElevatorQueue q2 = s.getElevatorShaft(2).getQueue();

		assertEquals(3, q.peek());

		// hard fault on elevator 0
		long now = System.currentTimeMillis();
		// ensures it's at least 10 seconds in the past
		s.getElevatorShaft(0).setLastResponse(now - 11 * 1000);
		s.getElevatorShaft(1).setLastResponse(now + 5 * 1000);
		s.getElevatorShaft(2).setLastResponse(now + 5 * 1000);

		// simulate the elevator moving to be considered a hard fault
		Elevator e = new Elevator(0);
		e.setState(Elevator.State.MOVING_UP);
		s.getElevatorShaft(0).setElevator(e);

		detector.handleUnresponsiveElevators();

		// elevator 0's queue should be removed
		assertEquals(null, s.getElevatorShaft(0));

		// it's queue should be redistribute to another queue
		assertEquals(3, q1.peek());
		assertEquals(true, q2.isEmpty());

		// new requests shouldn't be assigned to the dead elevator
		s.queuePickUp(Direction.UP, 5);
		assertEquals(s.getElevatorShaft(0), null);
		assertEquals(3, q1.peek());
		assertEquals(5, q2.peek());

		m.close();
	}

	@Test
	public void testHandleUnresponsiveElevatorsSoft() {
		SchedulerMessenger m = new SchedulerMessenger();
		Scheduler s = new Scheduler(m);
        FaultDetector detector = new FaultDetector(s, m);

		s.queuePickUp(Direction.UP, 3);
		ElevatorQueue q = s.getElevatorShaft(0).getQueue();
		assertEquals(3, q.peek());

		// soft fault on elevator 0
		long now = System.currentTimeMillis();
		// ensures it's at least 10 seconds in the past
		s.getElevatorShaft(0).setLastResponse(now - 11 * 1000);
		s.getElevatorShaft(1).setLastResponse(now + 5 * 1000);
		s.getElevatorShaft(2).setLastResponse(now + 5 * 1000);

		// simulate the elevator stopped to be considered a soft fault
		Elevator e = new Elevator(0);
		e.setState(State.STOPPED_DOORS_CLOSED);
		s.getElevatorShaft(0).setElevator(e);

		// a soft fault is detected
		assert (detector.handleUnresponsiveElevators() == 2);

		// elevator 0's queue should NOT be removed
		assert (s.getElevatorShaft(0) != null);

		m.close();
	}
}
