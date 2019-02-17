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
import messages.ElevatorMessage;
import messages.ElevatorMessage.MessageType;
import messages.ElevatorRequestMessage;
import messages.FloorArrivalMessage;
import messages.FloorRequestMessage;
import messages.Message;
import floor.SimulationVars;

public class Scheduler {
    public static String HOST = "127.0.0.1";
    public static short PORT = 3000;

    private DatagramSocket recvSock, sendSock;
    private LinkedList<Integer>[] queues;
    private Elevator[] elevators;

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
        queues = new LinkedList[SimulationVars.numberOfElevators];
        for (int i = 0; i < SimulationVars.numberOfElevators; i++) {
            queues[i] = new LinkedList<Integer>();
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
            handleFloorArrival((FloorArrivalMessage) m);
        } else if (m instanceof FloorRequestMessage) {
            handleFloorRequest((FloorRequestMessage) m);
        }
    }

    private void handleElevatorRequest(ElevatorRequestMessage m) {
        // add request to pick up elevators
        Integer emptyQueueIndex = null;
        for (int i = 0; i < queues.length; i ++) {
            if (queues[i].isEmpty()) {
                emptyQueueIndex = i;
            }
        }
        if (emptyQueueIndex != null) {
            addAndSort(emptyQueueIndex, m.getOriginFloor());
            int currentFloor = elevators[emptyQueueIndex].getFloor();
            sendToElevator(directElevatorTo(currentFloor, m.getOriginFloor()), emptyQueueIndex);
            return;
        }

        // find elevators going the same direction and can hit
        Elevator targetElevator = null;
        int smallestDiff = SimulationVars.numberOfFloors;
        for (Elevator e: elevators) {
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
            queues[targetElevator.getId()].add(m.getOriginFloor());
            return;
        }

        // find smallest queue size
        LinkedList<Integer> smallestQueue = queues[0];
        Integer smallestQueueIndex = null;
        for (int i = 1; i < queues.length; i ++) {
            if (queues[i].size() < smallestQueue.size()) {
                smallestQueue = queues[i];
                smallestQueueIndex = i;
            }
        }
        addAndSort(smallestQueueIndex, m.getOriginFloor());
    }

    private void handleFloorArrival(FloorArrivalMessage m) {
        // update elevator model
        elevators[m.getElevator()].setFloor(m.getFloor());
        LinkedList<Integer> elevatorQueue = queues[m.getElevator()];

        if (elevatorQueue.isEmpty()) {
            return;
        }
        // when an elevator arrives, tell it to go up or down, depending on the queues
        System.out.printf("Scheduler: Elevator %d arrived at floor %d\n", m.getElevator(), m.getFloor());
        // tell elevator to go up, down, or stop & open
        int destination = elevatorQueue.peek();
        if (destination == m.getFloor()) {
            System.out.printf("Scheduler: Elevator %d dequeuesing floor %d\n", m.getElevator(), destination);
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

    private void handleFloorRequest(FloorRequestMessage m) {
        // this means that an elevator is leaving a floor
        // we know what floor buttons were pressed
        LinkedList<Integer> elevatorQueue = queues[m.getElevator()];
        if (!elevatorQueue.isEmpty()) {
            int destination = elevatorQueue.peek();
            if (destination == m.getCurrent()) {
                System.out.printf("Scheduler: Elevator %d dequeuesing floor %d\n", m.getElevator(), destination);
                elevatorQueue.remove();
            }
        }
        // enqueues requested floors
        addAndSort(m.getElevator(), m.getDestination());
        System.out.println("Scheduler: Elevator " + m.getElevator() + " queue: " + elevatorQueue.toString());

        // send the elevator on its way
        sendToElevator(directElevatorTo(m.getCurrent(), elevatorQueue.peek()), m.getElevator());
    }

	// addAndSort addes the requested floor to the elevator's queue and sorts to stop
	// on floors on the way.
	private void addAndSort(int elevatorId, int floor) {
		queues[elevatorId].add(floor);
		switch (elevators[elevatorId].getState()) {
			case MOVING_UP:
				Collections.sort(queues[elevatorId]);
				break;
			case MOVING_DOWN:
				Collections.sort(queues[elevatorId], new Comparator<Integer>() {
					@Override
					public int compare(Integer int1, Integer int2) {
						return int2 - int1;
					}
				});
				break;
		}
        System.out.println("Scheduler: Elevator " + elevatorId +  " queue: " + queues[elevatorId].toString());
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
                // TODO is it okay to leave to ignore stop state? can we just assumed after it's stopped,
                // the elevator will continue in the current direction?
        }
    }
    
    public void sendToFloor(FloorArrivalMessage m) {
    	//peek at where this elevator is going next
    	LinkedList<Integer> elevatorQueue = queues[m.getElevator()];
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
        recvSock.close();
        sendSock.close();
    }

    public void run() {
        try {
            while (true) {
                // sendToElevator(MessageType.GOUP);
                schedule();
            }
        } catch (Exception e) {
            e.printStackTrace();
            close();
        }
    }

    public static void main(String args[]) {
        System.out.println("Scheduler: Starting on port 3000");
        Scheduler sched = new Scheduler();
        sched.run();
    }
}