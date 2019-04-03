package ui;

import floor.SimulationVars;

public class View {
	private String[][] elevators;
	private String[][] floors;

	public View() {
		elevators = new String[SimulationVars.numberOfElevators][];
		floors = new String [SimulationVars.numberOfFloors][];
		for (int e = 0; e  < SimulationVars.numberOfElevators; e++) {
			elevators[e] = new String[4];
		}
		for (int f = 0; f  < SimulationVars.numberOfFloors; f++) {
			floors[f] = new String[3];
		}
	}

	//parsedElevator = {
		//"elevatorID",
		//"elevatorState",
		//"currentFloor",
		//"destinationList"
		//"faultState"
		//}
	public void updateElevator(String[] elevator) {
		String elevatorID = elevator[0];
		String elevatorState = elevator[1];
		String currentFloor = elevator[2];
		String destinationList = elevator[3];
		String faultState = elevator[4];

		String[] elevatorToUpdate = elevators[Integer.parseInt(elevatorID)];

		setElevatorState(elevatorToUpdate, elevatorState);
		setCurrentFloor(elevatorToUpdate, currentFloor);
		setDestinationList(elevatorToUpdate, destinationList);
		setFaultState(elevatorToUpdate, faultState);

		//post to UI
		//System.out.println(elevator[0] + " " + elevator[1] + " " + elevator[2] + " " + elevator[3] + " " + elevator[4]);
	}

	//parsedFloor = {
		//"floorNumber",
		//"numberOfUpPassengers",
		//"numberOfDownPassengers",
		//"directionLampStates**"
		//}
	public void updateFloor(String[] floor) {
		String floorNumber = floor[0];
		String numberOfUpPassengers = floor[1];
		String numberOfDownPassengers = floor[2];
		String directionLampState = floor[3];

		String[] floorToUpdate = floors[Integer.parseInt(floorNumber)];

		setNumberOfUpPassengers(floorToUpdate, numberOfUpPassengers);
		setNumberOfDownPassengers(floorToUpdate, numberOfDownPassengers);
		setDirectionLampState(floorToUpdate, directionLampState);

		//post to UI
		//System.out.println(floor[0] + " " + floor[1] + " " + floor[2] + " " + floor[3]);
	}

	//helpers
	public void setElevatorState(String[] elevatorToUpdate, String elevatorState) {
		elevatorToUpdate[0] = elevatorState;
	}
	public void setCurrentFloor(String[] elevatorToUpdate, String currentFloor) {
		elevatorToUpdate[1] = currentFloor;
	}
	public void setDestinationList(String[] elevatorToUpdate, String destinationList) {
		elevatorToUpdate[1] = destinationList;
	}
	public void setFaultState(String[] elevatorToUpdate, String faultState) {
		elevatorToUpdate[2] = faultState;
	}
	public void setNumberOfUpPassengers(String[] floorToUpdate, String numberOfUpPassengers) {
		floorToUpdate[0] = numberOfUpPassengers;
	}
	public void setNumberOfDownPassengers(String[] floorToUpdate, String numberOfDownPassengers) {
		floorToUpdate[1] = numberOfDownPassengers;
	}
	public void setDirectionLampState(String[] floorToUpdate, String directionLampState){
		floorToUpdate[2] = directionLampState;
	}

	public String getElevatorState(String[] elevator) {
		return elevator[0];
	}
	public String getCurrentFloor(String[] elevator) {
		return elevator[1];
	}
	public String getDestinationList(String[] elevator) {
		return elevator[2];
	}
	public String getFaultState(String[] elevator) {
		return elevator[3];
	}
	public String getNumberOfUpPassengers(String[] floor) {
		return floor[0];
	}
	public String getNumberOfDownPassengers(String[] floor) {
		return floor[1];
	}
	public String getDirectionLampState(String[] floor) {
		return floor[2];
	}
}
