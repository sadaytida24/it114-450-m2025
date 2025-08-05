// Room.java
// UCID: ad273, Date: August 4th, 2025
// Base room class for managing client connections

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Room {
    protected String name;
    protected ConcurrentHashMap<Long, ServerThread> clients;
    
    public Room(String name) {
        this.name = name;
        this.clients = new ConcurrentHashMap<>();
    }
    
    public void handleJoin(ServerThread client) {
        clients.put(client.getClientId(), client);
        client.setCurrentRoom(this);
        broadcastMessage(client.getClientName() + " joined the room", client.getClientId());
    }
    
    public void handleLeave(ServerThread client) {
        clients.remove(client.getClientId());
        client.setCurrentRoom(null);
        broadcastMessage(client.getClientName() + " left the room", client.getClientId());
    }
    
    public void handleMessage(ServerThread sender, String message) {
        String formattedMessage = sender.getClientName() + ": " + message;
        broadcastMessage(formattedMessage, -1);
    }
    
    protected void broadcastMessage(String message, long excludeClientId) {
        for (ServerThread client : clients.values()) {
            if (client.getClientId() != excludeClientId) {
                client.sendMessage(PayloadType.MESSAGE, message);
            }
        }
    }
    
    protected void sendToAll(PayloadType type, String message) {
        for (ServerThread client : clients.values()) {
            client.sendMessage(type, message);
        }
    }
    
    public String getName() { return name; }
    public Collection<ServerThread> getClients() { return clients.values(); }
    public int getClientCount() { return clients.size(); }
}
