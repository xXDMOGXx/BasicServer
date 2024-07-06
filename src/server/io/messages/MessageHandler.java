package server.io.messages;

import java.util.Vector;

// This one is a bit of a doozy. Hard to explain, you can just
// treat it as a black box. It works, it does its job, don't worry about it
// (or look through it and try and figure it out if you want, lol)
public class MessageHandler {

    // Gets the message ready to be sent over the socket
    public static byte[] encode(Message message) {
        byte[] messageStream;
        int valueLength;
        if (message.values != null) {
            valueLength = message.values.length;
            messageStream = new byte[valueLength + 2];
            messageStream[0] = (byte) message.type.ordinal();
            for (int i = 0; i < valueLength; i++) {
                messageStream[i + 1] = ((byte) message.values[i]);
            }
        } else {
            valueLength = 0;
            messageStream = new byte[2];
            messageStream[0] = (byte) message.type.ordinal();
        }
        // Semicolon denotes the end of a message. Always tacked on at the... well... end
        messageStream[valueLength + 1] = ((byte) ';');
        return messageStream;
    }

    // Turns a message read over the socket back into a Message object
    public static Message decode(Vector<Integer> buffer) {
        MessageType type = MessageType.fromInt(buffer.get(0));
        if (buffer.size() > 1) {
            int[] values = new int[buffer.size()-1];
            for (int i = 1; i < buffer.size(); i++) {
                values[i-1] = buffer.get(i);
            }
            return new Message(type, values);
        } else {
            return new Message(type);
        }
    }

    public static Message construct(MessageType type) {
        return new Message(type);
    }

    public static Message construct(MessageType type, int[] values) {
        return new Message(type, values);
    }
}
