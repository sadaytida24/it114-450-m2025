// Client.java
// UCID: instructor, Date: 2025-08-04
// Main client class for connecting to server

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;

public class Client {
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private boolean isConnected = false;
    private ClientUI ui;
    private long clientId = -1;
    private String clientName;
    
    public Client() {
        SwingUtilities.invokeLater(() -> {
            ui = new ClientUI(this);
            ui.showConnectionPanel();
        });
        System.out.println("Client waiting for input...");
    }
    
    public boolean connect(String host, int port, String username) {
        try {
            socket = new Socket(host, port);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
            
            this.clientName = username;
            
            // Set name first
            sendPayload(new Payload(-1, username, PayloadType.CLIENT_NAME));
            
            // Then connect
            sendPayload(new Payload(-1, "", PayloadType.CLIENT_CONNECT));
            
            isConnected = true;
            
            // Start listening thread
            new Thread(this::listenForMessages).start();
            
            return true;
        } catch (IOException e) {
            System.err.println("Failed to connect: " + e.getMessage());
            return false;
        }
    }
    
    private void listenForMessages() {
        try {
            while (isConnected && socket != null && !socket.isClosed()) {
                Object obj = in.readObject();
                handlePayload(obj);
            }
        } catch (IOException | ClassNotFoundException e) {
            if (isConnected) {
                System.err.println("Connection lost: " + e.getMessage());
                SwingUtilities.invokeLater(() -> {
                    ui.showMessage("Connection lost to server");
                    ui.showConnectionPanel();
                });
                disconnect();
            }
        }
    }
    
    private void handlePayload(Object obj) {
        SwingUtilities.invokeLater(() -> {
            if (obj instanceof GameStatePayload) {
                ui.updateGameState((GameStatePayload) obj);
            } else if (obj instanceof PointsPayload) {
                ui.updatePoints((PointsPayload) obj);
            } else if (obj instanceof Payload) {
                Payload payload = (Payload) obj;
                
                switch (payload.getPayloadType()) {
                    case CLIENT_CONNECT:
                        if (payload.getMessage().contains("successfully")) {
                            ui.showReadyPanel();
                        }
                        ui.addMessage(payload.getMessage());
                        break;
                    case MESSAGE:
                        ui.addMessage(payload.getMessage());
                        break;
                    case SCOREBOARD:
                        ui.showScoreboard(payload.getMessage());
                        break;
                    case GAME_STATE_UPDATE:
                        // Handle any additional game state updates
                        break;
                    default:
                        ui.addMessage(payload.getMessage());
                        break;
                }
            }
        });
    }
    
    public void sendMessage(String message) {
        if (isConnected) {
            sendPayload(new Payload(clientId, message, PayloadType.MESSAGE));
        }
    }
    
    public void sendReady() {
        if (isConnected) {
            sendPayload(new Payload(clientId, "", PayloadType.READY));
        }
    }
    
    public void sendGuessWord(String word) {
        if (isConnected) {
            sendPayload(new Payload(clientId, word, PayloadType.GUESS_WORD));
        }
    }
    
    public void sendGuessLetter(String letter) {
        if (isConnected) {
            sendPayload(new Payload(clientId, letter, PayloadType.GUESS_LETTER));
        }
    }
    
    public void sendSkip() {
        if (isConnected) {
            sendPayload(new Payload(clientId, "", PayloadType.SKIP_TURN));
        }
    }
    
    public void sendAway(boolean isAway) {
        if (isConnected) {
            sendPayload(new Payload(clientId, String.valueOf(isAway), PayloadType.AWAY));
        }
    }
    
    public void sendSpectator(boolean isSpectator) {
        if (isConnected) {
            sendPayload(new Payload(clientId, String.valueOf(isSpectator), PayloadType.SPECTATOR));
        }
    }
    
    private void sendPayload(Payload payload) {
        try {
            out.writeObject(payload);
            out.flush();
        } catch (IOException e) {
            System.err.println("Error sending payload: " + e.getMessage());
        }
    }
    
    public void disconnect() {
        isConnected = false;
        try {
            if (out != null) out.close();
            if (in != null) in.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            System.err.println("Error disconnecting: " + e.getMessage());
        }
    }
    
    public static void main(String[] args) {
        new Client();
    }
}