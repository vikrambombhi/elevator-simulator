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
		if (m instanceof FloorRequestMessage) {
			// add request to pick up elevators
			FloorRequestMessage frm = (FloorRequestMessage) m;
			queue.add(frm.getFloor());
			System.out.println("New queue: " + queue.toString());
		} else if (m instanceof FloorArrivalMessage) {
			// this means that an elevator arrived at a floor, tell it what to
			// do next. Either open doors, or go up or down
			// this also means that the elevator's doors are now closing, and there
			// could be floors to enqueue
			FloorArrivalMessage am = (FloorArrivalMessage) m;
			// tell the elevator where to go based on the queue
			for (int floor : am.getDestinations()) {
				queue.add(floor);
				System.out.println("New queue: " + queue.toString());
			}

			// if am floor is queue.peek(), dequeue and tell the elevator
			// to stop and open doors
			if (am.getFloor() == queue.peek()) {
				queue.remove();
				sendToElevator(MessageType.STOP);
				return;
			}

			// create a new ElevatorMessage, and send it to the elevator
			// based on what floor it should go to next
			// sendToElevator(direction);
			if (am.getFloor() - queue.peek() > 0) {
				// go down
				sendToElevator(MessageType.GODOWN);
			} else {
				// go up
				sendToElevator(MessageType.GOUP);
			}
		}

	}

	private void sendToElevator(MessageType action) {
		System.out.println("Sending message of type " + action);
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
