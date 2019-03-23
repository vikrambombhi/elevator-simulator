package floor;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;
import messages.*;
import messages.ElevatorRequestMessage.Direction;

import java.util.*;

public class File {
	
	private String filename;
	
	public File(String f) {
		filename = f;
	}
	
	//returns the timestamp in milliseconds of the next message floor: floorNum will send
	public int getNextRequestTime(int currentTime, int floorNum) {
		int found = -1;
		try(BufferedReader br = new BufferedReader(new FileReader(filename))){
			String line;
			while ((line = br.readLine()) != null) {
				String[] parts = line.split(" ");
				String timeStamp = parts[0];
				String origin = parts[1];
				int msTime = getMS(timeStamp);
				
				if (Integer.parseInt(origin) == floorNum && msTime > currentTime) {
					if (found == -1 || msTime < found) {
						found = msTime;
					} 
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return found;
	}
	
	//returns the floor metamessage that will be sent by floor: floorNum at time: time
	public FloorMetaMessage getNextMetaMessage(int time, int floorNum) {
		FloorMetaMessage f = null;
		try(BufferedReader br = new BufferedReader(new FileReader(filename))){
			String line;
			while ((line = br.readLine()) != null) {
				String[] parts = line.split(" ");
				String timeStamp = parts[0];
				String origin = parts[1];
				String destination = parts[3];
				int msTime = getMS(timeStamp);
				
				if (Integer.parseInt(origin) == floorNum && msTime == time) {
					f = new FloorMetaMessage(true);
					f.setDestinationFloor(Integer.parseInt(destination));
					f.setStartingFloor(Integer.parseInt(origin));
					return f;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return f;
	}
	
	//returns the elevator request message that will be send by floor: floorNum at time: time 
	public ElevatorRequestMessage getRequestMessage(int time, int floorNum) {
		ElevatorRequestMessage m = null;
		try(BufferedReader br = new BufferedReader(new FileReader(filename))){
			String line;
			while ((line = br.readLine()) != null) {
				String[] parts = line.split(" ");
				String timeStamp = parts[0];
				String origin = parts[1];
				String direction = parts[2];
				int msTime = getMS(timeStamp);
				
				if (Integer.parseInt(origin) == floorNum && msTime == time) {
					m = new ElevatorRequestMessage();
					m.setOriginFloor(Integer.parseInt(origin));
					if (direction.equals("Up")) {
						m.setDirection(Direction.UP);
					} else {
						m.setDirection(Direction.DOWN);
					}
					return m;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return m;
	}
	
	public FaultMessage getFaultMessage(int time) {
		FaultMessage m = null;
		try(BufferedReader br = new BufferedReader(new FileReader(filename))){
			String line;
			while ((line = br.readLine()) != null) {
				String[] parts = line.split(" ");
				String timeStamp = parts[0];
				String faultIndicator = parts[1];
				String faultType = parts[2];
				String elevator = parts[3];
				int msTime = getMS(timeStamp);
				
				if (Integer.parseInt(faultIndicator) == -1 && msTime == time) {
					m = new FaultMessage();
					m.setElevator(Integer.parseInt(elevator));
					if (faultType.equals("Hard")) {
						m.setHardFault(true);
					} else {
						m.setSoftFault(true);
					}
					return m;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return m;
	}
	
	public TerminateMessage getTerminateMessage(int time) {
		TerminateMessage m;
		try(BufferedReader br = new BufferedReader(new FileReader(filename))){
			String line;
			while ((line = br.readLine()) != null) {
				String[] parts = line.split(" ");
				String timeStamp = parts[0];
				String faultIndicator = parts[1];
				String terminate = parts[2];
				String elevator = parts[3];
				int msTime = getMS(timeStamp);
				
				if (Integer.parseInt(faultIndicator) == -1 && msTime == time) {
					if (terminate.equals("Terminate")) {
						m = new TerminateMessage();
						return m;
					}	
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	//returns the stringified time stamp t in milliseconds
	public int getMS(String t) {
		String[] parts = t.split(":");
		int time = 0;
		//add milliseconds
		time += Integer.parseInt(parts[2].substring(3));
		//add seconds
		time += (Integer.parseInt(parts[2].substring(0, 2))*1000);
		//add mins 
		time += (Integer.parseInt(parts[1])*60000);
		//add hours
		time += (Integer.parseInt(parts[0])*3600000);
		
		return time;
	}
}
