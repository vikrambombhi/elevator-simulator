package elevator;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import messages.*;

/*
 * Elevator is the subsystem placed in each elevator.
 */
public class Elevator {
    // State is the possible states that the elevator can be in.
    enum State {
        MOVING_UP,
        MOVING_DOWN,
        STOPPED_DOORS_CLOSED,
        STOPPED_DOORS_OPENED
    }

    State state;

    // Simulates the physical attached with the elevator.
    Motor motor;
    Door door;

    // Communication sockets
    DatagramSocket sendSocket, receiveSocket;

    public Elevator() {
        state = State.STOPPED_DOORS_CLOSED;
        motor = new Motor();
        door = new Door();

        try {
            sendSocket = new DatagramSocket();
            receiveSocket = new DatagramSocket(4000);
        } catch (SocketException se) {
            se.printStackTrace();
            System.exit(1);
        }
    }

    /*
     * run starts the elevator subsystem and awaits for messages.
     */
    public void run() {
        try {
            while (true) {
                byte data[] = new byte[100];
                DatagramPacket receivePacket = Message.receive(receiveSocket, data);
                Message m = Message.deserialize(receivePacket.getData());
                handleMessage(m);
            }
        } catch (Exception e) {
            System.out.println("Elevator quiting.");
            e.printStackTrace();
            close();
        }
    }

    /*
     * close closes the communication sockets of the elevator subsystem.
     */
    public void close() {
        sendSocket.close();
        receiveSocket.close();
    }

    /*
     * handleMessage runs the corresponding action for the message type.
     */
    private void handleMessage(Message m) {
        // State machine switch
        if (m instanceof ElevatorMessage) {
            // scheduler tells the elevator to move, stop, open or close.
            handleElevatorMessage((ElevatorMessage) m);
            // } else if (m instanceof FloorMessage) {
            //     reroute the message to the schduler
            }
    }

    /*
     * handleElevatorMessage handles commands for the elevator's physical components.
     */
    private void handleElevatorMessage(ElevatorMessage m) {
        switch (m.getMessageType()) {
            case STOP:
                assert(state == State.MOVING_UP || state == State.MOVING_DOWN);
                motor.stop();
                state = State.STOPPED_DOORS_CLOSED;
                break;

            case GOUP:
                assert(state == State.STOPPED_DOORS_CLOSED);
                motor.move(Motor.Direction.UP);
                state = State.MOVING_UP;
                break;

            case GODOWN:
                assert(state == State.STOPPED_DOORS_CLOSED);
                motor.move(Motor.Direction.DOWN);
                state = State.MOVING_DOWN;
                break;

            case OPENDOOR:
                assert(state == State.STOPPED_DOORS_CLOSED);
                door.open();
                state = State.STOPPED_DOORS_OPENED;
                break;

            case CLOSEDOOR:
                assert(state == State.STOPPED_DOORS_OPENED);
                door.close();
                state = State.STOPPED_DOORS_CLOSED;
                break;
        }
    }

    public static void main(String args[]) {
        System.out.println("Elevator: Starting on port 4000");
        Elevator e = new Elevator();
        e.run();
    }
}
