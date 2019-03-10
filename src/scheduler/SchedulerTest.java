package scheduler;

import messages.ElevatorRequestMessage;
import messages.ElevatorRequestMessage.Direction;
import elevator.Elevator;
import elevator.Elevator.State;
import elevator.ElevatorQueue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import junit.framework.TestCase;

public class SchedulerTest extends TestCase {
	@Test
	public void testHandleUnresponsiveElevators() {
		Scheduler s = new Scheduler();
		s.handleElevatorRequest(new ElevatorRequestMessage(Direction.UP, 3));

		ElevatorQueue q = s.getQueue(0);
		ElevatorQueue q1 = s.getQueue(1);
		ElevatorQueue q2 = s.getQueue(2);

		assertEquals(3, q.peek());

		// hard fault on elevator 0
		long now = System.currentTimeMillis();
		// ensures it's at least 2 seconds in the past
		s.setLastResponses(0, now - 3 * 1000);
		s.setLastResponses(1, now + 10 * 1000);
		s.setLastResponses(2, now + 10 * 1000);

		// simulate the elevator moving to be considered a hard fault
		Elevator e = new Elevator(0);
		e.setState(Elevator.State.MOVING_UP);
		s.setElevator(0, e);

		s.handleUnresponsiveElevators();

		// elevator 0's queue should be removed
		assertEquals(null, s.getQueue(0));

		// it's queue should be redistribute to another queue
		assertEquals(3, q1.peek());
		assertEquals(true, q2.isEmpty());

		// new requests shouldn't be assigned to the dead elevator
		s.handleElevatorRequest(new ElevatorRequestMessage(Direction.UP, 5));
		assertEquals(s.getQueue(0), null);
		assertEquals(3, q1.peek());
		assertEquals(5, q2.peek());

		s.close();
	}

	@Test
	public void testHandleUnresponsiveElevatorsSoft() {
		Scheduler s = new Scheduler();
		s.handleElevatorRequest(new ElevatorRequestMessage(Direction.UP, 3));

		ElevatorQueue q = s.getQueue(0);

		assertEquals(3, q.peek());

		// soft fault on elevator 0
		long now = System.currentTimeMillis();
		// ensures it's at least 2 seconds in the past
		s.setLastResponses(0, now - 3 * 1000);
		s.setLastResponses(1, now + 10 * 1000);
		s.setLastResponses(2, now + 10 * 1000);

		// simulate the elevator stopped to be considered a soft fault
		Elevator e = new Elevator(0);
		e.setState(State.STOPPED_DOORS_CLOSED);
		s.setElevator(0, e);

		// a soft fault is detected
		assert (s.handleUnresponsiveElevators() == 2);

		// elevator 0's queue should NOT be removed
		assert (s.getQueue(0) != null);

		s.close();
	}
}
