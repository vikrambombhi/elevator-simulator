package scheduler;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.LinkedList;

import elevator.Elevator;
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
	private LinkedList<Integer>[] queues;
	private Elevator[] elevators;

	public Scheduler() {
		try {
			// Construct a datagram socket and bind it to port 3000
			// on the local host machine. This socket will be used to
			// receive UDP Datagram packets.
			recvSock = new DatagramSocket(PORT);
			sendSock = new DatagramSocket();

			elevators = new Elevator[SimulationVars.numberOfElevators];
			for (int i = 0; i < SimulationVars.numberOfElevators; i++) {
				elevators[i] = new Elevator(i);
			}

			// to test socket timeout (2 seconds)
			// receiveSocket.setSoTimeout(2000);
		} catch (SocketException se) {
			se.printStackTrace();
			System.exit(1);
		}
		queues = new LinkedList[SimulationVars.numberOfElevators];
		for (int i = 0; i < SimulationVars.numberOfElevators; i++) {
			queues[i] = new LinkedList<Integer>();
		}
	}

	public void schedule() {
		// continuously listen for packets from elevators or floors
		// handle them
		System.out.println("Scheduler: Waiting for message");
		Message m = Message.deserialize(Message.receive(recvSock).getData());
		if (m instanceof ElevatorRequestMessage) {
			handleElevatorRequest((ElevatorRequestMessage) m);
		} else if (m instanceof FloorArrivalMessage) {
			handleFloorArrival((FloorArrivalMessage) m);
		} else if (m instanceof FloorRequestMessage) {
			handleFloorRequest((FloorRequestMessage) m);
		}
	}

	private void handleElevatorRequest(ElevatorRequestMessage m) {
		// add request to pick up elevators
		queues[0].add(m.getOriginFloor());
		System.out.println("Scheduler: New queues: " + queues[0].toString());
	}

	private void handleFloorArrival(FloorArrivalMessage m) {
		// update elevator model
		elevators[m.getElevator()].setFloor(m.getFloor());

		if (queues[0].isEmpty()) {
			return;
		}
		// when an elevator arrives, tell it to go up or down, depending on the queues
		System.out.println("Scheduler: elevator arrived at floor " + m.getFloor());
		// tell elevator to go up, down, or stop & open
		int destination = queues[0].peek();
		if (destination == m.getFloor()) {
			System.out.println("Scheduler: Dequeuesing floor " + destination);
			queues[0].remove();
		}
		if (queues[0].isEmpty()) {
			return;
		}
		sendToElevator(directElevatorTo(m.getFloor(), queues[0].peek()));
		return;
	}

	private void handleFloorRequest(FloorRequestMessage m) {
		// this means that an elevator is leaving a floor
		// we know what floor buttons were pressed
		if (!queues[0].isEmpty()) {
			int destination = queues[0].peek();
			if (destination == m.getCurrent()) {
				System.out.println("Scheduler: Dequeuesing floor " + destination);
				queues[0].remove();
			}
		}
		// enqueues requested floors
		queues[0].add(m.getDestination());
		System.out.println("Scheduler: New queues: " + queues[0].toString());

		// send the elevator on its way
		sendToElevator(directElevatorTo(m.getCurrent(), queues[0].peek()));
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

		switch(action) {
			case GOUP:
				elevators[targetElevator].setState(Elevator.State.MOVING_UP);
				break;
			case GODOWN:
				elevators[targetElevator].setState(Elevator.State.MOVING_DOWN);
				break;
			// TODO is it okay to leave to ignore stop state? can we just assumed after it's stopped,
			// the elevator will continue in the current direction?
		}
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
