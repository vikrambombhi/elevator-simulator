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
	private Direction direction;

	public ElevatorQueue() {
		pickUpQueue = new LinkedList<ElevatorItem>();
		dropOffQueue = new LinkedList<Integer>();
        direction = Direction.UP;
	}

	public boolean isEmpty() {
		return (pickUpQueue.isEmpty() && dropOffQueue.isEmpty());
	}

	public boolean pickupIsEmpty() {
		return pickUpQueue.isEmpty();
	}

	public void addPickUp(Integer i, Direction d) {
		pickUpQueue.add(new ElevatorItem(i, d));
	}

	public void addDropOff(Integer i) {
		dropOffQueue.add(i);
	}

	public int size() {
		return pickUpQueue.size() + dropOffQueue.size();
	}

	public int peek() {
		if (isEmpty()) {
			return -1;
		}
		if (pickUpQueue.isEmpty()) {
			return dropOffQueue.peek();
		} else if (dropOffQueue.isEmpty()) {
			return pickUpQueue.peek().getFloor();
		}

		if (direction == Direction.UP) {
			return Math.min(pickUpQueue.peek().getFloor(), dropOffQueue.peek());
		} else {
			return Math.max(pickUpQueue.peek().getFloor(), dropOffQueue.peek());
		}
	}
	
	public Direction directionPeek() {
		if (pickUpQueue.isEmpty()) {
			return null;
		} else if (peek() != pickUpPeek()) {
			return null;
		} else {
			return pickUpQueue.peek().getDirection();
		}
	}

	public int pickUpPeek() {
		if (pickUpQueue.isEmpty()) {
			return -1;
		}
		return pickUpQueue.peek().getFloor();
	}

	public int dropOffPeek() {
		if (dropOffQueue.isEmpty()) {
			return -1;
		}
		return dropOffQueue.peek();
	}

	public Integer remove() {
		int peek = peek();
		if (peek != -1) {
			while (peek == pickUpPeek()) {
				pickUpQueue.remove();
			}
			while (peek == dropOffPeek()) {
				dropOffQueue.remove();
			}
		}
		return peek;
	}

	public Integer pickupPop() {
		return pickUpQueue.pop().getFloor();
	}

	public String toString() {
		return " pick-ups: "+pickUpQueue.toString()+ " drop-offs: "+dropOffQueue.toString();
	}

	public void sortUp() {
		Collections.sort(pickUpQueue, new Comparator<ElevatorItem>() {
			@Override
			public int compare(ElevatorItem int1, ElevatorItem int2) {
				return int1.getFloor() - int2.getFloor();
			}
		});
		Collections.sort(dropOffQueue);
		direction = Direction.UP;
	}

	public void sortDown() {
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
		direction = Direction.DOWN;
	}

	public Direction getDirection() {
		return direction;
	}
}
