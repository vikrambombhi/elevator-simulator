package floor;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import messages.ElevatorRequestMessage.Direction;
import messages.FloorRequestMessage;
import messages.Message;

public class TripTracker {

	DatagramSocket sendSocket;
	DatagramPacket sendPacket;
	
	public TripTracker() {
		try {
			sendSocket = new DatagramSocket();
		} catch (SocketException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	//facade for now
	public void addTrip(int tripStarting, Direction tripDirection) {
		
	}
	
	//facade for now... just pretends the destination is one floor in the direction the passenger chose
	public int getTripDestination(int tripStart, Direction tripDirection) {
		int tripDest = tripStart;
		if (tripDirection == Direction.UP) {
			tripDest++;
		} else {
			tripDest--;
		}
		return tripDest;
	}
	
	public void sendTripDestination(int elevator, int tripStart, Direction tripDirection) {
		int tripDest = getTripDestination(tripStart, tripDirection);
		FloorRequestMessage m = new FloorRequestMessage();
		m.setCurrent(tripStart);
		m.setFloor(tripDest);
		byte[] data = Message.serialize(m);
		sendPacket = new DatagramPacket(data, data.length, SimulationVars.elevatorAddresses[elevator], SimulationVars.elevatorPorts[elevator]);
		send(sendPacket);
	}
	
	private void send(DatagramPacket sendPacket) {
		// Send the datagram packet to the client via the send socket.
		try {
			sendSocket.send(sendPacket);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
}
