package elevator;

import messages.*;
import floor.SimulationVars;
import junit.framework.TestCase;

public class ElevatorSubSystemTest extends TestCase {

    public void testElevatorMessageStateMachine() {
        ElevatorSubSystem subsystem = new ElevatorSubSystem(1);
        assertEquals(1, subsystem.getElevators().length);

        // Start off stopped and closed
        Elevator elevator = subsystem.getElevators()[0];
        assertEquals(Elevator.State.STOPPED_DOORS_CLOSED, elevator.getState());

        // Test GOUP
        subsystem.handleMessage(new ElevatorMessage(0, messages.ElevatorMessage.MessageType.GOUP));
        assertEquals(Elevator.State.MOVING_UP, elevator.getState());

        // Test STOP
        subsystem.handleMessage(new ElevatorMessage(0, messages.ElevatorMessage.MessageType.STOP));
        assertEquals(Elevator.State.STOPPED_DOORS_CLOSED, elevator.getState());
        elevator.setFloor(1);

        // Test GODOWN
        subsystem.handleMessage(new ElevatorMessage(0, messages.ElevatorMessage.MessageType.GODOWN));
        assertEquals(Elevator.State.MOVING_DOWN, elevator.getState());
    }
}
