package scheduler;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import messages.ArrivalMessage;
import messages.ElevatorRequestMessage;
import messages.FloorRequestMessage;
import messages.Message;

public class Scheduler {
	DatagramSocket sendAndReceive, receiveSocket;

	public Scheduler() {
		try {
			// Construct a datagram socket and bind it to any available
			// port on the local host machine. This socket will be used to
			// send UDP Datagram packets.
			sendAndReceive = new DatagramSocket();

			// Construct a datagram socket and bind it to port 23
			// on the local host machine. This socket will be used to
			// receive UDP Datagram packets.
			receiveSocket = new DatagramSocket(3000);

			// to test socket timeout (2 seconds)
			// receiveSocket.setSoTimeout(2000);
		} catch (SocketException se) {
			se.printStackTrace();
			System.exit(1);
		}
	}

	public DatagramPacket receive(byte[] data, DatagramSocket socket) {
		DatagramPacket receivePacket = new DatagramPacket(data, data.length);
		System.out.println("Scheduler: Waiting for Packet.\n");

		// Block until a datagram packet is received from receiveSocket.
		try {
			socket.receive(receivePacket);
		} catch (IOException e) {
			System.out.print("IO Exception: likely:");
			System.out.println("Receive Socket Timed Out.\n" + e);
			e.printStackTrace();
			System.exit(1);
		}

		return receivePacket;
	}

	public void send(DatagramPacket sendPacket) {
		System.out.println("Scheduler: Sending packet:");
		int len = sendPacket.getLength();
		String str = new String(sendPacket.getData(), 0, len);

		// Send the datagram packet to the client via the send socket.
		try {
			sendAndReceive.send(sendPacket);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	public void receiveAndForward() {
		byte data[] = new byte[100];
		DatagramPacket receivePacket = receive(data, receiveSocket);

		InetAddress originAddress = receivePacket.getAddress();
		int originPort = receivePacket.getPort();

		// create new data packet but change the address to the server
		DatagramPacket sendPacket = null;
		try {
			sendPacket = new DatagramPacket(data, receivePacket.getLength(),
					InetAddress.getLocalHost(), 4000);
		} catch (UnknownHostException e) {
			e.printStackTrace();
			System.exit(1);
		}

		// forward
		send(sendPacket);

		// get upstream response
		data = new byte[100];
		receive(data, sendAndReceive);

		sendPacket = new DatagramPacket(data, receivePacket.getLength(),
				originAddress, originPort);
		send(sendPacket);
	}

	public void schedule() {
		// continuously listen for packets from elevators or floors
		// handle them
		Message m = Message.deserialize(Message.receive(receiveSocket)
				.getData());
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
		sendAndReceive.close();
		receiveSocket.close();
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
