package floor;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.List;

import floor.Floor.directionLampState;
import messages.FloorMetaMessage;
import messages.FloorRequestMessage;
import messages.Message;
import messages.ResponseTimeMessage;
import messages.ResponseTimeMessage.Subsystem;

public class DestinationSender implements Runnable {

	private int elevator;
	private int currentFloor;
	private List<Integer> passengers;
	private FloorSubsystem floorToUpdate;

	private DatagramSocket sendSocket;

	public DestinationSender(int c, int e, List<Integer> p, FloorSubsystem f) {
		elevator = e;
		passengers = p;
		currentFloor = c;
		floorToUpdate = f;

		try {
			sendSocket = new DatagramSocket();
		} catch (SocketException e1) {
			e1.printStackTrace();
			System.exit(1);
		}
	}

	@Override
	public void run() {
		DatagramPacket sendPacket;
		byte[] data;
		FloorMetaMessage f;
		for (int i = 0; i < passengers.size(); i++) {
			Integer passenger = passengers.get(i);
			// send one message to the elevator
			FloorRequestMessage m = new FloorRequestMessage();
			m.setCurrent(currentFloor);
			m.setDestination(passenger);
			m.setElevator(elevator);
			data = Message.serialize(m);

			sendPacket = new DatagramPacket(data, data.length, SimulationVars.elevatorAddresses[elevator], SimulationVars.elevatorPorts[elevator]);
			Message.send(sendSocket, sendPacket);

			// send 1 message to the floor expecting a passenger
			f = new FloorMetaMessage(false);
			f.setDestinationFloor(passenger);
			f.setElevator(elevator);
			f.setStartingFloor(currentFloor);
			data = Message.serialize(f);

			sendPacket = new DatagramPacket(data, data.length, SimulationVars.floorAddresses[passenger],
					SimulationVars.floorPorts[passenger]);
			
			Long initTime = System.nanoTime();
			Message.send(sendSocket, sendPacket);
			
			try {
				Thread.sleep(1000/SimulationVars.timeScalar);
			} catch (InterruptedException e) {
				
			}
		}
		floorToUpdate.getFloor().setDirectionLamp(elevator, directionLampState.IDLE);
		floorToUpdate.controller.updateFloor(floorToUpdate);
	}
}
