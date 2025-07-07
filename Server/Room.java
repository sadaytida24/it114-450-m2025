package Server;

import java.util.*;
import Common.*;

public class Room {
    private String name;
    private List<ServerThread> clients = Collections.synchronizedList(new ArrayList<>());
    private static Map<String, Room> allRooms = new HashMap<>();

    public Room(String name) {
        this.name = name;
        allRooms.put(name, this);
    }

    public static Room getRoomByName(String name) {
        return allRooms.get(name);
    }

    public void addClient(ServerThread client) {
        clients.add(client);
        broadcast("Server", client.toString() + " joined the room.");
    }

    public void removeClient(ServerThread client) {
        clients.remove(client);
        broadcast("Server", client.toString() + " left the room.");
    }

    public void broadcast(String from, String message) {
        for (ServerThread client : clients) {
            client.sendMessage("[" + from + "]: " + message);
        }
    }

    public String getName() {
        return name;
    }
}
