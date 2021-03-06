package elevator;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import messages.*;
import floor.SimulationVars;
import org.junit.After;
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


	@Test
	public void testElevatorCreation() {
		ElevatorManager manager = new ElevatorManager(3, null);
		assertEquals(3, manager.getElevatorSubsystems().length);
		
		for(int i = 0; i < 3; i++){
			manager.getElevatorSubsystems()[i].close();
		}
	}

	@Test
	public void testElevatorMessageStateMachine() {
		ElevatorManager manager = new ElevatorManager(1, null);
		ElevatorSubSystem subsystem = manager.getElevatorSubsystems()[0];

		// Start off stopped and closed
		Elevator elevator = subsystem.getElevator();
		assertEquals(Elevator.State.STOPPED_DOORS_CLOSED, subsystem.getElevator().getState());

		// Test GOUP
		subsystem.handleMessage(new ElevatorMessage(messages.ElevatorMessage.MessageType.GOUP));
		assertEquals(Elevator.State.MOVING_UP, subsystem.getElevator().getState());

		// Test STOP
		subsystem.handleMessage(new ElevatorMessage(messages.ElevatorMessage.MessageType.STOP));
		assertEquals(Elevator.State.STOPPED_DOORS_OPENED, subsystem.getElevator().getState());
		subsystem.getElevator().setFloor(1);

		// Test GODOWN
		subsystem.handleMessage(new ElevatorMessage(messages.ElevatorMessage.MessageType.GODOWN));
		assertEquals(Elevator.State.MOVING_DOWN, subsystem.getElevator().getState());
		
		subsystem.close();
	}

	@Test
	public void testElevatorMessageIsIndependent() {
		ElevatorManager manager = new ElevatorManager(3, null);
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
				SimulationVars.elevatorAddresses[2], SimulationVars.elevatorPorts[2]);
		Message.send(sendSocket, packet);

		try { Thread.sleep(500); } catch (InterruptedException e) { }
		
		assertEquals(Elevator.State.STOPPED_DOORS_CLOSED, elevatorSubSystems[0].getElevator().getState());
		assertEquals(Elevator.State.STOPPED_DOORS_CLOSED, elevatorSubSystems[1].getElevator().getState());
		assertEquals(Elevator.State.MOVING_UP, elevatorSubSystems[2].getElevator().getState());

		// Test only elevator 1 changes state
		elevatorSubSystems[1].getElevator().setFloor(1);
		data = Message.serialize(new ElevatorMessage(messages.ElevatorMessage.MessageType.GODOWN));
		packet = new DatagramPacket(data, data.length,
				SimulationVars.elevatorAddresses[1], SimulationVars.elevatorPorts[1]);
		Message.send(sendSocket, packet);

		try { Thread.sleep(500); } catch (InterruptedException e) { }

		assertEquals(Elevator.State.STOPPED_DOORS_CLOSED, elevatorSubSystems[0].getElevator().getState());
		assertEquals(Elevator.State.MOVING_DOWN, elevatorSubSystems[1].getElevator().getState());
		assertEquals(Elevator.State.MOVING_UP, elevatorSubSystems[2].getElevator().getState());
		
		TerminateMessage m = new TerminateMessage();
		
		for(int i = 0; i < 3; i++){
			data = Message.serialize(m);
			packet = new DatagramPacket(data, data.length,
			SimulationVars.elevatorAddresses[i], SimulationVars.elevatorPorts[i]);
			Message.send(sendSocket, packet);
		}
		
		try { Thread.sleep(1000); } catch (InterruptedException e) { }
		
		t.interrupt();
		manager.close();
	}

	@Test
	public void testFloorArrivalMessage() {
		ElevatorManager manager = new ElevatorManager(3, null);
		ElevatorSubSystem[] elevatorSubSystems = manager.getElevatorSubsystems();
		
		Thread t = new Thread(manager);
		t.start();



		FloorArrivalMessage fm = new FloorArrivalMessage();
		fm.setDirection(messages.ElevatorRequestMessage.Direction.UP);
		fm.setFloor(9);
		fm.setElevator(2);

		byte[] data = Message.serialize(fm);
		DatagramPacket packet = new DatagramPacket(data, data.length,
				SimulationVars.elevatorAddresses[2], SimulationVars.elevatorPorts[2]);
		Message.send(sendSocket, packet);

		try { Thread.sleep(500); } catch (InterruptedException e) { }

		// assert that floor arrival only effects the target elevator
		assertEquals(0, elevatorSubSystems[0].getElevator().getFloor());
		assertEquals(0, elevatorSubSystems[1].getElevator().getFloor());
		assertEquals(9, elevatorSubSystems[2].getElevator().getFloor());
		
		TerminateMessage m = new TerminateMessage();

		for(int i = 0; i < 3; i++){
			data = Message.serialize(m);
			packet = new DatagramPacket(data, data.length,
			SimulationVars.elevatorAddresses[i], SimulationVars.elevatorPorts[i]);
			Message.send(sendSocket, packet);
		}
		
		try { Thread.sleep(1000); } catch (InterruptedException e) { }
		
		t.interrupt();
		manager.close();
	}


	@After
	public void tearDown() {
		sendSocket.close();
	}
}
