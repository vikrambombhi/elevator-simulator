package elevator;

import java.io.*;
import java.net.*;
import java.util.Arrays;

public class Elevator {
  // SimpleEchoElevator.java
  // This class is the server side of a simple echo server based on
  // UDP/IP. The server receives from a client a packet containing a character
  // string, then echoes the string back to the client.
  // Last edited January 9th, 2016

  DatagramPacket sendPacket, receivePacket;
  DatagramSocket sendSocket, receiveSocket;

  public Elevator() {
    try {
      // Construct a datagram socket and bind it to any available
      // port on the local host machine. This socket will be used to
      // send UDP Datagram packets.
      sendSocket = new DatagramSocket();

      // Construct a datagram socket and bind it to port 5000
      // on the local host machine. This socket will be used to
      // receive UDP Datagram packets.
      receiveSocket = new DatagramSocket(4000);

      // to test socket timeout (2 seconds)
      // receiveSocket.setSoTimeout(2000);
    } catch (SocketException se) {
      se.printStackTrace();
      System.exit(1);
    }
  }

  private void receive(byte[] data) {
    receivePacket = new DatagramPacket(data, data.length);
    System.out.println("Elevator: Waiting for Packet.\n");

    // Block until a datagram packet is received from receiveSocket.
    try {
      receiveSocket.receive(receivePacket);
    } catch (IOException e) {
      System.out.print("IO Exception: likely:");
      System.out.println("Receive Socket Timed Out.\n" + e);
      e.printStackTrace();
      System.exit(1);
    }

  }

  private void send() {
    System.out.println("Elevator: Sending packet:");

    // Send the datagram packet to the client via the send socket.
    try {
      sendSocket.send(sendPacket);
    } catch (IOException e) {
      e.printStackTrace();
      System.exit(1);
    }
  }

  public void run() {
    try  {
      while(true) {
        byte data[] = new byte[100];
        receive(data);

        sendPacket = new DatagramPacket(data, data.length, receivePacket.getAddress(), receivePacket.getPort());

        send();
      }
    } catch (Exception e) {
        System.out.println("Elevator quiting.");
        e.printStackTrace();
        close();
    }
  }

  public void close() {
    sendSocket.close();
    receiveSocket.close();
  }

  public static void main(String args[]) {
    Elevator s = new Elevator();
    s.run();
  }
}
