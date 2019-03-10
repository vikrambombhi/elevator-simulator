package elevator;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

import messages.ElevatorRequestMessage.Direction;

public class ElevatorQueue {

	private LinkedList<Integer> pickUpQueue;
	private LinkedList<Integer> dropOffQueue;
	private Direction direction;

	public ElevatorQueue() {
		pickUpQueue = new LinkedList<Integer>();
		dropOffQueue = new LinkedList<Integer>();
        direction = Direction.UP;
	}

	public boolean isEmpty() {
		return (pickUpQueue.isEmpty() && dropOffQueue.isEmpty());
	}

	public boolean pickupIsEmpty() {
		return pickUpQueue.isEmpty();
	}

	public void addPickUp(Integer i) {
		pickUpQueue.add(i);
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
			return pickUpQueue.peek();
		}

		if (direction == Direction.UP) {
			return Math.min(pickUpQueue.peek(), dropOffQueue.peek());
		} else {
			return Math.max(pickUpQueue.peek(), dropOffQueue.peek());
		}
	}

	public int pickUpPeek() {
		if (pickUpQueue.isEmpty()) {
			return -1;
		}
		return pickUpQueue.peek();
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
		return pickUpQueue.pop();
	}

	public String toString() {
		return " pick-ups: "+pickUpQueue.toString()+ " drop-offs: "+dropOffQueue.toString();
	}

	public void sortUp() {
		Collections.sort(pickUpQueue);
		Collections.sort(dropOffQueue);
		direction = Direction.UP;
	}

	public void sortDown() {
		Collections.sort(pickUpQueue, new Comparator<Integer>() {
			@Override
			public int compare(Integer int1, Integer int2) {
				return int2 - int1;
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
