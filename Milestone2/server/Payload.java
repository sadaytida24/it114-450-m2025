// UCID: ad273
// Date: 07/21/2025
// Description: Base payload class used for transferring message data between client and server

public class Payload {
    private String clientId;
    private String message;
    private String type;

    public Payload(String clientId, String message, String type) {
        this.clientId = clientId;
        this.message = message;
        this.type = type;
    }

    public String getClientId() { return clientId; }
    public String getMessage() { return message; }
    public String getType() { return type; }

    @Override
    public String toString() {
        return "[Payload] type: " + type + ", clientId: " + clientId + ", message: " + message;
    }
}
