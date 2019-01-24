package messages;

// This is the class that the messages inherit from
// it has a toObject method that you use after switching on
// the byte array from a packet
public abstract class Message {
	// Take a byte array and return a type of message
	// The caller will switch on the first bytes of an array
	// then call this and cast it to whatever subclass they want.
	public abstract Message toObject(byte[] message);
}
