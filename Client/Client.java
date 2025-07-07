package Client;

import java.io.*;
import java.net.*;
import java.util.Scanner;
import Common.*;

public class Client {
    public static void main(String[] args) {
        try (Socket socket = new Socket("localhost", 3000);
             ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
             Scanner scanner = new Scanner(System.in)) {

            Thread listener = new Thread(() -> {
                try {
                    Message msg;
                    while ((msg = (Message) input.readObject()) != null) {
                        System.out.println(msg.getFrom() + ": " + msg.getContent());
                    }
                } catch (Exception e) {
                    System.out.println("Disconnected from server.");
                }
            });
            listener.start();



            //ucid: ad273  Date: 7/7/25
            while (true) {
                String line = scanner.nextLine();
                if (line.startsWith("/name ")) {
                    Payload p = new Payload(PayloadType.SET_NAME);
                    p.setMessage(line.substring(6));
                    output.writeObject(p);
                } else if (line.startsWith("/connect")) {
                    Payload p = new Payload(PayloadType.CONNECT);
                    output.writeObject(p);
                } else if (line.startsWith("/createroom ")) {
                    Payload p = new Payload(PayloadType.CREATE_ROOM);
                    p.setMessage(line.substring(12));
                    output.writeObject(p);
                } else if (line.startsWith("/joinroom ")) {
                    Payload p = new Payload(PayloadType.JOIN_ROOM);
                    p.setMessage(line.substring(10));
                    output.writeObject(p);
                } else if (line.startsWith("/exit")) {
                    Payload p = new Payload(PayloadType.DISCONNECT);
                    output.writeObject(p);
                    break;
                } else {
                    Payload p = new Payload(PayloadType.MESSAGE);
                    p.setMessage(line);
                    output.writeObject(p);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
