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

	public RequestSimulator(int f) {
		try {
			sendSocket = new DatagramSocket();
		} catch (SocketException e) {
			e.printStackTrace();
		    System.exit(1);
		}

		floorNum = f;
		time = 0;
	}

	@Override
	public void run() {
		ElevatorRequestMessage m;
		FloorMetaMessage f;
		
		try (BufferedReader br = new BufferedReader(new FileReader(SimulationVars.inputFile))) {
			String line;
			while ((line = br.readLine()) != null) {
				String[] parts = line.split(" ");

				//our next message
				if (Integer.parseInt(parts[1]) == floorNum) {
					//make the messages
					m = new ElevatorRequestMessage();
					if (parts[2].equals("Up")) {
						m.setDirection(Direction.UP);
					} else {
						m.setDirection(Direction.DOWN);
					}
					m.setOriginFloor(floorNum);
					byte[] schedulerData = Message.serialize(m);
					DatagramPacket schedulerPacket = new DatagramPacket(schedulerData, schedulerData.length, SimulationVars.schedulerAddress, SimulationVars.schedulerPort);
					
					f = new FloorMetaMessage(true);
					f.setStartingFloor(floorNum);
					f.setDestinationFloor(Integer.parseInt(parts[3]));
					byte[] metaData = Message.serialize(f);
					DatagramPacket metaPacket = new DatagramPacket(metaData, metaData.length, SimulationVars.floorAddresses[floorNum], SimulationVars.floorPorts[floorNum]);
					
					//sleep until its time to send
					try {
						Thread.sleep(getSleepTime(parts[0]));
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					
					//send the messages
					System.out.println("hello");
					Message.send(sendSocket, schedulerPacket);
					Message.send(sendSocket, metaPacket);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public int getSleepTime(String t) {
		String[] parts = t.split(":");
		int sleepTime = 0;
		//add milliseconds
		sleepTime += Integer.parseInt(parts[2].substring(3));
		//add seconds
		sleepTime += (Integer.parseInt(parts[2].substring(0, 2))*1000);
		//add mins 
		sleepTime += (Integer.parseInt(parts[1])*60000);
		//add hours
		sleepTime += (Integer.parseInt(parts[0])*3600000);
		
		//time until we reach the time of request
		sleepTime = sleepTime - time;
		//when the thread wakes up from this message it will be.. time + time slept
		time = sleepTime + time;
		
		return sleepTime/SimulationVars.timeScalar;
	}
}
