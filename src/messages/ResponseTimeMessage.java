package messages;

import java.io.Serializable;

public class ResponseTimeMessage implements Serializable, Message{

	/**
	 * 
	 */
	private static final long serialVersionUID = 4914989113229117250L;
	
	public enum Subsystem {ArrivalSensor, RequestSimulator, DestinationSender};
	private Subsystem subsystem;
	private long time;

	public ResponseTimeMessage() {
		
	}
	
	public void setTime(long t) {
		time = t;
	}
	
	public void setSubsystem(Subsystem s) {
		subsystem = s;
	}
	
	public long getTime() {
		return time;
	}
	
	public Subsystem getSubsystem() {
		return subsystem;
	}
}
