package scheduler;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.concurrent.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import elevator.Elevator;
import elevator.ElevatorQueue;
import floor.SimulationVars;
import messages.ElevatorMessage;
import messages.ElevatorMessage.MessageType;
import messages.ElevatorRequestMessage;
import messages.ElevatorRequestMessage.Direction;
import messages.FloorArrivalMessage;
import messages.FloorRequestMessage;
import messages.Message;

public class SchedulerSubSystem {
	private Scheduler scheduler;
	private SchedulerMessenger messenger;
	private Thread faultDetectorThread;
	private ExecutorService threadPool;
	private List<Long> elevatorRequestTimes;
	private List<Long> floorArrivalTimes;
	private List<Long> floorRequestTimes;

	public SchedulerSubSystem() {
		elevatorRequestTimes = Collections.synchronizedList(new ArrayList<Long>());
		floorArrivalTimes = Collections.synchronizedList(new ArrayList<Long>());
		floorRequestTimes = Collections.synchronizedList(new ArrayList<Long>());
		threadPool = Executors.newFixedThreadPool(SimulationVars.numberOfElevators);

		messenger = new SchedulerMessenger();
		scheduler = new Scheduler(messenger);

		FaultDetector faultDetector = new FaultDetector(scheduler, messenger);
		faultDetectorThread = new Thread(faultDetector);
	}

	public void schedule() {
		System.out.println("SchedulerSubSystem: Waiting for message");

		Message m = messenger.receive();


		if (m instanceof ElevatorRequestMessage) {
			Long initTime = System.nanoTime();
			threadPool.execute(new Runnable() {
				@Override
				public void run() {
					handleElevatorRequest((ElevatorRequestMessage) m);
					elevatorRequestTimes.add(initTime - System.nanoTime());
				}
			});
		} else if (m instanceof FloorArrivalMessage) {
			Long initTime = System.nanoTime();
			threadPool.execute(new Runnable() {
				@Override
				public void run() {
					handleFloorArrival((FloorArrivalMessage) m);
					floorArrivalTimes.add(initTime - System.nanoTime());
				}
			});
		} else if (m instanceof FloorRequestMessage) {
			Long initTime = System.nanoTime();
			threadPool.execute(new Runnable() {
				@Override
				public void run() {
					handleFloorRequest((FloorRequestMessage) m);
					floorRequestTimes.add(initTime - System.nanoTime());
				}
			});
		}
	}

	private void handleElevatorRequest(ElevatorRequestMessage m) {
		if (!scheduler.anyAvailableElevators()) {
			close();
			System.out.println("SchedulerSubSystem: No more available elevators, exiting");
			System.exit(0);
		}
		scheduler.queuePickUp(m.getDirection(), m.getOriginFloor());
	}

	private void handleFloorArrival(FloorArrivalMessage m) {
		if (scheduler.getElevatorShaft(m.getElevator()) == null) {
			// drop message
			return;
		}
		scheduler.updateQueueState(m.getDirection(), m.getElevator(), m.getFloor());
	}

	private void handleFloorRequest(FloorRequestMessage m) {
		if (scheduler.getElevatorShaft(m.getElevator()) == null) {
			// drop message
			return;
		}
		scheduler.queueDropOff(m.getElevator(), m.getCurrent(), m.getDestination());
	}

	public void close() {
		faultDetectorThread.interrupt();
		messenger.close();
	}

	public void run() {
		faultDetectorThread.start();
		try {
			while (true) {
				schedule();
			}
		} catch (Exception e) {
			e.printStackTrace();
			close();
		}
	}

	public static void main(String args[]) {
		System.out.println("Scheduler: Starting on port 3000");
		SchedulerSubSystem s = new SchedulerSubSystem();
		s.run();
	}
}
