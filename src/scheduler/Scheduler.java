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
import floor.SimulationVars;

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
			return;
		} else if (m instanceof FloorArrivalMessage) {
			if (queue.isEmpty()) {
				return;
			}
			// when an elevator arrives, tell it to go up or down, depending on the queue
			FloorArrivalMessage FAM = (FloorArrivalMessage) m;
			System.out.println("Scheduler: elevator arrived at floor " + FAM.getFloor());
			// BRUH FAM
			// tell elevator to go up, down, or stop & open
			int destination = queue.peek();
			if (destination == FAM.getFloor()) {
				System.out.println("Scheduler: Dequeueing floor " + destination);
				queue.remove();
			}
			if (queue.isEmpty()) {
				return;
			}
			sendToElevator(directElevatorTo(FAM.getFloor(), queue.peek()));
			return;
		} else if (m instanceof FloorRequestMessage) {
			// this means that an elevator is leaving a floor
			// we know what floor buttons were pressed
			FloorRequestMessage frm = (FloorRequestMessage) m;
			if (!queue.isEmpty()) {
				int destination = queue.peek();
				if (destination == frm.getCurrent()) {
					System.out.println("Scheduler: Dequeueing floor " + destination);
					queue.remove();
				}
			}
			// enqueue requested floors
			queue.add(frm.getDestination());
			System.out.println("Scheduler: New queue: " + queue.toString());

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
		// TODO set the correct target elevator in the message
		int targetElevator = 0;
		byte[] data = Message.serialize((new ElevatorMessage(action)));
		DatagramPacket pack = new DatagramPacket(data, data.length,
				SimulationVars.elevatorAddresses[targetElevator], SimulationVars.elevatorPorts[targetElevator]);
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
