// PointsPayload.java
// UCID: ad273, Date: August 4th, 2025
// Payload for syncing player points

public class PointsPayload extends Payload {
    private static final long serialVersionUID = 1L;
    
    private long playerId;
    private int points;
    
    public PointsPayload() {}
    
    public PointsPayload(long playerId, int points) {
        super();
        this.playerId = playerId;
        this.points = points;
        setPayloadType(PayloadType.POINTS_UPDATE);
    }
    
    public long getPlayerId() { return playerId; }
    public void setPlayerId(long playerId) { this.playerId = playerId; }
    
    public int getPoints() { return points; }
    public void setPoints(int points) { this.points = points; }
    
    @Override
    public String toString() {
        return String.format("PointsPayload[playerId=%d, points=%d]", playerId, points);
    }
}
