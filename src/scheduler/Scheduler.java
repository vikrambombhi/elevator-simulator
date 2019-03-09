package scheduler;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import elevator.Elevator;
import elevator.ElevatorQueue;
import messages.ElevatorMessage;
import messages.ElevatorMessage.MessageType;
import messages.ElevatorRequestMessage;
import messages.ElevatorRequestMessage.Direction;
import messages.FloorArrivalMessage;
import messages.FloorRequestMessage;
import messages.Message;
import floor.SimulationVars;

public class Scheduler {
    public static String HOST = "127.0.0.1";
    public static short PORT = 3000;
	// 2 seconds which is 4 times the SimulationVars.elevatorTravelTime
	private static long SOFT_FAULT_INTERVAL = 2 * 1000; // 2 seconds
	private static long HARD_FAULT_INTERVAL = 5 * 1000; // 5 seconds

    private DatagramSocket recvSock, sendSock;
    private ElevatorQueue[] queues;
    private Elevator[] elevators;
	private long[] lastResponses;
	private Thread elevatorWatcher;

    public Scheduler() {
        try {
            // Construct a datagram socket and bind it to port 3000
            // on the local host machine. This socket will be used to
            // receive UDP Datagram packets.
            recvSock = new DatagramSocket(PORT);
            sendSock = new DatagramSocket();

            elevators = new Elevator[SimulationVars.numberOfElevators];
            for (int i = 0; i < SimulationVars.numberOfElevators; i++) {
                elevators[i] = new Elevator(i);
            }

            // to test socket timeout (2 seconds)
            // receiveSocket.setSoTimeout(2000);
        } catch (SocketException se) {
            se.printStackTrace();
            System.exit(1);
        }
        queues = new ElevatorQueue[SimulationVars.numberOfElevators];
        for (int i = 0; i < SimulationVars.numberOfElevators; i++) {
            queues[i] = new ElevatorQueue();
        }

		newElevatorWatcher();
		lastResponses = new long[SimulationVars.numberOfElevators];
        for (int i = 0; i < SimulationVars.numberOfElevators; i++) {
            lastResponses[i] = System.currentTimeMillis();
        }
    }

    public void schedule() {
        // continuously listen for packets from elevators or floors
        // handle them
        System.out.println("Scheduler: Waiting for message");
        Message m = Message.deserialize(Message.receive(recvSock).getData());
        if (m instanceof ElevatorRequestMessage) {
            handleElevatorRequest((ElevatorRequestMessage) m);
        } else if (m instanceof FloorArrivalMessage) {
			FloorArrivalMessage msg = (FloorArrivalMessage) m;
			if (elevators[msg.getElevator()] == null) {
				// drop message
				return;
			}
            handleFloorArrival(msg);
        } else if (m instanceof FloorRequestMessage) {
			FloorRequestMessage msg = (FloorRequestMessage) m;
			if (elevators[msg.getElevator()] == null) {
				// drop message
				return;
			}
            handleFloorRequest((FloorRequestMessage) m);
        }
    }

	// Adds request to pick up to the best elevator. The best elevator for the
	// request is determined by it's qualification on different priority levels.
	// If no elevators satisfy priority 1, then the best elevator for priority 2
	// will be chosen and etc.
    private synchronized void handleElevatorRequest(ElevatorRequestMessage m) {
		// Priority 1: Elevator queues with no work.
        Integer emptyQueueIndex = null;
        for (int i = 0; i < queues.length; i ++) {
            if (queues[i] == null) {
				continue;
			}
            if (queues[i].isEmpty()) {
                emptyQueueIndex = i;
            }
        }
        if (emptyQueueIndex != null) {
            addPickUpAndSort(emptyQueueIndex, m.getOriginFloor());
            int currentFloor = elevators[emptyQueueIndex].getFloor();
            sendToElevator(directElevatorTo(currentFloor, m.getOriginFloor()), emptyQueueIndex);
            return;
        }

		// Priority 2: Elevators that will past the destination in the same direction.
        // find elevators going the same direction and can hit
        Elevator targetElevator = null;
        int smallestDiff = SimulationVars.numberOfFloors;
        for (Elevator e: elevators) {
            if (e == null) {
				continue;
			}
            switch (m.getDirection()) {
                case UP:
                    if (e.getState() == Elevator.State.MOVING_UP) {
                        if (e.getFloor() < m.getOriginFloor()) {
                            int diff = m.getOriginFloor() - e.getFloor();
                            if (diff < smallestDiff) {
                                targetElevator = e;
                            }
                        }
                    }
                    break;
                case DOWN:
                    if (e.getState() == Elevator.State.MOVING_DOWN) {
                        if (e.getFloor() > m.getOriginFloor()) {
                            int diff = e.getFloor() - m.getOriginFloor();
                            if (diff < smallestDiff) {
                                targetElevator = e;
                            }
                        }
                    }
                    break;
            }
        }
        if (targetElevator != null) {
            queues[targetElevator.getId()].addPickUp(m.getOriginFloor());
            return;
        }

		// Priority 3: Elevators with the smallest work queue.
        // find smallest queue size
        ElevatorQueue smallestQueue = queues[0];
        Integer smallestQueueIndex = null;
        for (int i = 1; i < queues.length; i ++) {
            if (queues[i] == null) {
				continue;
			}
            if (queues[i].size() < smallestQueue.size()) {
                smallestQueue = queues[i];
                smallestQueueIndex = i;
            }
        }
        addPickUpAndSort(smallestQueueIndex, m.getOriginFloor());
    }

    private synchronized void handleFloorArrival(FloorArrivalMessage m) {
		// TODO: properly time the elevators as last response time as the time
		// you sent it a message and the time it last replied.
		lastResponses[m.getElevator()] = System.currentTimeMillis();
        // update elevator model
        elevators[m.getElevator()].setFloor(m.getFloor());
        ElevatorQueue elevatorQueue = queues[m.getElevator()];

        if (elevatorQueue.isEmpty()) {
            return;
        }
        // when an elevator arrives, tell it to go up or down, depending on the queues
        System.out.printf("Scheduler: Elevator %d arrived at floor %d\n", m.getElevator(), m.getFloor());
        // tell elevator to go up, down, or stop & open
        int destination = elevatorQueue.peek();
        if (destination == m.getFloor()) {
            System.out.printf("Scheduler: Elevator %d dequeuing floor %d\n", m.getElevator(), destination);
            elevatorQueue.remove();

            //this is a pick up or drop off.. notify floor
            sendToFloor(m);
        }
        if (elevatorQueue.isEmpty()) {
            return;
        }
        sendToElevator(directElevatorTo(m.getFloor(), elevatorQueue.peek()), m.getElevator());
        return;
    }

    private synchronized void handleFloorRequest(FloorRequestMessage m) {
        // this means that an elevator is leaving a floor
        // we know what floor buttons were pressed
        ElevatorQueue elevatorQueue = queues[m.getElevator()];
        if (!elevatorQueue.isEmpty()) {
            int destination = elevatorQueue.peek();
            if (destination == m.getCurrent()) {
                System.out.printf("Scheduler: Elevator %d dequeuing floor %d\n", m.getElevator(), destination);
                elevatorQueue.remove();
            }
        }
        // enqueues requested floors
        addDropOffAndSort(m.getElevator(), m.getDestination());
        System.out.println("Scheduler: Elevator " + m.getElevator() + elevatorQueue.toString());

        // send the elevator on its way
        sendToElevator(directElevatorTo(m.getCurrent(), elevatorQueue.peek()), m.getElevator());
    }

	// addAndSort adds the requested floor to the elevator's queue and sorts to stop
	// on floors on the way.
	private void addPickUpAndSort(int elevatorId, int floor) {
		queues[elevatorId].addPickUp(floor);
		switch (elevators[elevatorId].getState()) {
			case MOVING_UP:
				queues[elevatorId].sortUp();
				break;
			case MOVING_DOWN:
				queues[elevatorId].sortDown();
				break;
		}
        System.out.println("Scheduler: Elevator " + elevatorId + queues[elevatorId].toString());
	}

	private void addDropOffAndSort(int elevatorId, int floor) {
		queues[elevatorId].addDropOff(floor);
		switch (elevators[elevatorId].getState()) {
			case MOVING_UP:
				queues[elevatorId].sortUp();;
				break;
			case MOVING_DOWN:
				queues[elevatorId].sortDown();
				break;
		}
        System.out.println("Scheduler: Elevator " + elevatorId + queues[elevatorId].toString());
	}

    private MessageType directElevatorTo(int currentFloor, int toFloor) {
        if (currentFloor == toFloor) {
            return MessageType.STOP;
        }
        if (currentFloor - toFloor > 0) {
            return MessageType.GODOWN;
        }
        return MessageType.GOUP;
    }

    private void sendToElevator(MessageType action, int targetElevator) {
        System.out.println("Scheduler: Sending message of type " + action + "to Elevator " + targetElevator);
        byte[] data = Message.serialize((new ElevatorMessage(action)));
        DatagramPacket pack = new DatagramPacket(data, data.length,
                SimulationVars.elevatorAddresses[targetElevator], SimulationVars.elevatorPorts[targetElevator]);
        Message.send(sendSock, pack);

        switch(action) {
            case GOUP:
                elevators[targetElevator].setState(Elevator.State.MOVING_UP);
                break;
            case GODOWN:
                elevators[targetElevator].setState(Elevator.State.MOVING_DOWN);
                break;
            case STOP:
                elevators[targetElevator].setState(Elevator.State.STOPPED_DOORS_CLOSED);
                //this is a pick up or drop off.. notify floor
            	FloorArrivalMessage m = new FloorArrivalMessage();
            	m.setElevator(targetElevator);
            	m.setFloor(elevators[targetElevator].getFloor());
                sendToFloor(m);
        }
    }

    public void sendToFloor(FloorArrivalMessage m) {
    	//peek at where this elevator is going next
    	ElevatorQueue elevatorQueue = queues[m.getElevator()];
    	if (elevatorQueue.isEmpty()) {
    		m.setDirection(null);
    	} else if (elevatorQueue.peek() > m.getFloor()) {
    		m.setDirection(ElevatorRequestMessage.Direction.UP);
    	} else {
    		m.setDirection(ElevatorRequestMessage.Direction.DOWN);
    	}
    	byte[] data = Message.serialize(m);
    	DatagramPacket pack = new DatagramPacket(data, data.length, SimulationVars.floorAddresses[m.getFloor()], SimulationVars.floorPorts[m.getFloor()]);
    	Message.send(sendSock, pack);
    }

    public void close() {
		elevatorWatcher.interrupt();
        recvSock.close();
        sendSock.close();
    }

    public void run() {
		elevatorWatcher.start();
        try {
            while (true) {
                schedule();
            }
        } catch (Exception e) {
            e.printStackTrace();
            close();
        }
    }

	// The watcher removes faulty elevators and redistributes their requests.
	// An elevator is considered faulty if it has not moved in-between floors
	// within the HARD_FAULT_INTERVAL.
	private void newElevatorWatcher() {
		elevatorWatcher = new Thread(new Runnable() {
			@Override
			public void run() {
				while (!Thread.currentThread().isInterrupted()) {
					handleUnresponsiveElevators();
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt(); // persist the interrupt flag
					}
				}
			}
		});
	}

	private synchronized void handleUnresponsiveElevators() {
		long now = System.currentTimeMillis();
		for (int i=0; i<lastResponses.length; i++) {
			long last = lastResponses[i];
			if (last == -1) {
				// Elevator is unregistered, ignore it.
				continue;
			}
			if (now - last > HARD_FAULT_INTERVAL) {
				// Elevator was resent last message and still hasn't responded.
				// This can now be considered a faulty elevator.
				removeElevator(i);
			} else if (now - last > SOFT_FAULT_INTERVAL) {
				// Elevator was told to move and either is stuck or hasn't received the message.
				// TODO should resend message
				// TODO make mechanism to revisit past message so they can be resent
			}
		}
	}

	private void removeElevator(int id) {
		ElevatorQueue queue = queues[id];
		queues[id] = null;
		elevators[id] = null;
		lastResponses[id] = -1;

		redistributePickupRequests(queue);
	}

	private void redistributePickupRequests(ElevatorQueue queue) {
		// ignore drop off queues, those people are stuck in the unresponsive elevator
		Direction dir = queue.getDirection();
		while (!queue.pickupIsEmpty()) {
			ElevatorRequestMessage msg = new ElevatorRequestMessage(dir, queue.pickupPop());
			handleElevatorRequest(msg);
		}
	}

	public static void main(String args[]) {
		System.out.println("Scheduler: Starting on port 3000");
		Scheduler sched = new Scheduler();
		sched.run();
	}
}
