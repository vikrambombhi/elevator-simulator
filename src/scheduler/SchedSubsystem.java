package scheduler;

import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;

import elevator.ElevatorQueue;
import floor.SimulationVars;
import messages.ElevatorRequestMessage;
import messages.FloorArrivalMessage;
import messages.FloorRequestMessage;
import messages.Message;
import messages.TerminateMessage;
import messages.ElevatorRequestMessage.Direction;

public class SchedSubsystem {
	
	private DatagramSocket recvSock;
	private Thread[] elevatorSchedulers;
	private ElevatorQueue[] elevatorQueues;
	private boolean bExit = false;
	
	public SchedSubsystem(Thread[] eS, ElevatorQueue[] eQ) {
		elevatorSchedulers = eS;
		elevatorQueues = eQ;
		for (int i = 0; i < SimulationVars.numberOfElevators; i++) {
			//elevatorSchedulers[i].run();
		}
		try {
			recvSock = new DatagramSocket(SimulationVars.schedulerPort);
		} catch (SocketException se) {
			se.printStackTrace();
			System.exit(1);
		}
	}
	
	public void schedule() {
		Message m = Message.deserialize(Message.receive(recvSock).getData());
		
		if (m instanceof ElevatorRequestMessage) {
			handleElevatorRequest((ElevatorRequestMessage) m);
			
		} else if (m instanceof FloorArrivalMessage) {
			handleFloorArrival((FloorArrivalMessage) m);
			
		} else if (m instanceof FloorRequestMessage) {
			handleFloorRequest((FloorRequestMessage) m);
			
		} else if (m instanceof TerminateMessage) {
			handleTerminate((TerminateMessage) m);
			
		} else {
			System.out.println("Scheduler: Recieved unrecognized message type");
		}
	}
	
	public void handleElevatorRequest(ElevatorRequestMessage m) {
		//we have to find the best elevator to pick up this passenger
		int lowestBid = -1;
		int bestElevator = 0;
		for (int i = 0; i < SimulationVars.numberOfElevators; i++) {
			if (elevatorQueues[i].getHardFaulted()) {continue;}
			int newBid = pickUpCost(i, m.getDirection(), m.getOriginFloor());
			if (lowestBid > newBid || lowestBid == -1) {
				lowestBid = newBid;
				bestElevator = i;
			}
		}
		synchronized(elevatorQueues[bestElevator]){
			elevatorQueues[bestElevator].addPickUp(m.getOriginFloor(), m.getDirection());
			elevatorQueues[bestElevator].rebalance();
			if (elevatorQueues[bestElevator].size() == 1) {
				elevatorQueues[bestElevator].notifyAll();
			}
			System.out.println("Elevator "+bestElevator+":"+elevatorQueues[bestElevator].toString());
		}
	}
	
	public void handleFloorArrival(FloorArrivalMessage m) {
		int elevator = m.getElevator();
		int floor = m.getFloor();
		synchronized(elevatorQueues[elevator]) {
			elevatorQueues[elevator].setElevatorPosition(floor);
			elevatorQueues[elevator].notifyAll();
		}
	}
	
	public void handleFloorRequest(FloorRequestMessage m) {
		int elevator = m.getElevator();
		synchronized(elevatorQueues[elevator]) {
			elevatorQueues[elevator].addDropOff(m.getDestination());
			elevatorQueues[elevator].rebalance();
			elevatorQueues[elevator].notifyAll();
			System.out.println("Elevator "+elevator+":"+elevatorQueues[elevator].toString());
		}
	}
	
	public void handleTerminate(TerminateMessage m) {
		bExit = true;
	}
	
	public int pickUpCost(int elevator, Direction d, int f) {
		synchronized (elevatorQueues[elevator]) {
			if (elevatorQueues[elevator].contains(f)) {
				return 0;
			} else if (elevatorQueues[elevator].isEmpty()) {
				return Math.abs(elevatorQueues[elevator].getElevatorPosition() - f)+1;
			} else if (elevatorQueues[elevator].onRoute(f)) {
				return (elevatorQueues[elevator].floorsAway(f) * elevatorQueues[elevator].size())+1;
			} else {
				return (Math.abs(elevatorQueues[elevator].getElevatorPosition() - f) * elevatorQueues[elevator].size())+1;
			}
		}
	}
	
	public void run() {
		while(!bExit) {
			schedule();
		}
	}
	
	public static void main(String args[]) {
		ElevatorQueue[] queues = new ElevatorQueue[SimulationVars.numberOfElevators];
		Thread[] schedulers = new Thread[SimulationVars.numberOfElevators];
		for (int i = 0; i < SimulationVars.numberOfElevators; i++) {
			queues[i] = new ElevatorQueue();
			schedulers[i] = new Thread(new ElevatorScheduler(i ,queues[i]));
		}
		for (int i = 0; i < SimulationVars.numberOfElevators; i++) {
			schedulers[i].start();
		}
		
		Thread queueCleaner = new Thread (new QueueCleaner(queues));
		queueCleaner.start();
		
		System.out.println("Scheduler: Starting on port " + SimulationVars.schedulerPort);
		SchedSubsystem schedSubsystem = (new SchedSubsystem(schedulers, queues));

		schedSubsystem.run();
		

		
		System.out.println("System exiting...");
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
		}
		System.exit(0);
	}
}
