package elevator;

import messages.*;
import floor.SimulationVars;
import junit.framework.TestCase;

public class ElevatorSubSystemTest extends TestCase {

    public void testElevatorSubSystemElevatorsCreated() {
        ElevatorSubSystem subsystem = new ElevatorSubSystem(9);
        assertEquals(9, subsystem.getElevators().length);
    }

    public void testElevatorMessageStateMachine() {
        ElevatorSubSystem subsystem = new ElevatorSubSystem(1);

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

    public void testFloorArrivalMessage() {
        ElevatorSubSystem subsystem = new ElevatorSubSystem(3);
        Elevator[] elevators = subsystem.getElevators();

        FloorArrivalMessage fm = new FloorArrivalMessage();
        fm.setDirection(messages.ElevatorMessage.MessageType.GOUP);
        fm.setFloor(9);
        fm.setElevator(2);

        subsystem.handleMessage(fm);

        // assert that floor arrival only effects the target elevator
        assertEquals(0, elevators[0].getFloor());
        assertEquals(0, elevators[1].getFloor());
        assertEquals(9, elevators[2].getFloor());
    }
}
