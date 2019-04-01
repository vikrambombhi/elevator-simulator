package scheduler;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import elevator.ElevatorQueue;
import floor.SimulationVars;
import messages.ElevatorRequestMessage;
import messages.ElevatorRequestMessage.Direction;
import messages.Message;

public class QueueCleaner implements Runnable {
	
	private ElevatorQueue[] queues;
	private DatagramSocket sendSocket;
	
	public QueueCleaner(ElevatorQueue[] q) {
		queues = q;
		try {
			sendSocket = new DatagramSocket();
		} catch (SocketException se) {
			se.printStackTrace();
			System.exit(1);
		}
	}
	
	@Override
	public void run() {
		while (true) {
			for (int i = 0; i < queues.length; i++) {
				try {
					synchronized(queues[i]) {
						queues[i].wait((long)(SimulationVars.elevatorTravelTime*2));
					}
				} catch (InterruptedException e) {}
				if (queues[i].getHardFaulted()) {
					ElevatorRequestMessage m;
					DatagramPacket pack;
					byte[] data;
					while (queues[i].pickUpPeek() != -1) {
						Direction direction = queues[i].pickUpPeekDirection();
						int floor = queues[i].pickupPop();
						m = new ElevatorRequestMessage();
						m.setDirection(direction);
						m.setOriginFloor(floor);
						data = Message.serialize(m);
						pack = new DatagramPacket(data, data.length, SimulationVars.schedulerAddress, SimulationVars.schedulerPort);
						Message.send(sendSocket, pack);
					}
				}
			}
		}
	}

}
