package floorSubSys;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class Floor{
	
	//each floor knows it's floor number
	int floorNumber;
	
	//Lamps that light if its respective request is pending
	boolean upLamp;
	boolean downLamp;
	
	boolean isTopFloor;
	boolean isBottomFloor;
	
	//each elevator shaft has a direction lamp 
	// 0 = elevator in shaft moving up
	// 1 = elevator in shaft moving down
	// 2 = elevator shaft is idle
	int[] directionLamps;
	
	//Each floor will need to send requests for elevators
	DatagramPacket sendRequestPacket, receiveRequestPacket;
	DatagramSocket requestSocket;
	
	//Each floor needs an ArrivalSensor ...
	Thread arrivalSensor;
	
	public Floor(int numElevators, int floorNum, boolean isTop, boolean isBot) {
		floorNumber = floorNum;
		upLamp = false;
		downLamp = false;
		isTopFloor = isTop;
		isBottomFloor = isBot;
		directionLamps = new int[numElevators];
		for (int i = 0; i < numElevators; i++) {
			//initialize all shafts as idle
			directionLamps[i] = 2;
		}
		
		try {
			requestSocket = new DatagramSocket();
	          
	       } catch (SocketException se) {
	          se.printStackTrace();
	          System.exit(1);
	       } 
		
		arrivalSensor = new Thread(new ArrivalSensor(this));
		arrivalSensor.start();
	}
	
	public void sendRequest(String request) {
		//TODO create valid message
		byte msg[] = request.getBytes();
		
		//create a request message
		try {
	        sendRequestPacket = new DatagramPacket(msg, msg.length,
	                                        InetAddress.getLocalHost(), SimulationVars.schedulerPort);
	     } catch (UnknownHostException e) {
	        e.printStackTrace();
	        System.exit(1);
	     }
		 
		//send the request message
		try {
	        requestSocket.send(sendRequestPacket);

	     } catch (IOException e) {
	        e.printStackTrace();
	        System.exit(1);
	     }
		 System.out.println("Floor: Sending elevator request");
	     //TODO check to see if request response OK - future iteration
	}
}
