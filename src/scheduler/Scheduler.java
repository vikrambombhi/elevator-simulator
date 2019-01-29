package scheduler;

import java.net.DatagramSocket;
import java.net.SocketException;

import messages.ArrivalMessage;
import messages.ElevatorRequestMessage;
import messages.FloorRequestMessage;
import messages.Message;

public class Scheduler {
	private DatagramSocket sock;

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
	}

	public void schedule() {
		// continuously listen for packets from elevators or floors
		// handle them
		Message m = Message.deserialize(Message.receive(sock).getData());
		if (m instanceof ElevatorRequestMessage) {
			// add request to elevator floor queue
			ElevatorRequestMessage erm = (ElevatorRequestMessage) m;
		} else if (m instanceof FloorRequestMessage) {
			// add request to pick up elevators
			FloorRequestMessage frm = (FloorRequestMessage) m;
		} else if (m instanceof ArrivalMessage) {
			// this means that an elevator arrived at a floor, tell it what to
			// do next
			ArrivalMessage am = (ArrivalMessage) m;
		}

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
