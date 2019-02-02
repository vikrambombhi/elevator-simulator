package floor;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import messages.ElevatorRequestMessage;
import messages.ElevatorRequestMessage.Direction;
import messages.FloorMetaMessage;
import messages.Message;

public class RequestSimulator implements Runnable{

	private DatagramSocket sendSocket;
	private int floorNum;

	public RequestSimulator(int f) {
		try {
			sendSocket = new DatagramSocket();
		} catch (SocketException e) {
			e.printStackTrace();
		    System.exit(1);
		}

		floorNum = f;
	}

	@Override
	public void run() {

		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		//for the scheduler
		ElevatorRequestMessage m = new ElevatorRequestMessage();
		m.setDirection(Direction.UP);
		m.setOriginFloor(floorNum);
		byte[] data = Message.serialize(m);
		DatagramPacket sendPacket = new DatagramPacket(data, data.length, SimulationVars.schedulerAddress, SimulationVars.schedulerPort);
		Message.send(sendSocket, sendPacket);

		//for the origin floor
		FloorMetaMessage f = new FloorMetaMessage(true);
		if (floorNum == SimulationVars.numberOfFloors-1) {
			f.setDestinationFloor(0);
		} else {
			f.setDestinationFloor(SimulationVars.numberOfFloors-1);
		}
		f.setStartingFloor(floorNum);
		data = Message.serialize(f);
		sendPacket = new DatagramPacket(data, data.length, SimulationVars.floorAddresses[floorNum], SimulationVars.floorPorts[floorNum]);
		Message.send(sendSocket, sendPacket);
	}
}
