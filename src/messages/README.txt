Message classes should be prepended with the name of the subsystem they come from. Eg ElevatorMessage is a message from the elevator subsystem

 - Adding a new message class

Create a class that implements Message and Serializable

add whatever fields needed to the subclass, including a randomly generated serialVersionUID (eclipse should offer to do this for you)

Message.serialize will take any class that implements Message

Message.deserialize will return a Message that can be casted with instanceof to whatever message type you want. This will include fields defined in the message subclass.


Example:

sender (elevator subsystem):

ElevatorMessag m = new ElevatorMessage();
// surround with try catch
byte[] message = Message.serialize(m);
// put message in a datagram packet and send it

receiver (any subsystem):

byte[] message = // from datagram packet
// surround with try catch
Message m = Message.deserialize(message);

switch (m) {
  if (m instanceof ElevatorMessage) {
    // handle the message
  }
}

