package server;

import server.io.IO;
import server.io.messages.Message;

import javax.swing.*;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

// Takes in the connection socket as an argument. Handles all interactions with the bot
public class ConnectionHandler {

    // This handles communicating directly with the bot
    private static IO io;
    // A ServerSocket is like management overlooking a tunnel system
    private final ServerSocket serverSocket;
    // While a regular Socket is more like the tunnel itself
    private Socket connectionSocket;

    private boolean accepting;
    private boolean connected;

    // We have a timeout so that if the bot stops
    // talking to the server, we know it disconnected
    private final int maxTimeout;
    public long lastUpdate;

    // Initializes parameters
    public ConnectionHandler(ServerSocket serverSocket) {
        // If a bot doesn't communicate with a server
        // for this (arbitrary) amount of time, it's unresponsive. Kill its connection
        maxTimeout = 5000;
        lastUpdate = 0;
        accepting = false;
        connected = false;
        this.serverSocket = serverSocket;
    }

    // Loop accepting connections
    public void startAccepting() {
        // Loops trying to accept connections in a new thread.
        // This is so that the main thread can still run code at the same time
        Thread acceptThread = new Thread(() -> {
            accepting = true;
            System.out.println("Accepting Connections");
            while (accepting) {
                try {
                    // Will block the thread until something tries to connect
                    Socket newSocket = serverSocket.accept();
                    // After something connects, save its socket
                    setConnectionSocket(newSocket);
                    System.out.println("New Connection Accepted");
                } catch (IOException e) {
                    // The socket tends to error whenever it's
                    // closed since serverSocket.accept() is interrupted
                    System.out.println("Connection Socket Closed");
                }
            }
        });
        // After setting the code we want to run in a new thread, we
        // have to actually start the thread
        acceptThread.start();
    }

    // Pretty simple. Just makes the ConnectionHandler stop accepting new connections
    // (This will also prevent a reconnection if your bot disconnects)
    public void stopAccepting() {
        accepting = false;
    }

    // When a connection tries to join, this handles setting it up
    public void setConnectionSocket(Socket newSocket) {
        // When your bot disconnects and reconnects, it may not have had time to timeout yet.
        // This will delete the old connection
        closeCurrentConnectionSocket();
        this.connectionSocket = newSocket;
        // Each connection needs a new IO (input/output)
        io = new IO(connectionSocket, this);
        io.startReceive();
        // We update the timeout since the bot obviously isn't timed out if
        // it's making a new connection
        updateTimeout();
        connected = true;
        // Starts the connection timeout timer.
        // The previous one expired since 'connected' was set to false when connection was closed
        startConnectionTimeout();
    }

    // Closes the connection with the bot
    public void closeCurrentConnectionSocket() {
        connected = false;
        // Always null check first if existence isn't guaranteed....
        if (io != null) {
            io.disconnect();
            io = null;
        }
        try {
            // Make sure it's not null first, then make sure it isn't already closed
            if (connectionSocket != null && !connectionSocket.isClosed()) {
                // Only now is it safe to close it
                connectionSocket.close();
            }
        } catch (IOException e) {
            // If this errors then uhh, skill issue. Force it to null so the
            // garbage collector (Java's memory management) will assassinate it
            System.out.println("Error Closing Connection's Socket");
            e.printStackTrace();
            connectionSocket = null;
        }
    }

    // This will pass a message to the IO to be sent to the bot.
    // Usually called when the upper level server needs to send something
    public void sendToIO(Message message) {
        // IO CAN BE NULL!!! (Since we set it to that when the connection is closed)
        // (It also starts null, and only changes when we get the first connection)
        // Always do a null check... Trust me, the errors are not fun
        if (io != null) { io.send(message); }
    }

    // Pretty simple. Just updates the timeout
    // (usually called when the bot communicates with the server)
    public void updateTimeout() {
        lastUpdate = System.currentTimeMillis();
    }

    // A simple helper function that just calculates the amount of time since the last timeout update
    private long timeSinceUpdate() {
        return System.currentTimeMillis() - lastUpdate;
    }

    // After a specified amount of time, check whether the bot has timed out
    // and kill it if it has
    private void startConnectionTimeout() {
        // Checks the timeout every half a second (500 milliseconds)
        Timer timer = new Timer(500, arg0 -> {
            if (connected) {
                if (timeSinceUpdate() > maxTimeout) {
                    // KILL!!!
                    closeCurrentConnectionSocket();
                } else {
                    // It's safe this time. Try to catch it slipping up next time
                    startConnectionTimeout();
                }
            }
        });
        // This is a one-off timer. We manually recreate it (more stable this way)
        timer.setRepeats(false);
        // Make sure to actually start the timer
        timer.start();
    }

    // Closes the ConnectionHandler.
    // Should only really be done if the server is closing
    public void close() {
        // Close all streams, remove references, then close the socket
        System.out.println("Connection Closing");
        stopAccepting();
        closeCurrentConnectionSocket();
        System.out.println("Connection Successfully Closed");
    }
}
