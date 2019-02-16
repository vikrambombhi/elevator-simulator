package floor;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import messages.ElevatorRequestMessage;
import messages.ElevatorRequestMessage.Direction;
import messages.FloorMetaMessage;
import messages.Message;

public class RequestSimulator implements Runnable{

	private DatagramSocket sendSocket;
	private int floorNum;
	private int time;
	private File file;

	public RequestSimulator(int f) {
		try {
			sendSocket = new DatagramSocket();
		} catch (SocketException e) {
			e.printStackTrace();
		    System.exit(1);
		}

		floorNum = f;
		time = -1;
		file = new File(SimulationVars.inputFile);
	}

	@Override	
	public void run() {
		int nextRequest;
		ElevatorRequestMessage m;
		FloorMetaMessage f;
		byte[] schedulerData;
		byte[] floorData;
		DatagramPacket schedulerPacket;
		DatagramPacket floorPacket;
		
		while ((nextRequest = file.getNextRequestTime(time, floorNum)) != -1) {
			//prep next messages
			f = file.getNextMetaMessage(nextRequest, floorNum);
			floorData = Message.serialize(f);
			floorPacket = new DatagramPacket(floorData, floorData.length, SimulationVars.floorAddresses[floorNum], SimulationVars.floorPorts[floorNum]);
			
			m = file.getRequestMessage(nextRequest, floorNum);
			schedulerData = Message.serialize(m);
			schedulerPacket = new DatagramPacket(schedulerData, schedulerData.length, SimulationVars.schedulerAddress, SimulationVars.schedulerPort);
			
			//sleep until 
			try {
				Thread.sleep((nextRequest - time)/SimulationVars.timeScalar);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			time = nextRequest;
			
			//send messages
			Message.send(sendSocket, floorPacket);
			Message.send(sendSocket, schedulerPacket);
		}
	}
}
