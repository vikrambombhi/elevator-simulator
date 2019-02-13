package elevator;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import messages.*;
import floor.SimulationVars;
import org.junit.Before;
import org.junit.Test;
import junit.framework.TestCase;

public class ElevatorManagerTest extends TestCase {

	private DatagramSocket sendSocket;

    @Before
	public void setUp() {
		try {
			sendSocket = new DatagramSocket();
		} catch (SocketException se) {
			se.printStackTrace();
			System.exit(1);
		}
	}

	public void tearDown() {
		sendSocket.close();
	}

    @Test
    public void testElevatorManagerElevatorsCreated() {
        ElevatorManager manager = new ElevatorManager(9);
        assertEquals(9, manager.getElevatorSubsystems().length);
    }

    @Test
    public void testElevatorMessageStateMachine() {
        ElevatorManager manager = new ElevatorManager(1);
		ElevatorSubSystem subsystem = manager.getElevatorSubsystems()[0];

        // Start off stopped and closed
        Elevator elevator = subsystem.getElevator();
        assertEquals(Elevator.State.STOPPED_DOORS_CLOSED, subsystem.getElevator().getState());

        // Test GOUP
        subsystem.handleMessage(new ElevatorMessage(messages.ElevatorMessage.MessageType.GOUP));
        assertEquals(Elevator.State.MOVING_UP, subsystem.getElevator().getState());

        // Test STOP
        subsystem.handleMessage(new ElevatorMessage(messages.ElevatorMessage.MessageType.STOP));
        assertEquals(Elevator.State.STOPPED_DOORS_CLOSED, subsystem.getElevator().getState());
        subsystem.getElevator().setFloor(1);

        // Test GODOWN
        subsystem.handleMessage(new ElevatorMessage(messages.ElevatorMessage.MessageType.GODOWN));
        assertEquals(Elevator.State.MOVING_DOWN, subsystem.getElevator().getState());
    }

    @Test
    public void testElevatorMessageIsIndependent() {
        ElevatorManager manager = new ElevatorManager(5);
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				manager.run();
			}
		});
		t.start();

        ElevatorSubSystem[] elevatorSubSystems = manager.getElevatorSubsystems();

        // Test only elevator 3 changes state
		byte[] data = Message.serialize(new ElevatorMessage(messages.ElevatorMessage.MessageType.GOUP));
		DatagramPacket packet = new DatagramPacket(data, data.length,
				SimulationVars.elevatorAddresses[3], SimulationVars.elevatorPorts[3]);
        Message.send(sendSocket, packet);

		try { Thread.sleep(500); } catch (InterruptedException e) { }

        assertEquals(Elevator.State.STOPPED_DOORS_CLOSED, elevatorSubSystems[0].getElevator().getState());
        assertEquals(Elevator.State.STOPPED_DOORS_CLOSED, elevatorSubSystems[1].getElevator().getState());
        assertEquals(Elevator.State.STOPPED_DOORS_CLOSED, elevatorSubSystems[2].getElevator().getState());
        assertEquals(Elevator.State.MOVING_UP, elevatorSubSystems[3].getElevator().getState());
        assertEquals(Elevator.State.STOPPED_DOORS_CLOSED, elevatorSubSystems[4].getElevator().getState());

        // Test only elevator 1 changes state
        elevatorSubSystems[1].getElevator().setFloor(1);
		data = Message.serialize(new ElevatorMessage(messages.ElevatorMessage.MessageType.GODOWN));
		packet = new DatagramPacket(data, data.length,
				SimulationVars.elevatorAddresses[1], SimulationVars.elevatorPorts[1]);
        Message.send(sendSocket, packet);

		try { Thread.sleep(500); } catch (InterruptedException e) { }

        assertEquals(Elevator.State.STOPPED_DOORS_CLOSED, elevatorSubSystems[0].getElevator().getState());
        assertEquals(Elevator.State.MOVING_DOWN, elevatorSubSystems[1].getElevator().getState());
        assertEquals(Elevator.State.STOPPED_DOORS_CLOSED, elevatorSubSystems[2].getElevator().getState());
        assertEquals(Elevator.State.MOVING_UP, elevatorSubSystems[3].getElevator().getState());
        assertEquals(Elevator.State.STOPPED_DOORS_CLOSED, elevatorSubSystems[4].getElevator().getState());
    }

    @Test
    public void testFloorArrivalMessage() {
        ElevatorManager manager = new ElevatorManager(3);
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				manager.run();
			}
		});
		t.start();

        ElevatorSubSystem[] elevatorSubSystems = manager.getElevatorSubsystems();

        FloorArrivalMessage fm = new FloorArrivalMessage();
        fm.setDirection(messages.ElevatorRequestMessage.Direction.UP);
        fm.setFloor(9);
        fm.setElevator(2);

		byte[] data = Message.serialize(fm);
		DatagramPacket packet = new DatagramPacket(data, data.length,
				SimulationVars.elevatorAddresses[1], SimulationVars.elevatorPorts[1]);
        Message.send(sendSocket, packet);

		try { Thread.sleep(500); } catch (InterruptedException e) { }

        // assert that floor arrival only effects the target elevator
        assertEquals(0, elevatorSubSystems[0].getElevator().getFloor());
        assertEquals(0, elevatorSubSystems[1].getElevator().getFloor());
        assertEquals(9, elevatorSubSystems[2].getElevator().getFloor());
    }
}
