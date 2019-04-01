package floor;

import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import messages.ElevatorRequestMessage;
import messages.Message;
import messages.ResponseTimeMessage;
import messages.ResponseTimeMessage.Subsystem;
import messages.TerminateMessage;

public class ResponseTimer implements Runnable{
	private List<Long> elevatorRequestTimes;
	private List<Long> floorArrivalTimes;
	private List<Long> floorRequestTimes;
	private DatagramSocket sock;
	private boolean bExit = false;
	
	public ResponseTimer() {
		elevatorRequestTimes = Collections.synchronizedList(new ArrayList<Long>());
		floorArrivalTimes = Collections.synchronizedList(new ArrayList<Long>());
		floorRequestTimes = Collections.synchronizedList(new ArrayList<Long>());
		
		try {
			sock = new DatagramSocket(SimulationVars.timerPort);
		} catch (SocketException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	@Override
	public void run() {
		while (!bExit) {
			Message m = Message.deserialize(Message.receive(sock).getData());
			if (m instanceof ResponseTimeMessage) {
				if (((ResponseTimeMessage) m).getSubsystem() == Subsystem.ArrivalSensor) {
					floorArrivalTimes.add(((ResponseTimeMessage) m).getTime());
				} else if (((ResponseTimeMessage) m).getSubsystem() == Subsystem.DestinationSender) {
					floorRequestTimes.add(((ResponseTimeMessage) m).getTime());
				} else {
					elevatorRequestTimes.add(((ResponseTimeMessage) m).getTime());
				}
			} else if (m instanceof TerminateMessage) {
				handleTerminateMessage();
			} else {
				System.out.println("Response Timer: unknown message type received");
			}
		}
	}
	
	public void handleTerminateMessage() {
		String erTimes = Arrays.toString(elevatorRequestTimes.toArray());
		String faTimes = Arrays.toString(floorArrivalTimes.toArray());
		String frTimes = Arrays.toString(floorRequestTimes.toArray());
		System.out.println("Timer: Elevator Request Response Times (nano) Average: " + average(elevatorRequestTimes)
				+ ", Variance: " + sampleVariance(elevatorRequestTimes) + " - " + erTimes);
		System.out.println("Timer: Floor Arrival Response Times (nano) Average: " + average(floorArrivalTimes)
				+ ", Variance: " + sampleVariance(floorArrivalTimes) + " - " + faTimes);
		System.out.println("Timer: Floor Request Response Times (nano) Average: " + average(floorRequestTimes)
				+ ", Variance: " + sampleVariance(floorRequestTimes) + " - " + frTimes);
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

	private double sampleVariance(List<Long> times) {
		long mean = average(times);
		long sum = 0;
		for (Long l : times) {
			long xi = l - mean;
			sum += xi * xi;
		}
		return sum / (times.size() - 1);
	}
}
