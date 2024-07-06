package server.io;

import server.ConnectionHandler;
import server.io.messages.Message;
import server.io.messages.MessageHandler;
import server.io.messages.MessageType;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Vector;

public class IO {
    private final ConnectionHandler connectionHandler;
    private InputStream in;
    private static OutputStream out;
    private boolean inOpen = false;

    public IO(Socket socket, ConnectionHandler connectionHandler) {
        // We get a reference to the parent ConnectionHandler, so we can update the timeout
        this.connectionHandler = connectionHandler;

        try {
            // An output stream is for sending data to the bot
            out = socket.getOutputStream();
        } catch (IOException e) {
            System.out.println("Error fetching socket output stream!");
            e.printStackTrace();
        }

        try {
            // An input stream is for getting data from the bot
            in = socket.getInputStream();
        } catch (IOException e) {
            System.out.println("Error fetching socket input stream!");
            e.printStackTrace();
        }
    }

    // Loop reading messages until connection is closed or errored
    public void startReceive() {
        // This is done in a different thread to prevent blocking the ConnectionHandler
        Thread inThread = new Thread(() -> {
            inOpen = true;
            // A Vector is a lot like an array. It's just a list of Integers with no set size
            Vector<Integer> receivedData = new Vector<>();
            while (inOpen) {
                // Try and receive data from the socket. Will be empty if no data available
                // We pass in an existing Vector to avoid creating a new one each loop
                // (The vector is updated inside receive())
                receive(receivedData);

                // If you actually received data, process it
                if (!receivedData.isEmpty()) {
                    // Since the bot sent us data, it hasn't timed out
                    connectionHandler.updateTimeout();

                    // Turns the received data into something usable
                    Message message = MessageHandler.decode(receivedData);
                    processMessage(message);
                    // We have to clear the vector after we are done processing the data in it
                    receivedData.clear();
                }
            }
        });
        // Remember, always start your threads
        inThread.start();
    }

    // Reads from the InputStream to see if there is data sent from the bot
    private void receive(Vector<Integer> receivedData) {
        int data = -1;
        try {
            // Semicolon is the delimiter I chose. Basically, it denotes the end of a message
            while (data != ';') {
                data = in.read();
                if (data != ';') {
                    receivedData.add(data);
                }
            }
        } catch (IOException e) {
            if (inOpen) {
                System.out.println("Error in Connection's IO!");
                e.printStackTrace();
            }
        }
    }

    // Well I mean, we want to actually do stuff when we receive messages, right?
    private void processMessage(Message message) {
        // A null message type means we won't know what to do with it, so ignore it
        if (message.type != null) {
            switch (message.type) {
                // A handshake is basically just a greeting between devices.
                // It confirms that the connection is functioning properly
                case HANDSHAKE_REQUEST -> {
                    // Creates a message with just a type. Accepts the bot's request
                    Message response = MessageHandler.construct(MessageType.HANDSHAKE_ACCEPTED);
                    // Sends the message to the bot
                    send(response);
                    System.out.println("Bot's Handshake accepted");
                }
                case PING_REQUEST -> {
                    // Creates a message with just a type. Responds to the bot's ping
                    Message response = MessageHandler.construct(MessageType.PING_RESPONDED);
                    // Sends the message to the bot
                    send(response);
                }
                // If there isn't a case for the message type, just print the message to console
                default -> System.out.println(message);
            }
        }
    }

    // Sends data to the bot through the OutputStream
    public void send(Message message) {
        try {
            // We must first turn the data into something the OutputStream can send
            out.write(MessageHandler.encode(message));
        } catch (IOException e) {
            System.out.println("Error sending message!");
            e.printStackTrace();
        }
    }

    // Disconnects the client from the server
    public void disconnect() {
        try {
            // Close all streams then the client
            inOpen = false;
            if (in != null) {in.close();}
            if (out != null) {out.close();}
        } catch (IOException e) {
            System.out.println("Error disconnecting Client!");
            e.printStackTrace();
        }
    }
}