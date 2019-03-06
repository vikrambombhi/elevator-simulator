package elevator;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import messages.*;
import scheduler.Scheduler;
import floor.ArrivalSensor;
import floor.SimulationVars;
import messages.ElevatorRequestMessage.Direction;

/*
 * ElevatorSubSystem is the subsystem placed in each elevator.
 */
public class ElevatorSubSystem implements Runnable {
	private Elevator elevator;

	//each elevator has an arrival sensor
	private Thread arrivalSensor;
	
	// Communication sockets
	private DatagramSocket sendSocket, receiveSocket;
	
	private boolean softFaultFlag, hardFaultFlag = false;

	public ElevatorSubSystem(int id) {
		elevator = new Elevator(id);
		try {
			sendSocket = new DatagramSocket();
			receiveSocket = new DatagramSocket(null);
			receiveSocket.setReuseAddress(true);
			receiveSocket.bind(new InetSocketAddress(SimulationVars.elevatorPorts[id]));
		} catch (SocketException se) {
			se.printStackTrace();
			System.exit(1);
		}
	}

	/*
	 * run starts the elevator subsystem and awaits for messages.
	 */
	@Override
	public void run() {
		int id = elevator.getId();
		System.out.printf("ElevatorSubSystem %d: Starting on port %d\n", id, SimulationVars.elevatorPorts[id]);
		try {
			while (!Thread.currentThread().isInterrupted()) {
				DatagramPacket receivePacket = Message.receive(receiveSocket);
				Message m = Message.deserialize(receivePacket.getData());
				handleMessage(m);
			}
		} catch (Exception e) {
			System.out.println("ElevatorSubSystem quiting.");
			e.printStackTrace();
		} finally {
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
	 * returns the elevator in the system. Used for tests.
	 */
	public Elevator getElevator() {
		return elevator;
	}

	/*
	 * handleMessage runs the corresponding action for the message type.
	 */
	public void handleMessage(Message m) {
		// State machine switch
		//ignore if softfaultFlag = true
		if (m instanceof ElevatorMessage && !softFaultFlag) {
			// scheduler tells the elevator to move, stop, open or close.
			
			elevator.handleElevatorMessage((ElevatorMessage) m);

			if (elevator.isMoving()) {
				sendTravelMessage();
			}
		} else if (m instanceof FloorRequestMessage) {
			forwardFloorRequest((FloorRequestMessage) m);
		} else if (m instanceof FloorArrivalMessage) {
			FloorArrivalMessage message = (FloorArrivalMessage) m;
			elevator.setFloor(message.getFloor());
		} else if (m instanceof FaultMessage) {
			if (((FaultMessage) m).getHardFault()) {
				hardFaultFlag = true;
			} else {
				softFaultFlag = true;
			}
		}
		//reset the soft fault flag
		softFaultFlag = false;
	}

	private void sendTravelMessage() {
		if (elevator.getState() == Elevator.State.MOVING_UP) {
			arrivalSensor = new Thread(new ArrivalSensor(elevator.getId(), elevator.getFloor(), elevator.getFloor()+1));
		} else {
			arrivalSensor = new Thread(new ArrivalSensor(elevator.getId(), elevator.getFloor(), elevator.getFloor()-1));
		}
		//if we arent simulating a hard fault
		if (!hardFaultFlag) {
			arrivalSensor.start();
		}
	}

	private void forwardFloorRequest(FloorRequestMessage m) {
		byte[] data = Message.serialize(m);
		DatagramPacket sendPacket = new DatagramPacket(data, data.length,
				SimulationVars.schedulerAddress, SimulationVars.schedulerPort);
		Message.send(sendSocket, sendPacket);
	}
}
