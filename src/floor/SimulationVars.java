package floor;

import java.net.InetAddress;

public  class SimulationVars {
	public static int numberOfElevators = 1;
	public static int numberOfFloors = 3;

	public static int elevatorTravelTime = 500;

	public static InetAddress[] elevatorAddresses = {InetAddress.getLoopbackAddress()};
	public static int[] elevatorPorts = {4000};

	public static InetAddress schedulerAddress = InetAddress.getLoopbackAddress();
	public static int schedulerPort = 3000;

	public static InetAddress[] floorAddresses = {InetAddress.getLoopbackAddress(), InetAddress.getLoopbackAddress(), InetAddress.getLoopbackAddress()};
	public static int[] floorPorts = {5000, 5001, 5002};
}
