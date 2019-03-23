package scheduler;

import elevator.Elevator;
import elevator.ElevatorQueue;
import elevator.ElevatorShaft;
import floor.SimulationVars;
import messages.FloorArrivalMessage;
import messages.ElevatorRequestMessage.Direction;

public class Scheduler {
    private SchedulerMessenger messenger;
    private ElevatorShaft[] elevatorShafts;

    public Scheduler(SchedulerMessenger m) {
        messenger = m;

        elevatorShafts = new ElevatorShaft[SimulationVars.numberOfElevators];
        for (int i = 0; i < SimulationVars.numberOfElevators; i++) {
            elevatorShafts[i] = new ElevatorShaft(i);
        }
    }

    public void queuePickUp(Direction direction, int floor) {
        // Must synchronized all shafts to decide which to queue request to.
        synchronized(elevatorShafts) {
            // Priority 1: Elevator queues with no work.
            ElevatorShaft shaft = elevatorWithEmptyQueue();
            if (shaft != null) {
                addPickUpAndSort(shaft, floor, direction);
                int currentFloor = shaft.getElevator().getFloor();
                messenger.sendToElevator(currentFloor, floor, shaft);
                shaft.setCurrentDestination(currentFloor);
                return;
            }

            // Priority 2: Elevators that will past the destination in the same direction.
            // find elevators going the same direction and can hit
            shaft = elevatorOnPath(direction, floor);
            if (shaft != null) {
                addPickUpAndSort(shaft, floor, direction);
                return;
            }

            // Priority 3: Elevators with the smallest work queue.
            // find smallest queue size
            shaft = elevatorWithSmallestQueue();
            addPickUpAndSort(shaft, floor, direction);
        }
    }

    private ElevatorShaft elevatorWithEmptyQueue() {
        for (int i = 0; i < elevatorShafts.length; i++) {
            if (elevatorShafts[i] == null) {
                continue;
            }
            if (elevatorShafts[i].getQueue().isEmpty()) {
                return elevatorShafts[i];
            }
        }
        return null;
    }

    // Elevators that will past the destination in the same direction.
    private ElevatorShaft elevatorOnPath(Direction dir, int floor) {
        ElevatorShaft targetElevator = null;
        int smallestDiff = SimulationVars.numberOfFloors;
        for (int i = 0; i < elevatorShafts.length; i++) {
            ElevatorShaft e = elevatorShafts[i];
            if (e == null) {
                continue;
            }
            int currentFloor = e.getElevator().getFloor();
            switch (dir) {
                case UP:
                    if (e.getQueue().getDirection() == Direction.UP) {
                        if (currentFloor < floor) {
                            int diff = floor - currentFloor;
                            if (diff < smallestDiff) {
                                targetElevator = e;
                            }
                        }
                    }
                    break;
                case DOWN:
                    if (e.getQueue().getDirection() == Direction.DOWN) {
                        if (currentFloor > floor) {
                            int diff = currentFloor - floor;
                            if (diff < smallestDiff) {
                                targetElevator = e;
                            }
                        }
                    }
                    break;
            }
        }
        return targetElevator;
    }

    private ElevatorShaft elevatorWithSmallestQueue() {
        ElevatorShaft shaft = null;
        Integer smallestQueueIndex = null;
        for (int i = 0; i < elevatorShafts.length; i++) {
            if (elevatorShafts[i]  == null) {
                continue;
            }
            if (shaft == null) {
                shaft = elevatorShafts[i];
                smallestQueueIndex = i;
                continue;
            }
            if (elevatorShafts[i].getQueue().size() < shaft.getQueue().size()) {
                shaft = elevatorShafts[i];
                smallestQueueIndex = i;
            }
        }
        return shaft;
    }


    public void updateQueueState(Direction dir, int elevatorId, int floor) {
        ElevatorShaft shaft = elevatorShafts[elevatorId];
        synchronized (shaft) {
            // update elevator model
            shaft.setLastResponse(System.currentTimeMillis());
            shaft.getElevator().setFloor(floor);
            ElevatorQueue elevatorQueue = shaft.getQueue();

            switch (elevatorQueue.getDirection()) {
                case UP:
                    shaft.getElevator().setState(Elevator.State.MOVING_UP);
                    break;
                case DOWN:
                    shaft.getElevator().setState(Elevator.State.MOVING_DOWN);
                    break;
            }

            if (elevatorQueue.isEmpty()) {
                return;
            }

            // when an elevator arrives, tell it to go up or down, depending on the queues
            //System.out.printf("Scheduler: Elevator %d arrived at floor %d\n", elevatorId, floor);
            // tell elevator to go up, down, or stop & open
            int destination = elevatorQueue.peek();
            if (destination == floor) {
                System.out.printf("Scheduler: Elevator %d dequeuing floor %d\n", elevatorId, destination);
                Direction trueDirection = elevatorQueue.directionPeek();
                elevatorQueue.remove();
                // this is a pick up or drop off.. notify floor
                 FloorArrivalMessage msg = new FloorArrivalMessage(floor, elevatorId, dir);
               
                //if this corresponds to a pick, we can improve the accuracy of the direction 
                if (trueDirection != null) {
                	msg.setDirection(trueDirection);
                } 
                
                messenger.sendToFloor(msg);

                if (elevatorQueue.isEmpty()) {
                    return;
                }
            }
            shaft.setCurrentDestination(elevatorQueue.peek());
            messenger.sendToElevator(floor, elevatorQueue.peek(), shaft);
        }
    }

    public void queueDropOff(int elevatorId, int currentFloor, int destination) {
        // This means that an elevator is leaving a floor and we know what floor buttons
        // were pressed. The elevator must be at a stopped state.
        ElevatorShaft shaft = elevatorShafts[elevatorId];
        synchronized (shaft) {
            shaft.getElevator().setState(Elevator.State.STOPPED_DOORS_CLOSED);

            ElevatorQueue elevatorQueue = shaft.getQueue();
            if (!elevatorQueue.isEmpty()) {
                int nextFloor = elevatorQueue.peek();
                if (nextFloor == currentFloor) {
                    System.out.printf("Scheduler: Elevator %d dequeuing floor %d\n", elevatorId, nextFloor);
                    elevatorQueue.remove();
                }
            }
            // enqueues requested floors
            addDropOffAndSort(shaft, destination);

            // send the elevator on its way
            shaft.setCurrentDestination(elevatorQueue.peek());
            messenger.sendToElevator(currentFloor, elevatorQueue.peek(), shaft);
        }
    }

    // addAndSort adds the requested floor to the elevator's queue and sorts to stop
    // on floors on the way.
    private void addPickUpAndSort(ElevatorShaft e, int floor, Direction d) {
        e.getQueue().addPickUp(floor, d);
        queueSort(e);
    }

    private void addDropOffAndSort(ElevatorShaft e, int floor) {
        e.getQueue().addDropOff(floor);
        queueSort(e);
    }

    private void queueSort(ElevatorShaft e) {
        switch (e.getElevator().getState()) {
            case MOVING_UP:
                e.getQueue().sortUp();
                break;
            case MOVING_DOWN:
                e.getQueue().sortDown();
                break;
            case STOPPED_DOORS_CLOSED:
                defaultQueueSort(e);
                break;
        }
        System.out.println("Scheduler: Elevator " + e.getElevator().getId() + e.getQueue().toString());
    }

    private void defaultQueueSort(ElevatorShaft e) {
        // Continue sorting in the direction the queue is in.
        switch (e.getQueue().getDirection()) {
            case UP:
                e.getQueue().sortUp();
                break;
            case DOWN:
                e.getQueue().sortDown();
                break;
        }
    }

    public void removeElevator(int id) {
        System.out.println("Scheduler: Removing elevator: " + id + " containing the queue: " + elevatorShafts[id].getQueue());
        ElevatorQueue queue = elevatorShafts[id].getQueue();
        elevatorShafts[id] = null;

        redistributePickupRequests(queue);
    }

    private void redistributePickupRequests(ElevatorQueue queue) {
        // ignore drop off queues, those people are stuck in the unresponsive elevator
    	Direction dir;
        while (!queue.pickupIsEmpty()) {
        	dir = queue.pickUpPeekDirection();
            queuePickUp(dir, queue.pickupPop());
        }
    }

    public boolean anyAvailableElevators() {
        for (ElevatorShaft e : elevatorShafts) {
            if (e != null) {
                return true;
            }
        }
        return false;
    }

    public ElevatorShaft getElevatorShaft(int id) {
        return elevatorShafts[id];
    }
}
