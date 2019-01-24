package scheduler;

import java.io.*;
import java.net.*;
import java.util.Arrays;

public class Scheduler {
  DatagramPacket sendPacket, receivePacket;
  DatagramSocket sendAndReceive, receiveSocket;

  public Scheduler() {
    try {
      // Construct a datagram socket and bind it to any available
      // port on the local host machine. This socket will be used to
      // send UDP Datagram packets.
      sendAndReceive = new DatagramSocket();

      // Construct a datagram socket and bind it to port 23
      // on the local host machine. This socket will be used to
      // receive UDP Datagram packets.
      receiveSocket = new DatagramSocket(3000);

      // to test socket timeout (2 seconds)
      // receiveSocket.setSoTimeout(2000);
    } catch (SocketException se) {
      se.printStackTrace();
      System.exit(1);
    }
  }

  public void receive(byte[] data, DatagramSocket socket) {
    receivePacket = new DatagramPacket(data, data.length);
    System.out.println("Scheduler: Waiting for Packet.\n");

    // Block until a datagram packet is received from receiveSocket.
    try {
      socket.receive(receivePacket);
    } catch (IOException e) {
      System.out.print("IO Exception: likely:");
      System.out.println("Receive Socket Timed Out.\n" + e);
      e.printStackTrace();
      System.exit(1);
    }
  }

  public void send() {
    System.out.println("Scheduler: Sending packet:");
    int len = sendPacket.getLength();
    String str  = new String(sendPacket.getData(), 0, len);

    // Send the datagram packet to the client via the send socket.
    try {
      sendAndReceive.send(sendPacket);
    } catch (IOException e) {
      e.printStackTrace();
      System.exit(1);
    }
  }

  public void receiveAndForward() {
    byte data[] = new byte[100];
    receive(data, receiveSocket);

    InetAddress originAddress = receivePacket.getAddress();
    int originPort = receivePacket.getPort();

    // create new data packet but change the address to the server
    try {
      sendPacket = new DatagramPacket(data, receivePacket.getLength(), InetAddress.getLocalHost(), 4000);
    } catch (UnknownHostException e) {
      e.printStackTrace();
      System.exit(1);
    }

    // forward
    send();

    // get upstream response
    data = new byte[100];
    receive(data, sendAndReceive);

    sendPacket = new DatagramPacket(data, receivePacket.getLength(), originAddress, originPort);
    send();
  }

  public void close() {
    sendAndReceive.close();
    receiveSocket.close();
  }

  public void run() {
    try  {
      while(true) {
        receiveAndForward();
      }
    } catch (Exception e) {
      close();
    }
  }

  public static void main(String args[]) {
    Scheduler proxy = new Scheduler();
    proxy.run();
  }
}
