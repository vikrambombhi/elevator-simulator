package scheduler;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import elevator.ElevatorQueue;
import floor.SimulationVars;
import messages.ElevatorMessage;
import messages.ElevatorMessage.MessageType;
import messages.ElevatorRequestMessage.Direction;
import messages.FloorArrivalMessage;
import messages.Message;

public class Scheduler implements Runnable {
	
	private int id;
	private ElevatorQueue elevatorQueue;
	private DatagramSocket sendSocket;
	private boolean faultSuspected = false;
	
	public Scheduler(int i, ElevatorQueue e) {
		id = i;
		elevatorQueue = e;
		try {
			sendSocket = new DatagramSocket();
		} catch (SocketException se) {
			se.printStackTrace();
			System.exit(1);
		}
	}

	@Override
	public void run() {
		int nextLocation = 0;
		System.out.println("Scheduler: starting elevator " + id + " controller");
		while (true) {
			try {
				synchronized(elevatorQueue) {
					elevatorQueue.wait((long)(SimulationVars.elevatorTravelTime*2));
				}
			} catch (InterruptedException e) {}
			
			//the elevator isn't where we expect it to be...
			if (nextLocation != elevatorQueue.getElevatorPosition()) {
				//and it wasn't where we expected last time as well
				if(faultSuspected) {
					//hard fault
					System.out.println("Scheduler "+id+": Hard fault confirmed, awaiting pick up redistrobution");
					elevatorQueue.setHardFaulted(true);
					break;
				} else {
					faultSuspected = true;
					System.out.println("Scheduler "+id+": Possible fault suspected");
				}
			} else if (faultSuspected) {
				System.out.println("Scheduler "+id+": Soft fault confirmed and resolved");
				faultSuspected = false;
			}
			
			ElevatorMessage m = new ElevatorMessage();
			DatagramPacket pack;
			byte[] data;
			if (elevatorQueue.peek() == elevatorQueue.getElevatorPosition()) {
				
				//stop elevator
				m.setMessageType(MessageType.STOP);
				data = Message.serialize(m);
				pack = new DatagramPacket(data, data.length, SimulationVars.elevatorAddresses[id], SimulationVars.elevatorPorts[id]);
				Message.send(sendSocket, pack);
				
				//notify floor
				int floor = elevatorQueue.getElevatorPosition();
				FloorArrivalMessage f = new FloorArrivalMessage();
				f.setDirection(elevatorQueue.directionPeek());
				f.setElevator(id);
				f.setFloor(floor);
				data = Message.serialize(f);
				pack = new DatagramPacket(data, data.length, SimulationVars.floorAddresses[floor], SimulationVars.floorPorts[floor]);
				Message.send(sendSocket, pack);
				
				//remove from queue
				elevatorQueue.remove();
				elevatorQueue.rebalance();
				System.out.println("Elevator "+id+":"+elevatorQueue.toString());
				
				//this is a pickup, we need to wait until we receive their destination to leave
				if (f.getDirection() != null) {
					try {
						synchronized(elevatorQueue) {
							elevatorQueue.wait();
						}
					} catch (InterruptedException e) {}
				}
			} 
			if (!elevatorQueue.isEmpty()) {
				if (elevatorQueue.peek() > elevatorQueue.getElevatorPosition()) {
					m.setMessageType(MessageType.GOUP);
					nextLocation = elevatorQueue.getElevatorPosition() + 1;
				} else {
					m.setMessageType(MessageType.GODOWN);
					nextLocation = elevatorQueue.getElevatorPosition() - 1;
				}
				data = Message.serialize(m);
				pack = new DatagramPacket(data, data.length, SimulationVars.elevatorAddresses[id], SimulationVars.elevatorPorts[id]);
				Message.send(sendSocket, pack);
			} else {
				nextLocation = elevatorQueue.getElevatorPosition();
			}
		}
		System.out.println("Elevator "+id+" Exitting");
	}
}
