package server;

import server.io.messages.MessageHandler;
import server.io.messages.MessageType;

import javax.swing.*;
import java.awt.event.*;

public class ServerKeyListener extends JFrame implements KeyListener {

    // Constructor
    public ServerKeyListener() {
        // Set frame properties
        setTitle("Server Key Listener");
        setSize(300, 0);
        setResizable(false);

        // WHen you close the window, it shuts down the server
        WindowListener exitListener = new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                closeWindow();
            }
        };
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(exitListener);

        // Ensure the frame can receive key events
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);
        // It seems weird to add something to itself, but it works, trust
        this.addKeyListener(this);

        // Make the frame visible
        setVisible(true);
    }

    // When a key is pressed down, this is called
    @Override
    public void keyPressed(KeyEvent e) {
        // Gets what key was pressed down
        int keyCode = e.getKeyCode();
        // Depending on the key, do something specific
        switch (KeyEvent.getKeyText(keyCode)) {
            // Spin up weapon
            // For weapon, 100 is full power, -100 is off, 0 is half
            case "1" -> Main.connectionHandler.sendToIO(MessageHandler.construct(
                    MessageType.SEND_WEAPON,
                    new int[]{100}));
            // Spin down weapon
            case "2" -> Main.connectionHandler.sendToIO(MessageHandler.construct(
                    MessageType.SEND_WEAPON,
                    new int[]{-100}));
            // For drive, 100 is forward, -100 is backward, 0 is off
            // Drive bot forward
            case "Up" -> {
                Main.connectionHandler.sendToIO(MessageHandler.construct(
                        MessageType.SEND_LEFT,
                        new int[]{100}));
                Main.connectionHandler.sendToIO(MessageHandler.construct(
                        MessageType.SEND_RIGHT,
                        new int[]{100}));
            }
            // Turn bot left
            case "Left" -> {
                Main.connectionHandler.sendToIO(MessageHandler.construct(
                        MessageType.SEND_LEFT,
                        new int[]{-100}));
                Main.connectionHandler.sendToIO(MessageHandler.construct(
                        MessageType.SEND_RIGHT,
                        new int[]{100}));
            }
            // Enable autonomous mode
            case "3" -> Main.autoEnabled = true;
            // Disable autonomous mode
            case "4" -> Main.autoEnabled = false;
        }
    }

    // When a key is released, this is called
    @Override
    public void keyReleased(KeyEvent e) {
        // Gets what key was pressed down
        int keyCode = e.getKeyCode();
        String key = KeyEvent.getKeyText(keyCode);
        // If the key is a directional key, stop the bot when it is released
        if (key.equals("Up") || key.equals("Down") || key.equals("Left") || key.equals("Right")) {
            Main.connectionHandler.sendToIO(MessageHandler.construct(
                    MessageType.SEND_LEFT,
                    new int[]{0}));
            Main.connectionHandler.sendToIO(MessageHandler.construct(
                    MessageType.SEND_RIGHT,
                    new int[]{0}));
        }
    }

    // We don't use this, but have to put it or else Java yells at us
    public void keyTyped(KeyEvent e) {}

    public void closeWindow() {
        Main.shutdown();
        dispose();
    }
}
