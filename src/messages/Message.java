package messages;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

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

	public static void send(DatagramSocket sock, DatagramPacket pack) {
		try {
			sock.send(pack);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	public static DatagramPacket receive(DatagramSocket sock) {
		byte data[] = new byte[100];
		DatagramPacket pack = new DatagramPacket(data, data.length);

		// Block until a datagram packet is received from receiveSocket.
		try {
			sock.receive(pack);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		return pack;
	}
}
