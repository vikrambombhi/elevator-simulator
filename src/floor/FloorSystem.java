package floorSubSys;

import floorSubSys.Floor;
		
public class FloorSystem {
	
	static Floor[] floors;
	
	public FloorSystem() {

	}
	
	public static void main(String args[])
	{	
		//TODO
		//read input file and dispatch requests
		//tracks each request and makes sure the passenger arrives eventually
		
		floors = new Floor[SimulationVars.numberOfFloors];
		for (int i = 0; i < SimulationVars.numberOfFloors; i++) {
			if (i == 0) {
				//isBottom = true
				floors[i] = new Floor(1, i, false, true);
			} else if (i == SimulationVars.numberOfFloors-1) {
				//isTop = true
				floors[i] = new Floor(1, i,  true, false);
			} else {
				//middle floor
				floors[i] = new Floor(1, i, false, false);
			}
		}
		
		
		for (int i = 0; i < SimulationVars.numberOfFloors; i++) {
			
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e ) {
				e.printStackTrace();
				System.exit(1);
			}
			
			floors[i].sendRequest("test");
		}
		
	}

}
