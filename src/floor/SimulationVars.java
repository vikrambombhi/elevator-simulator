package floor;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class SimulationVars {
	public static String nonLocalAddress = "192.168.0.??";
	public static boolean schedulerIsLocal = true;
	public static boolean floorIsLocal = true;
	public static boolean elevatorIsLocal = true;
	
	public static int numberOfElevators = 4;
	public static int numberOfFloors = 22;

	public static int timeScalar = 2;
	public static int elevatorTravelTime = 5000/timeScalar;

	public static InetAddress elevatorSystemAddress = getAddress("Elevator");
	public static InetAddress[] elevatorAddresses = {elevatorSystemAddress, elevatorSystemAddress, elevatorSystemAddress, elevatorSystemAddress};
	public static int[] elevatorPorts = {4000, 4001, 4002, 4003};
	
	public static InetAddress schedulerAddress = getAddress("Scheduler");
	public static int schedulerPort = 3000;
	
	public static InetAddress floorSystemAddress = getAddress("Floor");
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
	
	public static InetAddress getAddress(String s) {
		if (s.equals("Scheduler")){
			if (schedulerIsLocal) {
				return InetAddress.getLoopbackAddress();
			} else {
				try {
					return InetAddress.getByName(nonLocalAddress);
				} catch (UnknownHostException e) {}
			}
		} else {
			if (elevatorIsLocal) {
				return InetAddress.getLoopbackAddress();
			} else {
				try {
					return InetAddress.getByName(nonLocalAddress);
				} catch (UnknownHostException e) {}
			}
		}
		return null;
	}
}
