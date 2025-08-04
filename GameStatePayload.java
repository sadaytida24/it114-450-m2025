// GameStatePayload.java
// UCID: ad273, Date: 2025-08-04
// Payload for syncing game state information

import java.util.List;

public class GameStatePayload extends Payload {
    private static final long serialVersionUID = 1L;
    
    private String currentWord;
    private String wordBlanks;
    private int strikes;
    private int maxStrikes;
    private long currentPlayerId;
    private List<Character> guessedLetters;
    private int round;
    private int maxRounds;
    private boolean gameActive;
    private int turnTimeRemaining;
    
    public GameStatePayload() {
        setPayloadType(PayloadType.GAME_STATE_UPDATE);
    }
    
    // Getters and setters
    public String getCurrentWord() { return currentWord; }
    public void setCurrentWord(String currentWord) { this.currentWord = currentWord; }
    
    public String getWordBlanks() { return wordBlanks; }
    public void setWordBlanks(String wordBlanks) { this.wordBlanks = wordBlanks; }
    
    public int getStrikes() { return strikes; }
    public void setStrikes(int strikes) { this.strikes = strikes; }
    
    public int getMaxStrikes() { return maxStrikes; }
    public void setMaxStrikes(int maxStrikes) { this.maxStrikes = maxStrikes; }
    
    public long getCurrentPlayerId() { return currentPlayerId; }
    public void setCurrentPlayerId(long currentPlayerId) { this.currentPlayerId = currentPlayerId; }
    
    public List<Character> getGuessedLetters() { return guessedLetters; }
    public void setGuessedLetters(List<Character> guessedLetters) { this.guessedLetters = guessedLetters; }
    
    public int getRound() { return round; }
    public void setRound(int round) { this.round = round; }
    
    public int getMaxRounds() { return maxRounds; }
    public void setMaxRounds(int maxRounds) { this.maxRounds = maxRounds; }
    
    public boolean isGameActive() { return gameActive; }
    public void setGameActive(boolean gameActive) { this.gameActive = gameActive; }
    
    public int getTurnTimeRemaining() { return turnTimeRemaining; }
    public void setTurnTimeRemaining(int turnTimeRemaining) { this.turnTimeRemaining = turnTimeRemaining; }
    
    @Override
    public String toString() {
        return String.format("GameStatePayload[word='%s', blanks='%s', strikes=%d/%d, currentPlayer=%d, round=%d/%d, active=%b]",
                           currentWord, wordBlanks, strikes, maxStrikes, currentPlayerId, round, maxRounds, gameActive);
    }
}
