package floorSubSys;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class ArrivalSensor implements Runnable{
	
	DatagramPacket sendPacket, receivePacket;
	DatagramSocket sendReceiveSocket;
	
	Floor floor;
	   
	public ArrivalSensor(Floor f)
	{
		floor = f;
		try {
			sendReceiveSocket = new DatagramSocket();
		} catch (SocketException se) {   // Can't create the socket.
			se.printStackTrace();
			System.exit(1);
		}
	}

	@Override
	public void run() {
		//Send a hello message to the scheduler so it can be registered to receive arrival messages
		//TODO define a proper hello message
		byte msg[] = "Hello Server".getBytes();
		try {
			sendPacket = new DatagramPacket(msg, msg.length,
	                                        InetAddress.getLocalHost(), SimulationVars.schedulerPort);
		} catch (UnknownHostException e) {
			e.printStackTrace();
	        System.exit(1);
		}
		try {
			sendReceiveSocket.send(sendPacket);
	        System.out.println("Arrival Sensor: Sending hello message");
		} catch (IOException e) {
	        e.printStackTrace();
	        System.exit(1);
		}
		
		while(true) {
			//wait to receive message
			byte data[] = new byte[100];
			receivePacket = new DatagramPacket(data, data.length);
			try {
				sendReceiveSocket.receive(receivePacket);

			} catch (IOException e) {
		         e.printStackTrace();
		         System.exit(1);
			}
			
			System.out.println("Arrival Sensor: Packet recieved");
			//TODO parse message
				//respond with 'accept stop' if sensed elevator serves pending trip
					//change lamps, etc
				// respond with 'decline stop' if no stop needed
					//update direction lamp 
				//respond with another hello to a failed registration
		}
		
	}
}
