package scheduler;

import elevator.Elevator;
import elevator.ElevatorQueue;
import elevator.ElevatorShaft;
import floor.SimulationVars;
import messages.ElevatorMessage;
import messages.ElevatorMessage.MessageType;
import messages.ElevatorRequestMessage;
import messages.ElevatorRequestMessage.Direction;
import messages.FloorArrivalMessage;
import messages.FloorRequestMessage;
import messages.Message;

public class FaultDetector implements Runnable {
    // 10 seconds which is 2 times the SimulationVars.elevatorTravelTime
    private static long FAULT_INTERVAL = 10 * 1000 / SimulationVars.timeScalar; // 10 seconds

    private Scheduler scheduler;
    private SchedulerMessenger messenger;

    public FaultDetector(Scheduler s, SchedulerMessenger m) {
        scheduler = s;
        messenger = m;
    }

    // The watcher removes faulty elevators and redistributes their requests.
    // An elevator is considered faulty if it has not moved in-between floors
    // within the HARD_FAULT_INTERVAL.
    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            handleUnresponsiveElevators();
            try {
                Thread.sleep(1000 / SimulationVars.timeScalar);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // persist the interrupt flag
            }
        }
    }

    public int handleUnresponsiveElevators() {
        long now = System.currentTimeMillis();

        for (int i = 0; i < SimulationVars.numberOfElevators; i++) {
            ElevatorShaft shaft = scheduler.getElevatorShaft(i);
            synchronized (shaft) {
                if (shaft == null) {
                    // Elevator is unregistered, ignore it.
                    continue;
                }

                long last = shaft.getLastResponse();
                long diff = (now - last) / SimulationVars.timeScalar;
                if (diff > FAULT_INTERVAL) {
                    // check state. Is it
                    if (shaft.getElevator().isMoving()) {
                        // Elevator was resent last message and still hasn't responded.
                        // This can now be considered a faulty elevator.

                        // Don't remove the elevator if it's queue is empty. That just means
                        // it has no work to do.
                        if (!shaft.getQueue().isEmpty()) {
                            System.out.println("FaultDetector: Hard fault on elevator " + i);
                            scheduler.removeElevator(i);
                        }
                        return 1;
                    } else {
                        // soft fault, resend message

                        int destination = shaft.getCurrentDestination();
                        if (destination == shaft.getElevator().getFloor()) {
                            // System.out.print("FaultDetector: elevator soft faulted on destination floor,
                            // ignoring.");
                            return 0;
                        }
                        System.out.println(
                                "FaultDetector: elevator soft faulted, sending elevator " + i + " to floor " + destination);
                        if (destination == scheduler.getElevatorShaft(i).getElevator().getFloor()) {
                            destination = shaft.getQueue().peek();
                            if (destination == -1) {
                                // do nothing, detected soft fault with no where to go
                                return 0;
                            }
                            System.out.println(
                                    "FaultDetector: elevator soft faulted at destination, now sending it to " + destination);
                        }
                        messenger.sendToElevator(shaft.getElevator().getFloor(), destination, shaft);
                        return 2;
                    }
                }
            }
        }
        return 0;
    }
}
