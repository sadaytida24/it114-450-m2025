// RoomManager.java
// UCID: ad273, Date: 2025-08-04
// Singleton class to manage all rooms

import java.util.concurrent.ConcurrentHashMap;

public class RoomManager {
    private static ConcurrentHashMap<String, Room> rooms = new ConcurrentHashMap<>();
    
    public static Room createRoom(String name) {
        if (rooms.containsKey(name)) {
            return null;
        }
        Room room = new Room(name);
        rooms.put(name, room);
        return room;
    }
    
    public static GameRoom createGameRoom(String name) {
        if (rooms.containsKey(name)) {
            return null;
        }
        GameRoom room = new GameRoom(name);
        rooms.put(name, room);
        return room;
    }
    
    public static Room getRoom(String name) {
        return rooms.get(name);
    }
    
    public static void removeRoom(String name) {
        rooms.remove(name);
    }
}