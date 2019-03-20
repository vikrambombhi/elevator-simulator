package scheduler;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

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
	private FaultDetector faultDetector;
	private Thread faultDetectorThread;

	public SchedulerSubSystem() {
		messenger = new SchedulerMessenger();
		scheduler = new Scheduler(messenger);
		faultDetector = new FaultDetector(scheduler, messenger);
		faultDetectorThread = new Thread(faultDetector);
	}

	public void schedule() {
		System.out.println("SchedulerSubSystem: Waiting for message");

		Message m = messenger.receive();

		if (m instanceof ElevatorRequestMessage) {
			handleElevatorRequest((ElevatorRequestMessage) m);
		} else if (m instanceof FloorArrivalMessage) {
			handleFloorArrival((FloorArrivalMessage) m);
		} else if (m instanceof FloorRequestMessage) {
			handleFloorRequest((FloorRequestMessage) m);
		}
	}

	public void handleElevatorRequest(ElevatorRequestMessage m) {
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
