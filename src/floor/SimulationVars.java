package floor;

import java.net.InetAddress;

public  class SimulationVars {
	public static int numberOfElevators = 4;
	public static int numberOfFloors = 22;

	public static int timeScalar = 10;
	public static int elevatorTravelTime = 5000/timeScalar;

	public static InetAddress elevatorSystemAddress = InetAddress.getLoopbackAddress();
	public static InetAddress[] elevatorAddresses = {elevatorSystemAddress, elevatorSystemAddress, elevatorSystemAddress, elevatorSystemAddress};
	public static int[] elevatorPorts = {4000, 4001, 4002, 4003};
	
	public static InetAddress schedulerAddress = InetAddress.getLoopbackAddress();
	public static int schedulerPort = 3000;
	
	public static InetAddress floorSystemAddress = InetAddress.getLoopbackAddress();
	public static InetAddress[] floorAddresses = 
		{
			floorSystemAddress, floorSystemAddress,floorSystemAddress, floorSystemAddress, floorSystemAddress,
			floorSystemAddress, floorSystemAddress,floorSystemAddress, floorSystemAddress, floorSystemAddress,
			floorSystemAddress, floorSystemAddress,floorSystemAddress, floorSystemAddress, floorSystemAddress,
			floorSystemAddress, floorSystemAddress,floorSystemAddress, floorSystemAddress, floorSystemAddress,
			floorSystemAddress, floorSystemAddress
		};	
	
	public static int[] floorPorts = {5000, 5001, 5002, 5003, 5004, 5005, 5006, 5007, 5008, 5009, 5010, 5011, 5012, 5013, 5014, 5015, 5016, 5017, 5018, 5019, 5020, 5021};
	public static int timerPort = 6000;
	
	public static String inputFile = "src/input.txt";
	public static String outputFile = "src/output.txt";
}
