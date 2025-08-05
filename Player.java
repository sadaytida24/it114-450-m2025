// Player.java
// UCID: ad273, Date: August 4th, 2025
// Player class to store player information and points

public class Player {
    private long id;
    private String name;
    private int points;
    
    public Player(long id, String name) {
        this.id = id;
        this.name = name;
        this.points = 0;
    }
    
    public void addPoints(int points) {
        this.points += points;
    }
    
    public void resetPoints() {
        this.points = 0;
    }
    
    // Getters and setters
    public long getId() { return id; }
    public String getName() { return name; }
    public int getPoints() { return points; }
    public void setPoints(int points) { this.points = points; }
}
