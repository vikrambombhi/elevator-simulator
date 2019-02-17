package floor;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import messages.FloorArrivalMessage;
import messages.ElevatorRequestMessage.Direction;
import messages.Message;

public class ArrivalSensor implements Runnable{
	
	private DatagramSocket sendSocket;
	
	private int elevatorShaft;
	private int startFloor;
	private int endFloor;

	public ArrivalSensor(int elevator, int startingFloor, int endingFloor) {
		elevatorShaft = elevator;
		startFloor = startingFloor;
		endFloor = endingFloor;
		
		try {
			sendSocket = new DatagramSocket();
		} catch (SocketException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	@Override
	public void run() {
		//make a message
		FloorArrivalMessage m = new FloorArrivalMessage();
		m.setElevator(elevatorShaft);
		m.setFloor(endFloor);
		if(endFloor > startFloor) {
			m.setDirection(Direction.UP);
		} else {
			m.setDirection(Direction.DOWN);
		}
		byte[] data = Message.serialize(m);

		//sleep until it the elevator 'arrives'
		try {
			Thread.sleep(SimulationVars.elevatorTravelTime);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		//tell the elevator it made it
		DatagramPacket sendPacket = new DatagramPacket(data, data.length, SimulationVars.elevatorAddresses[elevatorShaft], SimulationVars.elevatorPorts[elevatorShaft]);
		Message.send(sendSocket, sendPacket);
		//tell the scheduler the elevator made it
		sendPacket = new DatagramPacket(data, data.length, SimulationVars.schedulerAddress, SimulationVars.schedulerPort);
		Message.send(sendSocket, sendPacket);
	}
}
