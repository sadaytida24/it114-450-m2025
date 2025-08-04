// Server.java
// UCID: ad273, Date: 2025-08-04
// Main server class that starts the server and listens for client connections

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
    private int port = 3000;
    private boolean isRunning = false;
    private ServerSocket server;
    private ConcurrentHashMap<Long, ServerThread> connectedClients = new ConcurrentHashMap<>();
    private static long clientIdCounter = 1;
    
    public Server() {
        System.out.println("Server initialized");
    }
    
    public Server(int port) {
        this.port = port;
        System.out.println("Server initialized on port: " + port);
    }
    
    public void start() throws IOException {
        server = new ServerSocket(port);
        System.out.println("Server listening on port: " + port);
        isRunning = true;
        
        // Initialize lobby as game room
        RoomManager.createGameRoom("Lobby");
        
        while (isRunning) {
            try {
                Socket client = server.accept();
                long clientId = clientIdCounter++;
                ServerThread serverThread = new ServerThread(client, clientId, this);
                connectedClients.put(clientId, serverThread);
                serverThread.start();
                System.out.println("Client connected: " + clientId);
            } catch (IOException e) {
                if (isRunning) {
                    System.err.println("Error accepting client connection: " + e.getMessage());
                }
            }
        }
    }
    
    public void removeClient(long clientId) {
        connectedClients.remove(clientId);
        System.out.println("Client disconnected: " + clientId);
    }
    
    public void stop() {
        isRunning = false;
        try {
            if (server != null && !server.isClosed()) {
                server.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing server: " + e.getMessage());
        }
    }
    
    public static void main(String[] args) {
        int port = 3000;
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("Invalid port number, using default: 3000");
            }
        }
        
        Server server = new Server(port);
        try {
            server.start();
        } catch (IOException e) {
            System.err.println("Failed to start server: " + e.getMessage());
        }
    }
}