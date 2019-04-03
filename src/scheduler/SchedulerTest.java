package scheduler;

import org.junit.Test;

import elevator.Elevator;
import elevator.Elevator.State;
import floor.SimulationVars;
import elevator.ElevatorQueue;
import elevator.ElevatorSubSystem;
import junit.framework.TestCase;
import messages.ElevatorMessage;
import messages.ElevatorRequestMessage;
import messages.ElevatorRequestMessage.Direction;
import messages.FaultMessage;
import messages.ElevatorMessage.MessageType;

public class SchedulerTest extends TestCase {
	@Test
	public void testHandleUnresponsiveElevators() {
		//set up test
		ElevatorQueue queue = new ElevatorQueue();
		ElevatorQueue[] qs = new ElevatorQueue[1];
		qs[0] = queue;
        Thread c = new Thread(new QueueCleaner(qs));
		Thread s = new Thread(new Scheduler(0, queue));
        
        //add some test pickups for A
        queue.addPickUp(1, Direction.UP);
        queue.addPickUp(6, Direction.DOWN);
        queue.addPickUp(10, Direction.DOWN);
		
		assertTrue(queue.contains(1) && queue.contains(6) && queue.contains(10));
		
        c.start();
        s.start();

        //wait for the scheduler to deem a fault and the cleaner to  notice the fault empty the pickups 
        try {
        	Thread.sleep(60000/SimulationVars.timeScalar);
        } catch (InterruptedException e) {}
		
        //fault should be set on queue
        assertTrue(queue.getHardFaulted());
		//the queued pickups should be removed
		assertTrue(!queue.contains(1) && !queue.contains(6) && !queue.contains(10));
	}

	@Test
	public void testHandleUnresponsiveElevatorsSoft() {
		ElevatorQueue queue = new ElevatorQueue();
		ElevatorSubSystem elevator = new ElevatorSubSystem(0);
		Thread elevatorThread;
		Thread scheduler = new Thread(new Scheduler(0, queue));
		
		//scheduler starts
		scheduler.start();
		
		//simulated trip added
		synchronized(queue) {
			queue.addPickUp(5, Direction.UP);
			queue.notifyAll();
		}
		
		//scheduler will send an instruction 
        try {
        	Thread.sleep((long) (SimulationVars.elevatorTravelTime*2.5));
        } catch (InterruptedException e) {}
		
        //simulate the first instruction being ignored
		synchronized(queue) {
			queue.notifyAll();
		}
		
		//scheduler will send another instruction 
        try {
        	Thread.sleep(SimulationVars.elevatorTravelTime);
        } catch (InterruptedException e) {}
		
        //simulate instruction successfully carried out - soft fault recovered
		synchronized(queue) {
			queue.setElevatorPosition(1);
			queue.notifyAll();
		}
		
		assertTrue(!queue.getHardFaulted());
	}
}