package ui;

import elevator.Elevator;
import elevator.Elevator.State;
import floor.Floor;
import floor.Floor.directionLampState;
import floor.FloorSubsystem;
import floor.SimulationVars;

public class Controller {
	private View view;
	
	public Controller() {
		view = new View();
	}
	
	//parsedElevator = {
	//"elevatorID",
	//"elevatorState",
	//"currentFloor",
	//"destinationList"
	//"faultState"
	//}
	public void updateElevator(Elevator elevator) {
		String[] s = new String[5];
		
		//id
		s[0] = Integer.toString(elevator.getId());
		
		//state
		State state = elevator.getState();
		if (state == State.MOVING_DOWN) {
			s[1] = "Moving Down";
		} else if (state == State.MOVING_UP) {
			s[1] = "Moving Up";
		} else if (state == State.STOPPED_DOORS_CLOSED) {
			s[1] = "Stopped: Doors Closed";
		} else {
			s[1] = "Stopped: Doors Open";
		}
		
		//current floor
		s[2] = Integer.toString(elevator.getFloor());
		
		//destinations
		s[3] = elevator.getButtons().toString();
		
		//fault state
		if (elevator.getHardFault()) {
			s[4] = "Out of Order";
		} else if (elevator.getSoftFault()) {
			s[4] = "Doors Stuck";
		} else {
			s[4] = "-";
		}
		
		view.updateElevator(s);
	}
	
	//parsedFloor = {
	//"floorNumber",
	//"numberOfUpPassengers",
	//"numberOfDownPassengers",
	//"directionLampStates**"
	//}
	public void updateFloor(FloorSubsystem floor) {
		String[] s = new String[4];
		
		//floorNumber
		s[0] = Integer.toString(floor.getFloorNum());
		
		//# of up passengers
		s[1] = Integer.toString(floor.getUpRequests().size());
		
		//# of down passengers
		s[2] = Integer.toString(floor.getDownRequests().size());
		
		//directionLampStates
		String lampStates = "";
		for (int i = 0; i < SimulationVars.numberOfElevators; i++) {
			lampStates += ("Elevator "+i+": ");
			Floor.directionLampState state = floor.getFloor().getDirectionLamp(i);
			if (state == directionLampState.DOWN) {
				lampStates += "Down, ";
			} else if (state == directionLampState.UP) {
				lampStates += "Up, ";
			} else {
				lampStates += "-, ";
			}	
		}
		s[3] = lampStates;
		
		view.updateFloor(s);
	}
}
