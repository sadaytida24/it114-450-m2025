package Server;

import java.net.*;
import java.io.*;
import java.util.*;

public class Server {
    public static final int PORT = 3000;
    private static List<ServerThread> clients = Collections.synchronizedList(new ArrayList<>());
    private static Room lobby = new Room("Lobby");

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("[Server] Listening on port " + PORT);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                ServerThread clientThread = new ServerThread(clientSocket, lobby);
                clients.add(clientThread);
                clientThread.start(); 
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
