package floor;

import floor.Floor.directionLampState;
import junit.framework.TestCase;
import messages.FloorMetaMessage;
import messages.FloorTravelMessage;
import messages.ElevatorRequestMessage.Direction;
import messages.FloorArrivalMessage;

public class FloorUnitTests extends TestCase {

	public FloorUnitTests() {}
	
	public void testModelOnStart() {
		
		FloorSubsystem[] floors = new FloorSubsystem[SimulationVars.numberOfFloors];
		
		//try each combination of bools on the constructor
		for (int i = 0; i < SimulationVars.numberOfFloors; i++) {
			boolean isBot = false;
			boolean isTop = false;
			if (i == 0) {
				isBot = true;
			} 
			if (i == SimulationVars.numberOfFloors-1) {
				isTop = true;
			} 
			floors[i] = new FloorSubsystem(i, isBot, isTop);
		}
		
		Floor tempFloor;
		
		for (int i = 0; i < 3; i++) {
			//check floorNum
			assertEquals(floors[i].getFloorNum(), i);
			
			//check request queues are starting empty
			assertTrue(floors[i].getDownRequests().isEmpty());
			assertTrue(floors[i].getUpRequests().isEmpty());
			
			//check floor states
			tempFloor = floors[i].getFloor();
			
			for (int a = 0; a < SimulationVars.numberOfElevators; a++) {
				//make sure all direction lamps are idle
				assertEquals(tempFloor.getDirectionLamp(a), directionLampState.IDLE);
				
				//make sure we arent waiting for any passengers to arrive 
				assertEquals(floors[i].getPassengers()[a], 0);
			}
			
			//make sure request lamps are off
			assertEquals(tempFloor.getDownLamp(), false);
			assertEquals(tempFloor.getUpLamp(), false);
		}
		
		for (int i = 0; i < SimulationVars.numberOfFloors; i++) {
			floors[i].tearDown();
		}
	}
	
	public void testRequestMessage() {
		
		FloorSubsystem floorSubsys = new FloorSubsystem(1, false, false);
		
		//create a basic request message - as RequestSimulator would
		FloorMetaMessage m = new FloorMetaMessage(true);
		m.setStartingFloor(1);
		m.setDestinationFloor(2);
		
		//up lamp starts OFF
		assertFalse(floorSubsys.getFloor().getUpLamp());
		//there is no one waiting to go to floor 2 yet...
		assertFalse(floorSubsys.getUpRequests().contains(2));
		
		//message received
		floorSubsys.requestMessage(m);
		
		//up lamp end ON
		assertTrue(floorSubsys.getFloor().getUpLamp());
		//there is a request for floor 2 
		assertTrue(floorSubsys.getUpRequests().contains(2));
		
		floorSubsys.tearDown();
	}
	
	public void testMetaMessage() {
		
		FloorSubsystem floorSubsys = new FloorSubsystem(2, false, true);
		
		//create a basic request message - as a floorSubsystem would
		FloorMetaMessage m = new FloorMetaMessage(false);
		m.setStartingFloor(1);
		m.setDestinationFloor(2);
		m.setElevator(0);
		
		//the number of people the floor expects to arrive on its floor in elevator (m.getElevator())
		int before = floorSubsys.getPassengers()[m.getElevator()];
		
		floorSubsys.metaMessage(m);
		
		int after = floorSubsys.getPassengers()[m.getElevator()];
		
		//one more passenger is expected to arrive than before
		assertTrue(after == before +1);
		
		floorSubsys.tearDown();
	}
	
	public void testTravelMessage() {
		
		FloorSubsystem floorSubsys = new FloorSubsystem(1, false, false);
		
		//create a FloorTravelMessage going UP - as an elevator would
		FloorTravelMessage mUP = new FloorTravelMessage();
		mUP.setDirection(Direction.UP);
		mUP.setElevator(0);
		mUP.setStartingFloor(0);
		
		//create a FloorTravelMessage going DOWN - as an elevator would
		FloorTravelMessage mDOWN = new FloorTravelMessage();
		mDOWN.setDirection(Direction.DOWN);
		mDOWN.setElevator(0);
		mDOWN.setStartingFloor(2);
		
		//check that the direction lamp starts idle
		assertEquals(floorSubsys.getFloor().getDirectionLamp(0), directionLampState.IDLE);
		
		floorSubsys.travelMessage(mUP);
		
		//check that the state updates to UP
		assertEquals(floorSubsys.getFloor().getDirectionLamp(0), directionLampState.UP);
		
		floorSubsys.travelMessage(mDOWN);
		
		//check that the state updates to DOWN
		assertEquals(floorSubsys.getFloor().getDirectionLamp(0), directionLampState.DOWN);
		
		floorSubsys.tearDown();
	}
	
	public void testArrivalMessage() {
		//This scenario is more complex and involves the previous messages having already been sent...
		
		FloorSubsystem floorSubsys = new FloorSubsystem(1, false, false);
		
		//first simulate a passenger we expect to get off on our floor in the arriving elevator
		//this is done as in testMetaMessage...
		FloorMetaMessage m = new FloorMetaMessage(false);
		m.setStartingFloor(0);
		m.setDestinationFloor(1);
		m.setElevator(0);
		
		//meta message received
		floorSubsys.metaMessage(m);
		//here is our one passenger
		assertEquals(floorSubsys.getPassengers()[0], 1);
		
		//second simulate a request message
		FloorMetaMessage rm = new FloorMetaMessage(false);
		rm.setStartingFloor(1);
		rm.setDestinationFloor(2);
		rm.setElevator(0);
		
		//request message received
		floorSubsys.requestMessage(rm);
		//someone is waiting to get on going UP..
		assertTrue(floorSubsys.getFloor().getUpLamp());
		
		//create an arrival message - as an arrival sensor would
		FloorArrivalMessage am = new FloorArrivalMessage();
		am.setDirection(Direction.UP);
		am.setElevator(0);
		am.setFloor(1);
		
		//arrival message received
		floorSubsys.arrivalMessage(am);
		
		//check that the passengers we expected to get off really did get off
		assertEquals(floorSubsys.getPassengers()[0], 0);
		
		//check that our up request is no longer pending + the lamp is off
		assertFalse(floorSubsys.getUpRequests().contains(2));
		assertFalse(floorSubsys.getFloor().getUpLamp());
		
		floorSubsys.tearDown();
	}
}
