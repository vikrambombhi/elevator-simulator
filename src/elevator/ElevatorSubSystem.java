package elevator;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import messages.*;
import scheduler.Scheduler;

/*
 * ElevatorSubSystem is the subsystem placed in each elevator.
 */
public class ElevatorSubSystem {
	// State is the possible states that the elevator can be in.
	public static String HOST = "127.0.0.1";
	public static short PORT = 4000;

    // TODO: support more than 1 elevator
    private Elevator elevator;
	// Communication sockets
	private DatagramSocket sendSocket, receiveSocket;

	public ElevatorSubSystem() {
        elevator = new Elevator(0);
		try {
			sendSocket = new DatagramSocket();
			receiveSocket = new DatagramSocket(PORT);
		} catch (SocketException se) {
			se.printStackTrace();
			System.exit(1);
		}
	}

	/*
	 * run starts the elevator subsystem and awaits for messages.
	 */
	public void run() {
		try {
			while (true) {
				DatagramPacket receivePacket = Message.receive(receiveSocket);
				Message m = Message.deserialize(receivePacket.getData());
				handleMessage(m);
			}
		} catch (Exception e) {
			System.out.println("ElevatorSubSystem quiting.");
			e.printStackTrace();
			close();
		}
	}

	/*
	 * close closes the communication sockets of the elevator subsystem.
	 */
	public void close() {
		sendSocket.close();
		receiveSocket.close();
	}

	/*
	 * handleMessage runs the corresponding action for the message type.
	 */
	private void handleMessage(Message m) {
		// State machine switch
		if (m instanceof ElevatorMessage) {
			// scheduler tells the elevator to move, stop, open or close.
			elevator.handleElevatorMessage((ElevatorMessage) m);
		} else if (m instanceof FloorRequestMessage) {
            forwardFloorRequest((FloorRequestMessage) m);
        }
	}

    private void forwardFloorRequest(FloorRequestMessage m) {
        byte[] data = Message.serialize(m);
		InetAddress destHost = null;
		try {
			destHost = InetAddress.getByName(ElevatorSubSystem.HOST);
		} catch (UnknownHostException e) {
			e.printStackTrace();
			System.exit(1);
		}
        DatagramPacket sendPacket = new DatagramPacket(data, data.length, destHost, Scheduler.PORT);
        Message.send(sendSocket, sendPacket);
    }

    public static void main(String args[]) {
        System.out.println("ElevatorSubSystem: Starting on port 4000");
        ElevatorSubSystem e = new ElevatorSubSystem();
        e.run();
    }
}
