package messages;

import java.io.Serializable;

public class ElevatorMessage implements Message, Serializable {

    private static final long serialVersionUID = -2950505368297636750L;

    public enum MessageType {
        STOP, GOUP, GODOWN, OPENDOOR, CLOSEDOOR;
    }

    MessageType messageType;

    /*
     * Constructor for empty elevator message
     */
    public ElevatorMessage() {}

    /*
     * Constructor for elevator message
     *
     * @param messageType   The type of message to create
     */
    public ElevatorMessage(MessageType messageType) {
        this.messageType = messageType;
    }

    /*
     * Returns the type of message
     *
     * @return the type of message
     */
    public MessageType getMessageType() {
        return this.messageType;
    }

    /*
     * Set message type
     *
     * @param messageType   Message type to set
     */
    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }
}
