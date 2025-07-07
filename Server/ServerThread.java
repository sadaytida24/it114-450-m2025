package Server;

import java.io.*;
import java.net.*;
import Common.*;

//UCID: ad273 Date:7/7/25
public class ServerThread extends Thread {
    private Socket socket;
    private ObjectInputStream input;
    private ObjectOutputStream output;
    private String clientName;
    private Room currentRoom;

    public ServerThread(Socket socket, Room room) {
        this.socket = socket;
        this.currentRoom = room;
    }

    public void run() {
        try {
            output = new ObjectOutputStream(socket.getOutputStream());
            input = new ObjectInputStream(socket.getInputStream());
            System.out.println("[Server] Client connected.");

            // ✅ Moved here: output is now initialized
            currentRoom.addClient(this);

            Payload payload;
            while ((payload = (Payload) input.readObject()) != null) {
                switch (payload.getType()) {
                    case SET_NAME:
                        this.clientName = payload.getMessage();
                        sendMessage("[Server] Name set to " + clientName);
                        break;
                    case MESSAGE:
                        currentRoom.broadcast(clientName, payload.getMessage());
                        break;
                    case CREATE_ROOM:
                        Room newRoom = new Room(payload.getMessage());
                        currentRoom.removeClient(this);
                        currentRoom = newRoom;
                        currentRoom.addClient(this);
                        sendMessage("[Server] Created and joined room: " + payload.getMessage());
                        break;
                    case JOIN_ROOM:
                        currentRoom.removeClient(this);
                        currentRoom = Room.getRoomByName(payload.getMessage());
                        if (currentRoom == null) currentRoom = new Room("Lobby");
                        currentRoom.addClient(this);
                        sendMessage("[Server] Joined room: " + currentRoom.getName());
                        break;
                    case DISCONNECT:
                        close();
                        return;
                }
            }
        } catch (Exception e) {
            System.out.println("[Server] Client disconnected or error occurred.");
        } finally {
            close();
        }
    }

    public void sendMessage(String msg) {
        try {
            if (output != null) {
                output.writeObject(new Message("Server", msg));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            if (input != null) input.close();
            if (output != null) output.close();
            if (socket != null) socket.close();
            if (currentRoom != null) currentRoom.removeClient(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        return (clientName != null) ? clientName : "Unknown";
    }
}
