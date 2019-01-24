package messages;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public interface Message {
	// When receiving a message, deserialize it with this, and cast it using
	// instanceof
	public static Message deserialize(byte[] data) {
		ByteArrayInputStream in = new ByteArrayInputStream(data);
		Message m = null;
		try {
			ObjectInputStream is = new ObjectInputStream(in);
			m = (Message) is.readObject();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return m;
	}

	// This works with empty bytes postpended
	// don't worry about message sizes or types, just cast
	public static byte[] serialize(Message obj) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			ObjectOutputStream os = new ObjectOutputStream(out);
			os.writeObject(obj);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return out.toByteArray();
	}
}
