package server;

import server.io.messages.Message;
import server.io.messages.MessageHandler;
import server.io.messages.MessageType;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Objects;
import java.util.Scanner;

public class Main {
    // A socket is basically a metaphorical tunnel system between two devices
    // Data travels back and forth using this tunnel system

    // A ServerSocket is like management overlooking the tunnel
    private static ServerSocket serverSocket;
    // The ConnectionHandler... well.... handles the connection
    private static ConnectionHandler connectionHandler;
    // The port can be any combination of 5 digits at or under 65535
    // as long as the bot connects to the exact same port on its end
    // (Some ports will likely already be used on your computer)
    private static final int port = 61134;

    public static void main(String[] args) {
        // When the file runs, just opens the connection with the specified port
        openConnection(port);
    }

    public static void openConnection(int port) {
        try {
            // Opens the connection on the supplied port
            serverSocket = new ServerSocket(port);
            System.out.println("Server Opened");
        } catch (IOException e) {
            System.out.println("Connection Listener Closed");
            e.printStackTrace();
        }

        // Creates a handler for incoming connections
        connectionHandler = new ConnectionHandler(serverSocket);
        // Tells the ConnectionHandler to accept incoming connections
        connectionHandler.startAccepting();

        // Accepts input from the console, so you can actually control the server
        consoleInput();
    }

    // Will read inout from the console.
    // Will do specific stuff if you type specific things.
    // Feel free to add your own conditions and stuff
    private static void consoleInput() {
        boolean shutdown = false;
        // Reading from System.in
        Scanner reader = new Scanner(System.in);
        while (!shutdown) {
            // Will block the thread until you send something in console
            String input = reader.nextLine();
            // If you type shutdown, the server will shut down
            if (Objects.equals(input, "shutdown")) {
                shutdown = true;
            } else if (input.startsWith("send ")) {
                // If you type 'send number', it will send a message
                // with that number as the type to the bot
                MessageType type = MessageType.fromInt(Integer.parseInt(input.substring(5)));
                Message message = MessageHandler.construct(type);
                connectionHandler.sendToIO(message);
            }
        }
        // Always close the reader after use
        reader.close();
        // Shuts down after input breaks
        shutdown();
    }

    // Shuts down the server
    public static void shutdown() {
        System.out.println("Server Shutdown Started");
        // Close the connection handler
        if (connectionHandler != null) { connectionHandler.close(); }
        // Close the server if it isn't already
        if (serverSocket!= null && !serverSocket.isClosed()) {
            System.out.println("Server Closing");
            try {
                serverSocket.close();
            } catch (IOException e) {
                System.out.println("Error closing Server!");
                e.printStackTrace();
                serverSocket = null;
            }
        }
        System.out.println("Server Closed");
        // Exits the program
        System.exit(0);
    }
}