package floor;

import java.net.InetAddress;

public  class SimulationVars {
	public static int numberOfElevators = 3;
	public static int numberOfFloors = 3;

	public static int timeScalar = 2;
	public static int elevatorTravelTime = 5000/timeScalar;

	public static InetAddress[] elevatorAddresses = {InetAddress.getLoopbackAddress(), InetAddress.getLoopbackAddress(), InetAddress.getLoopbackAddress()};
	public static int[] elevatorPorts = {4000, 4001, 4002};

	public static InetAddress schedulerAddress = InetAddress.getLoopbackAddress();
	public static int schedulerPort = 3000;

	public static InetAddress[] floorAddresses = {InetAddress.getLoopbackAddress(), InetAddress.getLoopbackAddress(), InetAddress.getLoopbackAddress()};
	public static int[] floorPorts = {5000, 5001, 5002};

	public static String inputFile = "src/input.txt";
}
