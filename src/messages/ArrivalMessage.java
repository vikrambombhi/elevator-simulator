package messages;

import java.io.Serializable;

// the arrival sensors use this to tell the scheduler when an elevator arrives at a floor
public class ArrivalMessage implements Message, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7521529484689926656L;

	public ArrivalMessage() {

	}

}
