package elevator;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

import messages.ElevatorRequestMessage.Direction;

public class ElevatorQueue {
	
	public class ElevatorItem {
		private int floor;
		private Direction direction;
		public ElevatorItem(int f, Direction d) {
			floor = f;
			direction = d;
		}
		public int getFloor() {return floor;}
		public Direction getDirection() {return direction;}
		public String toString() {
			String d;
			if (Direction.UP == direction) {
				d = "UP";
			} else {
				d = "DOWN";
			}
			return floor+"-"+d;
		}
	}

	private LinkedList<ElevatorItem> pickUpQueue;
	private LinkedList<Integer> dropOffQueue;
	private int elevatorPosition;
	private boolean hardFaulted = false;

	public ElevatorQueue() {
		pickUpQueue = new LinkedList<ElevatorItem>();
		dropOffQueue = new LinkedList<Integer>();
        elevatorPosition = 0;
	}

	public synchronized boolean isEmpty() {
		return (pickUpQueue.isEmpty() && dropOffQueue.isEmpty());
	}

	public synchronized boolean pickupIsEmpty() {
		return pickUpQueue.isEmpty();
	}

	public synchronized void addPickUp(Integer i, Direction d) {
		pickUpQueue.add(new ElevatorItem(i, d));
	}

	public synchronized void addDropOff(Integer i) {
		dropOffQueue.add(i);
	}

	public synchronized int size() {
		return pickUpQueue.size() + dropOffQueue.size();
	}

	public synchronized int peek() {
		if (isEmpty()) {
			return -1;
		}
		if (pickUpQueue.isEmpty()) {
			return dropOffQueue.peek();
		} else if (dropOffQueue.isEmpty()) {
			return pickUpQueue.peek().getFloor();
		}
		int pickUpDistance = Math.abs(pickUpQueue.peek().getFloor() - elevatorPosition);
		int dropOffDistance = Math.abs(dropOffQueue.peek() - elevatorPosition);
		if (pickUpDistance >= dropOffDistance) {
			return dropOffQueue.peek();
		} else {
			return pickUpQueue.peek().getFloor();
		}
	}
	
	public synchronized Direction directionPeek() {
		if (pickUpQueue.isEmpty()) {
			return null;
		} else if (peek() != pickUpPeek()) {
			return null;
		} else {
			return pickUpQueue.peek().getDirection();
		}
	}

	public synchronized int pickUpPeek() {
		if (pickUpQueue.isEmpty()) {
			return -1;
		}
		return pickUpQueue.peek().getFloor();
	}

	public synchronized int dropOffPeek() {
		if (dropOffQueue.isEmpty()) {
			return -1;
		}
		return dropOffQueue.peek();
	}

	public synchronized void remove() {
		int peek = peek();
		while (peek == peek()) {
			if((!pickUpQueue.isEmpty()) && pickUpQueue.peek().getFloor() == peek) {
				pickUpQueue.pop();
			}
			if((!dropOffQueue.isEmpty()) && dropOffQueue.peek() == peek) {
				dropOffQueue.pop();
			}
		}
	}

	public synchronized Integer pickupPop() {
		return pickUpQueue.pop().getFloor();
		
	}

	public synchronized String toString() {
		return " at floor: "+elevatorPosition+" pick-ups: "+pickUpQueue.toString()+ " drop-offs: "+dropOffQueue.toString();
	}

	public synchronized void sortUp() {
		Collections.sort(pickUpQueue, new Comparator<ElevatorItem>() {
			@Override
			public int compare(ElevatorItem int1, ElevatorItem int2) {
				return int1.getFloor() - int2.getFloor();
			}
		});
		Collections.sort(dropOffQueue);
		//direction = Direction.UP;
	}

	public synchronized void sortDown() {
		Collections.sort(pickUpQueue, new Comparator<ElevatorItem>() {
			@Override
			public int compare(ElevatorItem int1, ElevatorItem int2) {
				return int2.getFloor() - int1.getFloor();
			}
		});
		Collections.sort(dropOffQueue, new Comparator<Integer>() {
			@Override
			public int compare(Integer int1, Integer int2) {
				return int2 - int1;
			}
		});
		//direction = Direction.DOWN;
	}

	public synchronized Direction getDirection() {
		return null;
	}
	
	public synchronized Direction pickUpPeekDirection() {
		if (peek() == pickUpQueue.peek().getFloor()) {
			return pickUpQueue.peek().getDirection();
		}
		return null;
	}
	
	public synchronized boolean contains(int f) {
		for (int i = 0; i < pickUpQueue.size(); i++) {
			if (pickUpQueue.get(i).getFloor() == f) {
				return true;
			}
		}
		for (int i = 0; i < dropOffQueue.size(); i++) {
			if (dropOffQueue.get(i) == f) {
				return true;
			}
		}
		return false;
	}
	
	public synchronized boolean onRoute(int f) {
		ElevatorItem tempUp = new ElevatorItem(f, Direction.UP);
		ElevatorItem tempDown = new ElevatorItem(f, Direction.DOWN);
		if (dropOffQueue.contains(f) || pickUpQueue.contains(tempUp) || pickUpQueue.contains(tempDown)) {
			return true;
		}
		int lowest = -1;
		int highest = -1;
		for (int i = 0; i < pickUpQueue.size(); i++) {
			int temp = pickUpQueue.get(i).getFloor();
			if (lowest > temp || lowest == -1) {
				lowest = temp;
			}
			if (highest < temp || highest == -1) {
				highest = temp;
			}
		}
		for (int i = 0; i < dropOffQueue.size(); i++) {
			int temp = dropOffQueue.get(i);
			if (lowest > temp || lowest == -1) {
				lowest = temp;
			}
			if (highest < temp || highest == -1) {
				highest = temp;
			}
		}
		if (lowest <= f && highest >= f) {
			return true;
		}
		return false;
	}
	
	public synchronized int floorsAway(int f) {
		int floorsAway = -1;
		for (int i = 0; i < pickUpQueue.size(); i++) {
			int temp = pickUpQueue.get(i).getFloor();
			if (floorsAway > temp || floorsAway == -1) {
				floorsAway = temp;
			}
		}
		for (int i = 0; i < dropOffQueue.size(); i++) {
			int temp = dropOffQueue.get(i);
			if (floorsAway > temp || floorsAway == -1) {
				floorsAway = temp;
			}
		}
		return floorsAway;
	}
	
	public synchronized void rebalance() {
		Collections.sort(pickUpQueue, new Comparator<ElevatorItem>() {
			@Override
			public int compare(ElevatorItem int1, ElevatorItem int2) {
				return Math.abs(int1.getFloor() - elevatorPosition) - Math.abs(int2.getFloor() - elevatorPosition);
			}
		});
		Collections.sort(dropOffQueue, new Comparator<Integer>() {
			@Override
			public int compare(Integer int1, Integer int2) {
				return Math.abs(int1 - elevatorPosition) - Math.abs(int2 - elevatorPosition);
			}
		});
	}
	
	public int getElevatorPosition() {
		return elevatorPosition;
	}
	
	public void setElevatorPosition(int i) {
		elevatorPosition = i;
	}
	
	public void setHardFaulted(boolean b) {
		hardFaulted = b;
	}
	
	public boolean getHardFaulted() {
		return hardFaulted;
	}
}
