package scheduler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import floor.SimulationVars;
import messages.ElevatorRequestMessage;
import messages.FloorArrivalMessage;
import messages.FloorRequestMessage;
import messages.Message;
import messages.TerminateMessage;

public class SchedulerSubSystem {
	private Scheduler scheduler;
	private SchedulerMessenger messenger;
	private Thread faultDetectorThread;
	private ExecutorService threadPool;
	private List<Long> elevatorRequestTimes;
	private List<Long> floorArrivalTimes;
	private List<Long> floorRequestTimes;
	private List<Long> schedulerTimes; // list of times in ns to run scheduler once
	private boolean bExit = false;

	public SchedulerSubSystem() {
		elevatorRequestTimes = Collections.synchronizedList(new ArrayList<Long>());
		floorArrivalTimes = Collections.synchronizedList(new ArrayList<Long>());
		floorRequestTimes = Collections.synchronizedList(new ArrayList<Long>());
		threadPool = Executors.newFixedThreadPool(SimulationVars.numberOfElevators);
		schedulerTimes = Collections.synchronizedList(new ArrayList<Long>());

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
					elevatorRequestTimes.add(System.nanoTime() - initTime);
				}
			});
		} else if (m instanceof FloorArrivalMessage) {
			Long initTime = System.nanoTime();
			threadPool.execute(new Runnable() {
				@Override
				public void run() {
					handleFloorArrival((FloorArrivalMessage) m);
					floorArrivalTimes.add(System.nanoTime() - initTime);
				}
			});
		} else if (m instanceof FloorRequestMessage) {
			Long initTime = System.nanoTime();
			threadPool.execute(new Runnable() {
				@Override
				public void run() {
					handleFloorRequest((FloorRequestMessage) m);
					floorRequestTimes.add(System.nanoTime() - initTime);
				}
			});
		} else if (m instanceof TerminateMessage) {
			handleTerminateMessage((TerminateMessage) m);
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

	private void handleTerminateMessage(TerminateMessage m) {
		String erTimes = Arrays.toString(elevatorRequestTimes.toArray());
		String faTimes = Arrays.toString(floorArrivalTimes.toArray());
		String frTimes = Arrays.toString(floorRequestTimes.toArray());
		System.out.println("Scheduler: Elevator Request Response Times (nano) - " + erTimes);
		System.out.println("Scheduler: Floor Arrival Response Times (nano) - " + faTimes);
		System.out.println("Scheduler: Floor Request Response Times (nano) - " + frTimes);
		System.out.println(
				"Scheduler: Times to send messages (nano) - " + Arrays.toString(messenger.getMessageTimes().toArray()));
		System.out
				.println("Scheduler: Average time to send a message (nano) - " + average(messenger.getMessageTimes()));
		List<Long> tmp = Collections.synchronizedList(new ArrayList<Long>());
		tmp.addAll(elevatorRequestTimes);
		tmp.addAll(floorArrivalTimes);
		tmp.addAll(floorRequestTimes);
		System.out.println("Scheduler: Average time to run scheduler once (nano) - " + average(tmp));
		bExit = true;
	}

	private long sum(List<Long> times) {
		long sum = 0;
		for (Long l : times) {
			sum += l;
		}
		return sum;
	}

	// this method works... sum times
	private long average(List<Long> times) {
		return sum(times) / times.size();
	}

	public void close() {
		faultDetectorThread.interrupt();
		messenger.close();
	}

	public void run() {
		faultDetectorThread.start();
		try {
			while (!bExit) {
				schedule();
			}
			close();
		} catch (Exception e) {
			e.printStackTrace();
			close();
		}
	}

	public static void main(String args[]) {
		System.out.println("Scheduler: Starting on port 3000");
		SchedulerSubSystem s = new SchedulerSubSystem();
		s.run();
		System.out.println("System exiting...");
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
		}
		System.exit(0);
	}
}
