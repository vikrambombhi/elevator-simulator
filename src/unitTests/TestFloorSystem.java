package unitTests;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import floor.FloorSystem;
import junit.framework.TestCase;
import messages.ElevatorMessage;
import messages.ElevatorMessage.MessageType;
import messages.ElevatorRequestMessage;
import messages.ElevatorRequestMessage.Direction;
import messages.Message;

public class TestFloorSystem extends TestCase {
	
	
	public void testFloor() {
		ProxyArrival a = new ProxyArrival();
		a.start();
		
		FloorSystem floorSys = new FloorSystem();

		
	}


}
