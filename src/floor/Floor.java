package floor;

import java.io.*;
import java.net.*;


public class Floor {
	
	DatagramSocket sendReceiveSocket, receiveSocket;
	
	public Floor () {
		
		try {
			// Construct a datagram socket and bind it to any available
			// port on the local host machine. This socket will be used to
			// send UDP Datagram packets.
			sendReceiveSocket = new DatagramSocket();

			// Construct a datagram socket and bind it to port 4000
			// on the local host machine. This socket will be used to
			// receive UDP Datagram packets.
			receiveSocket = new DatagramSocket(4000);

			// to test socket timeout (2 seconds)
			// receiveSocket.setSoTimeout(2000);
		} catch (SocketException se) {
			se.printStackTrace();
			System.exit(1);
		}
			
	}
	
	public DatagramPacket receive(byte[] data, DatagramSocket socket) {
	    DatagramPacket receivePacket = new DatagramPacket(data, data.length);
	    System.out.println("Scheduler: Waiting for Packet.\n");

	    // Block until a datagram packet is received from receiveSocket.
	    try {
	      socket.receive(receivePacket);
	    } catch (IOException e) {
	      System.out.print("IO Exception: likely:");
	      System.out.println("Receive Socket Timed Out.\n" + e);
	      e.printStackTrace();
	      System.exit(1);
	    }

	    return receivePacket;
	  }

	  public void send(DatagramPacket sendPacket) {
	    System.out.println("Scheduler: Sending packet:");
	    int len = sendPacket.getLength();
	    String str  = new String(sendPacket.getData(), 0, len);

	    // Send the datagram packet to the client via the send socket.
	    try {
	    	sendReceiveSocket.send(sendPacket);
	    } catch (IOException e) {
	      e.printStackTrace();
	      System.exit(1);
	    }
	  }
	  
	  public void sendAndReceive() {
		 
	  }
	
	
	
	
	 

}
