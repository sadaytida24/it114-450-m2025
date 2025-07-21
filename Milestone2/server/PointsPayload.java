// UCID: ad273
// Date: 07/21/2025
// Description: Payload subclass for syncing point data between server and clients

public class PointsPayload extends Payload {
    private int points;

    public PointsPayload(String clientId, int points) {
        super(clientId, "", "points");
        this.points = points;
    }

    public int getPoints() { return points; }

    @Override
    public String toString() {
        return "[PointsPayload] clientId: " + getClientId() + ", points: " + points;
    }
}
