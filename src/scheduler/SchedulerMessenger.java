package scheduler;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import elevator.Elevator;
import elevator.ElevatorQueue;
import elevator.ElevatorShaft;
import floor.SimulationVars;
import messages.ElevatorMessage;
import messages.ElevatorMessage.MessageType;
import messages.ElevatorRequestMessage;
import messages.ElevatorRequestMessage.Direction;
import messages.FloorArrivalMessage;
import messages.FloorRequestMessage;
import messages.Message;

public class SchedulerMessenger {
	public static String HOST = "127.0.0.1";
	public static short PORT = 3000;
	private DatagramSocket recvSock, sendSock;

	public SchedulerMessenger() {
		try {
			// Construct a datagram socket and bind it to port 3000
			// on the local host machine. This socket will be used to
			// receive UDP Datagram packets.
			recvSock = new DatagramSocket(PORT);
			sendSock = new DatagramSocket();

			// to test socket timeout (2 seconds)
			// receiveSocket.setSoTimeout(2000);
		} catch (SocketException se) {
			se.printStackTrace();
			System.exit(1);
		}
	}

	// receive continuously blocks and listens for packets from elevators or floors
	public Message receive() {
		return Message.deserialize(Message.receive(recvSock).getData());
	}

	public void sendToElevator(int currentFloor, int destination, ElevatorShaft shaft) {
		ElevatorMessage m = new ElevatorMessage(directElevatorTo(currentFloor, destination));
		int id = shaft.getElevator().getId();
		System.out.println("Scheduler: Sending message of type " + m.getMessageType() + "to Elevator " + id);

		byte[] data = Message.serialize(m);
		DatagramPacket pack = new DatagramPacket(data, data.length, SimulationVars.elevatorAddresses[id],
				SimulationVars.elevatorPorts[id]);
		Message.send(sendSock, pack);

		switch (m.getMessageType()) {
		case STOP:
			// this is a pick up or drop off.. notify floor
			FloorArrivalMessage fm = new FloorArrivalMessage();
			fm.setElevator(id);
			fm.setFloor(shaft.getElevator().getFloor());
			fm.setDirection(shaft.getQueue().getDirection());
			sendToFloor(fm);
		}
	}

	public void sendToFloor(FloorArrivalMessage m) {
		// peek at where this elevator is going next
		byte[] data = Message.serialize(m);
		DatagramPacket pack = new DatagramPacket(data, data.length, SimulationVars.floorAddresses[m.getFloor()],
				SimulationVars.floorPorts[m.getFloor()]);
		Message.send(sendSock, pack);
	}

	private MessageType directElevatorTo(int currentFloor, int toFloor) {
		if (currentFloor == toFloor) {
			return MessageType.STOP;
		}
		if (currentFloor - toFloor > 0) {
			return MessageType.GODOWN;
		}
		return MessageType.GOUP;
	}

	public void close() {
		recvSock.close();
		sendSock.close();
	}
}
