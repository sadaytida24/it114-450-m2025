// UCID: ad273
// Date: 07/21/2025
// Description: Processes incoming GuessPayload for letter guesses.

public class ServerThread {
    private GameRoom room = new GameRoom();

    public void process(Payload payload) {
        if (payload instanceof GuessPayload) {
            room.handleGuess(this, (GuessPayload) payload);
        }
    }

    public void sendToClient(Payload payload) {
        System.out.println("Sending to client: " + payload.toString());
    }
}
