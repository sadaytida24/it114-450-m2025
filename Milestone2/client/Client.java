// UCID: ad273
// Date: 07/21/2025
// Description: Processes /skip command and sends to server.

public class Client {
    public void processCommand(String input) {
        if (input.equals("/skip")) {
            Payload skipPayload = new Payload("client123", "skip", "skipCommand");
            sendToServer(skipPayload);
        }
    }

    public void receivePayload(Payload payload) {
        System.out.println("Client received: " + payload.toString());
    }

    public void sendToServer(Payload payload) {
        System.out.println("Sending to server: " + payload);
    }
}
