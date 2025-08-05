// PayloadType.java
// UCID: ad273, Date: August 4th, 2025
// Enumeration of all payload types for communication

public enum PayloadType {
    CLIENT_NAME,
    CLIENT_CONNECT,
    CLIENT_DISCONNECT,
    MESSAGE,
    CREATE_ROOM,
    JOIN_ROOM,
    LEAVE_ROOM,
    READY,
    GAME_START,
    TURN_START,
    TURN_END,
    ROUND_START,
    ROUND_END,
    SESSION_END,
    GUESS_WORD,
    GUESS_LETTER,
    SKIP_TURN,
    POINTS_UPDATE,
    GAME_STATE_UPDATE,
    SCOREBOARD,
    AWAY,
    SPECTATOR,
    SYNC
}
