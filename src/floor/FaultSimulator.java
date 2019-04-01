package floor;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import messages.ElevatorRequestMessage;
import messages.FaultMessage;
import messages.FloorMetaMessage;
import messages.Message;
import messages.TerminateMessage;

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
		FaultMessage m = null;
		TerminateMessage t = null;
		byte[] faultData;
		byte[] terminateData;
		DatagramPacket faultPacket = null;
		DatagramPacket[] terminatePackets = new DatagramPacket[2 + SimulationVars.numberOfElevators + SimulationVars.numberOfFloors];
		
		while ((nextRequest = file.getNextRequestTime(time, faultIndicator)) != -1) {
			
			if (file.getTerminateMessage(nextRequest) == null) {
				m = file.getFaultMessage(nextRequest);
				faultData = Message.serialize(m);
				faultPacket = new DatagramPacket(faultData, faultData.length, SimulationVars.elevatorAddresses[m.getElevator()], SimulationVars.elevatorPorts[m.getElevator()]);
			} else {
				t = file.getTerminateMessage(nextRequest);
				terminateData = Message.serialize(t);
				for (int i = 0; i < SimulationVars.numberOfElevators; i++) {
					terminatePackets[i] = new DatagramPacket(terminateData, terminateData.length, SimulationVars.elevatorAddresses[i], SimulationVars.elevatorPorts[i]);
				}
				for (int i = 0; i < SimulationVars.numberOfFloors; i++) {
					terminatePackets[i+SimulationVars.numberOfElevators] = new DatagramPacket(terminateData, terminateData.length, SimulationVars.floorAddresses[i], SimulationVars.floorPorts[i]);
				}
				terminatePackets[SimulationVars.numberOfElevators+SimulationVars.numberOfFloors] = new DatagramPacket(terminateData, terminateData.length, SimulationVars.schedulerAddress, SimulationVars.schedulerPort);
				terminatePackets[SimulationVars.numberOfElevators+SimulationVars.numberOfFloors+1] = new DatagramPacket(terminateData, terminateData.length, SimulationVars.floorSystemAddress, SimulationVars.timerPort);
			}
			//sleep until 
			try {
				Thread.sleep((nextRequest - time)/SimulationVars.timeScalar);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			time = nextRequest;
			
			//send messages
			if (t == null) {
				Message.send(sendSocket, faultPacket);
			} else {
				for (int i = 0; i < terminatePackets.length; i++) {
					Message.send(sendSocket, terminatePackets[i]);
				}
			}
		}
	}

}
