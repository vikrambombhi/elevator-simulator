package scheduler;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.LinkedList;

import elevator.ElevatorSubSystem;
import messages.ElevatorMessage;
import messages.ElevatorMessage.MessageType;
import messages.ElevatorRequestMessage;
import messages.FloorArrivalMessage;
import messages.FloorRequestMessage;
import messages.Message;

public class Scheduler {
	public static String HOST = "127.0.0.1";
	public static short PORT = 3000;

	private DatagramSocket recvSock, sendSock;
	private LinkedList<Integer> queue;

	public Scheduler() {
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
		queue = new LinkedList<Integer>();
	}

	public void schedule() {
		// continuously listen for packets from elevators or floors
		// handle them
		System.out.println("Scheduler: Waiting for message");
		Message m = Message.deserialize(Message.receive(recvSock).getData());
		if (m instanceof ElevatorRequestMessage) {
			// add request to pick up elevators
			ElevatorRequestMessage erm = (ElevatorRequestMessage) m;
			queue.add(erm.getOriginFloor());
			System.out.println("Scheduler: New queue: " + queue.toString());
		} else if (m instanceof FloorArrivalMessage) {
			// when an elevator arrives, tell it to go up or down, depending on the queue
			FloorArrivalMessage FAM = (FloorArrivalMessage) m;
			// BRUH FAM
			// tell elevator to go up, down, or stop & open
			sendToElevator(directElevatorTo(FAM.getFloor(), queue.peek()));
		} else if (m instanceof FloorRequestMessage) {
			// this means that an elevator is leaving a floor
			// we know what floor buttons were pressed
			FloorRequestMessage frm = (FloorRequestMessage) m;
			// enqueue requested floors
			for (int floor : frm.getDestinations()) {
				queue.add(floor);
				System.out.println("Scheduler: New queue: " + queue.toString());
			}
			// send the elevator on its way
			sendToElevator(directElevatorTo(frm.getCurrent(), queue.peek()));
		}
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

	private void sendToElevator(MessageType action) {
		System.out.println("Scheduler: Sending message of type " + action);
		byte[] data = Message.serialize((new ElevatorMessage(action)));
		InetAddress destHost = null;
		try {
			destHost = InetAddress.getByName(ElevatorSubSystem.HOST);
		} catch (UnknownHostException e) {
			e.printStackTrace();
			System.exit(1);
		}
		DatagramPacket pack = new DatagramPacket(data, data.length, destHost, ElevatorSubSystem.PORT);
		Message.send(sendSock, pack);
	}

	public void close() {
		recvSock.close();
		sendSock.close();
	}

	public void run() {
		try {
			while (true) {
				// sendToElevator(MessageType.GOUP);
				schedule();
			}
		} catch (Exception e) {
			e.printStackTrace();
			close();
		}
	}

	public static void main(String args[]) {
		System.out.println("Scheduler: Starting on port 3000");
		Scheduler sched = new Scheduler();
		sched.run();
	}
}
