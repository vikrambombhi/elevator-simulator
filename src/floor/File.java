package floor;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;
import messages.*;
import java.util.*;

public class File {
	public File() {
	}

	public ArrayList<messages.ElevatorRequestMessage> readFile(String filename) {
		ArrayList<messages.ElevatorRequestMessage> messages = new ArrayList<messages.ElevatorRequestMessage>();
		try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
			String line;
			while ((line = br.readLine()) != null) {
				System.out.printf("processing line: %s\n", line);
				String[] parts = line.split(" ");

				ElevatorRequestMessage msg = new ElevatorRequestMessage();
				msg.setDirection(
						parts[2] == "Up" ? ElevatorRequestMessage.Direction.UP : ElevatorRequestMessage.Direction.DOWN);
				msg.setOriginFloor(Integer.parseInt(parts[1]));
				messages.add(msg);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return messages;
	}
}
