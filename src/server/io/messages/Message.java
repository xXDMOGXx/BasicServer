package server.io.messages;

public class Message {
    public MessageType type;
    public int[] values;

    // A message can either just have a type with null values (useful for a ping)
    Message(MessageType type) {
        this.type = type;
    }

    // Or it can have both a type and values (an array of integers)
    Message(MessageType type, int[] values) {
        this.type = type;
        this.values = values;
    }
}
