package unitTests;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

import java.net.InetAddress;
import java.net.SocketException;

import messages.ElevatorRequestMessage.Direction;
import messages.FloorArrivalMessage;
import messages.Message;

public class ProxyArrival extends Thread {
	
	DatagramPacket pack;
	DatagramSocket sock;
	FloorArrivalMessage mess;
	
	public ProxyArrival() {
		mess = new FloorArrivalMessage();
		try {
			sock = new DatagramSocket();
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mess.setDirection(FloorArrivalMessage.Direction.UP);
		mess.setElevator(0);
		mess.setFloor(1);
		byte[] data = Message.serialize(mess);
		
		pack = new DatagramPacket(data, data.length, InetAddress.getLoopbackAddress(), 4004);
	}
	
	@Override
	public void run() {
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Message.send(sock, pack);
	}
}
