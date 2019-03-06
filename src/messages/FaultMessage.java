package messages;

import java.io.Serializable;

public class FaultMessage implements Message, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3532662599622451499L;

	private boolean hardFault;
	private int elevator;
	
	public FaultMessage() {
		
	}
	
	public void setHardFault(boolean b) {
		hardFault = b;
	}
	
	public void setSoftFault(boolean b) {
		hardFault = !b;
	}
	
	public boolean getHardFault() {
		return hardFault;
	}
	
	public boolean getSoftFault() {
		return !hardFault;
	}
	
	public int getElevator() {
		return elevator;
	}
	
	public void setElevator(int i) {
		elevator = i;
	}
	
}
