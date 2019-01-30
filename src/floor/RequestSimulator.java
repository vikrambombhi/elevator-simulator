package floor;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import messages.ElevatorRequestMessage;
import messages.ElevatorRequestMessage.Direction;
import messages.Message;

public class RequestSimulator implements Runnable{
	
	DatagramSocket sendSocket;
	DatagramPacket sendPacket;
	
	public RequestSimulator() {
		try {
			sendSocket = new DatagramSocket();
		} catch (SocketException e) {
			e.printStackTrace();
		    System.exit(1);
		}
	}
	
	@Override
	public void run() {
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		ElevatorRequestMessage m = new ElevatorRequestMessage();
		m.setDirection(Direction.UP);
		m.setFloor(1);
		byte[] data = Message.serialize(m);
		//inform the floor subsystem
		sendPacket = new DatagramPacket(data, data.length, SimulationVars.floorSystemAddress, SimulationVars.floorSystemPort);
		send(sendPacket);
		//inform the scheduler
		sendPacket = new DatagramPacket(data, data.length, SimulationVars.schedulerAddress, SimulationVars.schedulerPort);
		send(sendPacket);
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		m = new ElevatorRequestMessage();
		m.setDirection(Direction.UP);
		m.setFloor(2);
		data = Message.serialize(m);
		//inform the floor subsystem
		sendPacket = new DatagramPacket(data, data.length, SimulationVars.floorSystemAddress, SimulationVars.floorSystemPort);
		send(sendPacket);
		//inform the scheduler
		sendPacket = new DatagramPacket(data, data.length, SimulationVars.schedulerAddress, SimulationVars.schedulerPort);
		send(sendPacket);
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		m = new ElevatorRequestMessage();
		m.setDirection(Direction.DOWN);
		m.setFloor(3);
		data = Message.serialize(m);
		//inform the floor subsystem
		sendPacket = new DatagramPacket(data, data.length, SimulationVars.floorSystemAddress, SimulationVars.floorSystemPort);
		send(sendPacket);
		//inform the scheduler
		sendPacket = new DatagramPacket(data, data.length, SimulationVars.schedulerAddress, SimulationVars.schedulerPort);
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
