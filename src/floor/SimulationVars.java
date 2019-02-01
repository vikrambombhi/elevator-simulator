package floor;

import java.net.InetAddress;

public  class SimulationVars {
	static int numberOfElevators = 1;
	static int numberOfFloors = 3;
	
	static int elevatorTravelTime = 500;
	
	static InetAddress[] elevatorAddresses = {InetAddress.getLoopbackAddress()};
	static int[] elevatorPorts = {4000};
	
	static InetAddress schedulerAddress = InetAddress.getLoopbackAddress();
	static int schedulerPort = 3000;
	
	static InetAddress[] floorAddresses = {InetAddress.getLoopbackAddress(), InetAddress.getLoopbackAddress(), InetAddress.getLoopbackAddress()};
	static int[] floorPorts = {5000, 5001, 5002};
	
	
}
