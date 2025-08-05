// ServerThread.java
// UCID: ad273, Date: August 4th, 2025
// Handles individual client connections and message processing

import java.io.*;
import java.net.Socket;
import java.net.SocketException;

public class ServerThread extends Thread {
    private Socket client;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private long clientId;
    private String clientName;
    private Server server;
    private Room currentRoom;
    private boolean isRunning = true;
    
    public ServerThread(Socket client, long clientId, Server server) {
        this.client = client;
        this.clientId = clientId;
        this.server = server;
        try {
            this.out = new ObjectOutputStream(client.getOutputStream());
            this.in = new ObjectInputStream(client.getInputStream());
        } catch (IOException e) {
            System.err.println("Error setting up client streams: " + e.getMessage());
        }
    }
    
    @Override
    public void run() {
        try {
            while (isRunning && !client.isClosed()) {
                try {
                    Object obj = in.readObject();
                    if (obj instanceof Payload) {
                        handlePayload((Payload) obj);
                    }
                } catch (ClassNotFoundException e) {
                    System.err.println("Unknown payload type received");
                } catch (SocketException e) {
                    break; // Client disconnected
                }
            }
        } catch (IOException e) {
            if (isRunning) {
                System.err.println("Client communication error: " + e.getMessage());
            }
        } finally {
            cleanup();
        }
    }
    
    private void handlePayload(Payload payload) {
        System.out.println("Received payload: " + payload.toString());
        
        switch (payload.getPayloadType()) {
            case CLIENT_NAME:
                handleSetName(payload.getMessage());
                break;
            case CLIENT_CONNECT:
                handleConnect();
                break;
            case MESSAGE:
                if (currentRoom != null) {
                    currentRoom.handleMessage(this, payload.getMessage());
                }
                break;
            case CREATE_ROOM:
                handleCreateRoom(payload.getMessage());
                break;
            case JOIN_ROOM:
                handleJoinRoom(payload.getMessage());
                break;
            case READY:
                if (currentRoom instanceof GameRoom) {
                    ((GameRoom) currentRoom).handleReady(this);
                }
                break;
            case GUESS_WORD:
                if (currentRoom instanceof GameRoom) {
                    ((GameRoom) currentRoom).handleGuessWord(this, payload.getMessage());
                }
                break;
            case GUESS_LETTER:
                if (currentRoom instanceof GameRoom) {
                    ((GameRoom) currentRoom).handleGuessLetter(this, payload.getMessage());
                }
                break;
            case SKIP_TURN:
                if (currentRoom instanceof GameRoom) {
                    ((GameRoom) currentRoom).handleSkip(this);
                }
                break;
            case AWAY:
                if (currentRoom instanceof GameRoom) {
                    ((GameRoom) currentRoom).handleAway(this, Boolean.parseBoolean(payload.getMessage()));
                }
                break;
            case SPECTATOR:
                if (currentRoom instanceof GameRoom) {
                    ((GameRoom) currentRoom).handleSpectator(this, Boolean.parseBoolean(payload.getMessage()));
                }
                break;
        }
    }
    
    private void handleSetName(String name) {
        this.clientName = name;
        sendMessage(PayloadType.CLIENT_NAME, "Name set to: " + name);
    }
    
    private void handleConnect() {
        if (clientName == null || clientName.trim().isEmpty()) {
            sendMessage(PayloadType.CLIENT_CONNECT, "You must set a name first with /name <name>");
            return;
        }
        
        Room lobby = RoomManager.getRoom("Lobby");
        if (lobby != null) {
            lobby.handleJoin(this);
        }
        sendMessage(PayloadType.CLIENT_CONNECT, "Connected successfully");
    }
    
    private void handleCreateRoom(String roomName) {
        if (currentRoom == null) {
            sendMessage(PayloadType.CREATE_ROOM, "You must be connected first");
            return;
        }
        
        GameRoom newRoom = RoomManager.createGameRoom(roomName);
        if (newRoom != null) {
            currentRoom.handleLeave(this);
            newRoom.handleJoin(this);
            sendMessage(PayloadType.CREATE_ROOM, "Room created: " + roomName);
        } else {
            sendMessage(PayloadType.CREATE_ROOM, "Room already exists: " + roomName);
        }
    }
    
    private void handleJoinRoom(String roomName) {
        Room room = RoomManager.getRoom(roomName);
        if (room != null) {
            if (currentRoom != null) {
                currentRoom.handleLeave(this);
            }
            room.handleJoin(this);
            sendMessage(PayloadType.JOIN_ROOM, "Joined room: " + roomName);
        } else {
            sendMessage(PayloadType.JOIN_ROOM, "Room does not exist: " + roomName);
        }
    }
    
    public void sendMessage(PayloadType type, String message) {
        try {
            Payload payload = new Payload(clientId, message, type);
            System.out.println("Sending payload: " + payload.toString());
            out.writeObject(payload);
            out.flush();
        } catch (IOException e) {
            System.err.println("Error sending message to client " + clientId + ": " + e.getMessage());
        }
    }
    
    public void sendPointsPayload(PointsPayload pointsPayload) {
        try {
            System.out.println("Sending points payload: " + pointsPayload.toString());
            out.writeObject(pointsPayload);
            out.flush();
        } catch (IOException e) {
            System.err.println("Error sending points to client " + clientId + ": " + e.getMessage());
        }
    }
    
    public void sendGameStatePayload(GameStatePayload gameStatePayload) {
        try {
            System.out.println("Sending game state payload: " + gameStatePayload.toString());
            out.writeObject(gameStatePayload);
            out.flush();
        } catch (IOException e) {
            System.err.println("Error sending game state to client " + clientId + ": " + e.getMessage());
        }
    }
    
    private void cleanup() {
        isRunning = false;
        if (currentRoom != null) {
            currentRoom.handleLeave(this);
        }
        server.removeClient(clientId);
        
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (client != null && !client.isClosed()) client.close();
        } catch (IOException e) {
            System.err.println("Error cleaning up client connection: " + e.getMessage());
        }
    }
    
    // Getters
    public long getClientId() { return clientId; }
    public String getClientName() { return clientName; }
    public Room getCurrentRoom() { return currentRoom; }
    public void setCurrentRoom(Room room) { this.currentRoom = room; }
}
