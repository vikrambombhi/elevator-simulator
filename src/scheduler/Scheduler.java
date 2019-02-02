package scheduler;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.LinkedList;

import messages.ArrivalMessage;
import messages.ElevatorMessage;
import messages.ElevatorMessage.MessageType;
import messages.ElevatorRequestMessage;
import messages.FloorRequestMessage;
import messages.Message;
import elevator.Elevator;

public class Scheduler {
	private DatagramSocket sock;
	private LinkedList<Integer> queue;

	public Scheduler() {
		try {
			// Construct a datagram socket and bind it to port 3000
			// on the local host machine. This socket will be used to
			// receive UDP Datagram packets.
			sock = new DatagramSocket(3000);

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
		Message m = Message.deserialize(Message.receive(sock).getData());
		if (m instanceof ElevatorRequestMessage) {
			// add request to elevator floor queue
			ElevatorRequestMessage erm = (ElevatorRequestMessage) m;
			queue.add(erm.getOriginFloor());
		} else if (m instanceof FloorRequestMessage) {
			// add request to pick up elevators
			FloorRequestMessage frm = (FloorRequestMessage) m;
			queue.add(frm.getFloor());
		} else if (m instanceof ArrivalMessage) {
			// this means that an elevator arrived at a floor, tell it what to
			// do next. Either open doors, or go up or down
			ArrivalMessage am = (ArrivalMessage) m;
			// tell the elevator where to go based on the queue

			// if am floor is queue.peek(), dequeue

			// create a new ElevatorMessage, and send it to the elevator
			// based on what floor it should go to next
			// sendToElevator(direction);
		}

	}

	private void sendToElevator(MessageType action) {
		byte[] data = Message.serialize((new ElevatorMessage(action)));
		InetAddress destHost = null;
		try {
			destHost = InetAddress.getByName(Elevator.HOST);
		} catch (UnknownHostException e) {
			e.printStackTrace();
			System.exit(1);
		}
		DatagramPacket pack = new DatagramPacket(data, data.length, destHost, Elevator.PORT);
		Message.send(sock, pack);
	}

	public void close() {
		sock.close();
	}

	public void run() {
		try {
			while (true) {
				schedule();
			}
		} catch (Exception e) {
			close();
		}
	}

	public static void main(String args[]) {
		System.out.println("Scheduler: Starting on port 3000");
		Scheduler proxy = new Scheduler();
		proxy.run();
	}
}
