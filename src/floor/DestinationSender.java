package floor;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.List;

import messages.FloorMetaMessage;
import messages.FloorRequestMessage;
import messages.Message;

public class DestinationSender implements Runnable {

	private int elevator;
	private int currentFloor;
	private List<Integer> passengers;

	private DatagramSocket sendSocket;

	public DestinationSender(int c, int e, List<Integer> p) {
		elevator = e;
		passengers = p;
		currentFloor = c;

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
			Integer passenger = passengers.remove(0);

			// send one message to the elevator

			FloorRequestMessage m = new FloorRequestMessage();
			m.setCurrent(currentFloor);
			m.setDestination(passenger);
			data = Message.serialize(m);

			sendPacket = new DatagramPacket(data, data.length, SimulationVars.elevatorAddresses[elevator],
					SimulationVars.elevatorPorts[elevator]);
			// send one message to the elevator
			m = new FloorRequestMessage();
			m.setCurrent(currentFloor);
			m.setDestination(passenger);
			data = Message.serialize(m);

			sendPacket = new DatagramPacket(data, data.length, SimulationVars.elevatorAddresses[elevator],
					SimulationVars.elevatorPorts[elevator]);
			Message.send(sendSocket, sendPacket);

			// send 1 message to the floor expecting a passenger
			f = new FloorMetaMessage(false);
			f.setDestinationFloor(passenger);
			f.setElevator(elevator);
			f.setStartingFloor(currentFloor);
			data = Message.serialize(f);

			sendPacket = new DatagramPacket(data, data.length, SimulationVars.floorAddresses[passenger],
					SimulationVars.floorPorts[passenger]);
			Message.send(sendSocket, sendPacket);

			// small sleep so things don't get too spicy
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
	}
}
