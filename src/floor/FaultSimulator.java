package floor;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import messages.ElevatorRequestMessage;
import messages.FaultMessage;
import messages.FloorMetaMessage;
import messages.Message;

public class FaultSimulator implements Runnable {

	private DatagramSocket sendSocket;
	//will look for -1 to indicate the line in the file is signaling a fault
	private int faultIndicator = -1;
	private int time;
	private File file;
	
	public FaultSimulator() {
		try {
			sendSocket = new DatagramSocket();
		} catch (SocketException e) {
			e.printStackTrace();
		    System.exit(1);
		}

		time = -1;
		file = new File(SimulationVars.inputFile);
	}
	
	@Override
	public void run() {
		int nextRequest;
		FaultMessage m;
		byte[] faultData;
		DatagramPacket faultPacket;
		
		while ((nextRequest = file.getNextRequestTime(time, faultIndicator)) != -1) {
			//prep next messages
			m = file.getFaultMessage(nextRequest);
			faultData = Message.serialize(m);
			faultPacket = new DatagramPacket(faultData, faultData.length, SimulationVars.elevatorAddresses[m.getElevator()], SimulationVars.elevatorPorts[m.getElevator()]);
			
			//sleep until 
			try {
				Thread.sleep((nextRequest - time)/SimulationVars.timeScalar);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			time = nextRequest;
			
			//send messages
			Message.send(sendSocket, faultPacket);
		}
	}

}
