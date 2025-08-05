// GameRoom.java
// UCID: ad273, Date: August 4th, 2025
// Extended room class for managing hangman game logic

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class GameRoom extends Room {
    private List<String> wordList;
    private String currentWord;
    private StringBuilder wordBlanks;
    private Set<Character> guessedLetters;
    private int strikes;
    private int maxStrikes = 6;
    private int round;
    private int maxRounds = 3;
    private boolean gameActive;
    private boolean sessionActive;
    
    // Player management
    private ConcurrentHashMap<Long, Player> players;
    private List<Long> turnOrder;
    private int currentPlayerIndex;
    private Set<Long> readyPlayers;
    private Set<Long> awayPlayers;
    private Set<Long> spectators;
    
    // Timer
    private ScheduledExecutorService turnTimer;
    private int turnTimeLimit = 30; // seconds
    private int currentTurnTime;
    
    // Game options
    private boolean correctGuessRemovesStrike = false;
    private boolean hardMode = false;
    
    public GameRoom(String name) {
        super(name);
        this.players = new ConcurrentHashMap<>();
        this.turnOrder = new ArrayList<>();
        this.readyPlayers = new HashSet<>();
        this.awayPlayers = new HashSet<>();
        this.spectators = new HashSet<>();
        this.guessedLetters = new HashSet<>();
        this.turnTimer = Executors.newSingleThreadScheduledExecutor();
        loadWordList();
    }
    
    private void loadWordList() {
        wordList = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader("words.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    wordList.add(line.trim().toUpperCase());
                }
            }
        } catch (IOException e) {
            // Default word list if file not found
            wordList.addAll(Arrays.asList(
                "JAVA", "PROGRAMMING", "COMPUTER", "NETWORK", "CLIENT", "SERVER",
                "HANGMAN", "GAME", "PLAYER", "SOCKET", "THREAD", "PAYLOAD"
            ));
        }
        System.out.println("Loaded " + wordList.size() + " words into memory");
    }
    
    @Override
    public void handleJoin(ServerThread client) {
        super.handleJoin(client);
        if (!players.containsKey(client.getClientId())) {
            players.put(client.getClientId(), new Player(client.getClientId(), client.getClientName()));
        }
        syncGameState();
    }
    
    @Override
    public void handleLeave(ServerThread client) {
        super.handleLeave(client);
        players.remove(client.getClientId());
        readyPlayers.remove(client.getClientId());
        awayPlayers.remove(client.getClientId());
        spectators.remove(client.getClientId());
        turnOrder.remove(client.getClientId());
        
        if (gameActive && turnOrder.size() < 2) {
            endSession();
        }
    }
    
    public void handleReady(ServerThread client) {
        if (sessionActive) {
            client.sendMessage(PayloadType.READY, "Game already in progress");
            return;
        }
        
        long clientId = client.getClientId();
        if (readyPlayers.contains(clientId)) {
            readyPlayers.remove(clientId);
            broadcastMessage(client.getClientName() + " is no longer ready", -1);
        } else {
            readyPlayers.add(clientId);
            broadcastMessage(client.getClientName() + " is ready", -1);
        }
        
        // Check if all players are ready (minimum 2 players)
        if (readyPlayers.size() >= 2 && readyPlayers.size() == players.size() - spectators.size()) {
            startSession();
        }
    }
    
    private void startSession() {
        sessionActive = true;
        gameActive = true;
        round = 1;
        strikes = 0;
        
        // Reset player points
        for (Player player : players.values()) {
            player.resetPoints();
        }
        
        // Set up turn order (exclude spectators and away players)
        turnOrder.clear();
        for (Long playerId : players.keySet()) {
            if (!spectators.contains(playerId) && !awayPlayers.contains(playerId)) {
                turnOrder.add(playerId);
            }
        }
        Collections.shuffle(turnOrder);
        
        broadcastMessage("Game starting! Round " + round + " of " + maxRounds, -1);
        startRound();
    }
    
    private void startRound() {
        // Pick random word
        currentWord = wordList.get(new Random().nextInt(wordList.size()));
        wordBlanks = new StringBuilder();
        for (int i = 0; i < currentWord.length(); i++) {
            if (currentWord.charAt(i) == ' ') {
                wordBlanks.append(' ');
            } else {
                wordBlanks.append('_');
            }
        }
        
        guessedLetters.clear();
        strikes = 0;
        currentPlayerIndex = 0;
        
        broadcastMessage("Round " + round + " started! Word has " + currentWord.length() + " letters", -1);
        syncGameState();
        startTurn();
    }
    
    private void startTurn() {
        if (turnOrder.isEmpty()) {
            endRound();
            return;
        }
        
        // Skip away players
        while (currentPlayerIndex < turnOrder.size() && 
               awayPlayers.contains(turnOrder.get(currentPlayerIndex))) {
            currentPlayerIndex++;
        }
        
        if (currentPlayerIndex >= turnOrder.size()) {
            currentPlayerIndex = 0;
            while (currentPlayerIndex < turnOrder.size() && 
                   awayPlayers.contains(turnOrder.get(currentPlayerIndex))) {
                currentPlayerIndex++;
            }
        }
        
        if (currentPlayerIndex >= turnOrder.size()) {
            endRound();
            return;
        }
        
        long currentPlayerId = turnOrder.get(currentPlayerIndex);
        ServerThread currentPlayer = clients.get(currentPlayerId);
        
        if (currentPlayer != null) {
            broadcastMessage("It's " + currentPlayer.getClientName() + "'s turn", -1);
            syncGameState();
            startTurnTimer();
        } else {
            endTurn();
        }
    }
    
    private void startTurnTimer() {
        currentTurnTime = turnTimeLimit;
        turnTimer.scheduleAtFixedRate(() -> {
            currentTurnTime--;
            if (currentTurnTime <= 0) {
                broadcastMessage("Time's up!", -1);
                endTurn();
            }
        }, 1, 1, TimeUnit.SECONDS);
    }
    
    private void stopTurnTimer() {
        turnTimer.shutdownNow();
        turnTimer = Executors.newSingleThreadScheduledExecutor();
    }
    
    public void handleGuessWord(ServerThread client, String guess) {
        if (!gameActive || !isPlayerTurn(client.getClientId())) {
            client.sendMessage(PayloadType.GUESS_WORD, "It's not your turn");
            return;
        }
        
        stopTurnTimer();
        
        if (guess.equalsIgnoreCase(currentWord)) {
            // Correct guess
            int points = 0;
            for (int i = 0; i < wordBlanks.length(); i++) {
                if (wordBlanks.charAt(i) == '_') {
                    points++;
                }
            }
            points *= 2; // Bonus for solving
            
            Player player = players.get(client.getClientId());
            player.addPoints(points);
            
            broadcastMessage(client.getClientName() + " guessed the correct word " + currentWord + 
                           " and got " + points + " points", -1);
            
            // Complete the word
            wordBlanks = new StringBuilder(currentWord);
            
            syncPoints();
            syncGameState();
            endRound();
        } else {
            // Wrong guess
            strikes++;
            broadcastMessage(client.getClientName() + " guessed " + guess + " and it was wrong", -1);
            
            if (strikes >= maxStrikes) {
                broadcastMessage("Too many strikes! The word was: " + currentWord, -1);
                endRound();
            } else {
                syncGameState();
                endTurn();
            }
        }
    }
    
    public void handleGuessLetter(ServerThread client, String letterStr) {
        if (!gameActive || !isPlayerTurn(client.getClientId())) {
            client.sendMessage(PayloadType.GUESS_LETTER, "It's not your turn");
            return;
        }
        
        if (letterStr.length() != 1) {
            client.sendMessage(PayloadType.GUESS_LETTER, "Please enter a single letter");
            return;
        }
        
        char letter = letterStr.toUpperCase().charAt(0);
        
        if (!hardMode && guessedLetters.contains(letter)) {
            client.sendMessage(PayloadType.GUESS_LETTER, "Letter already guessed");
            return;
        }
        
        stopTurnTimer();
        guessedLetters.add(letter);
        
        int matches = 0;
        for (int i = 0; i < currentWord.length(); i++) {
            if (currentWord.charAt(i) == letter && wordBlanks.charAt(i) == '_') {
                wordBlanks.setCharAt(i, letter);
                matches++;
            }
        }
        
        if (matches > 0) {
            // Correct letter
            Player player = players.get(client.getClientId());
            player.addPoints(matches);
            
            broadcastMessage(client.getClientName() + " guessed " + letter + 
                           " and there were " + matches + " " + letter + "'s which yielded " + 
                           matches + " points", -1);
            
            // Check if word is complete
            if (wordBlanks.toString().equals(currentWord)) {
                broadcastMessage("Word completed!", -1);
                syncPoints();
                syncGameState();
                endRound();
                return;
            }
        } else {
            // Wrong letter
            strikes++;
            if (correctGuessRemovesStrike && strikes > 0) {
                // This option is not applicable here since it's a wrong guess
            }
            
            broadcastMessage(client.getClientName() + " guessed " + letter + 
                           " letter, which isn't in the word", -1);
            
            if (strikes >= maxStrikes) {
                broadcastMessage("Too many strikes! The word was: " + currentWord, -1);
                endRound();
                return;
            }
        }
        
        syncPoints();
        syncGameState();
        endTurn();
    }
    
    public void handleSkip(ServerThread client) {
        if (!gameActive || !isPlayerTurn(client.getClientId())) {
            client.sendMessage(PayloadType.SKIP_TURN, "It's not your turn");
            return;
        }
        
        stopTurnTimer();
        broadcastMessage(client.getClientName() + " skipped their turn", -1);
        endTurn();
    }
    
    public void handleAway(ServerThread client, boolean isAway) {
        long clientId = client.getClientId();
        if (isAway) {
            awayPlayers.add(clientId);
            broadcastMessage(client.getClientName() + " is away", -1);
        } else {
            awayPlayers.remove(clientId);
            broadcastMessage(client.getClientName() + " is no longer away", -1);
        }
        syncGameState();
    }
    
    public void handleSpectator(ServerThread client, boolean isSpectator) {
        long clientId = client.getClientId();
        if (isSpectator) {
            spectators.add(clientId);
            turnOrder.remove(clientId);
            broadcastMessage(client.getClientName() + " joined as a spectator", -1);
            // Send correct word to spectator
            client.sendMessage(PayloadType.MESSAGE, "The word is: " + currentWord);
        } else {
            spectators.remove(clientId);
            if (!turnOrder.contains(clientId)) {
                turnOrder.add(clientId);
            }
            broadcastMessage(client.getClientName() + " is no longer a spectator", -1);
        }
        syncGameState();
    }
    
    private void endTurn() {
        currentPlayerIndex = (currentPlayerIndex + 1) % turnOrder.size();
        
        // Skip away players
        int attempts = 0;
        while (attempts < turnOrder.size() && awayPlayers.contains(turnOrder.get(currentPlayerIndex))) {
            currentPlayerIndex = (currentPlayerIndex + 1) % turnOrder.size();
            attempts++;
        }
        
        if (attempts >= turnOrder.size()) {
            // All players are away
            endRound();
        } else {
            startTurn();
        }
    }
    
    private void endRound() {
        stopTurnTimer();
        
        if (round >= maxRounds || strikes >= maxStrikes) {
            endSession();
        } else {
            round++;
            sendScoreboard(false);
            broadcastMessage("Round " + (round-1) + " ended. Starting round " + round, -1);
            startRound();
        }
    }
    
    private void endSession() {
        gameActive = false;
        sessionActive = false;
        stopTurnTimer();
        
        sendScoreboard(true);
        broadcastMessage("Game Over! Thanks for playing!", -1);
        
        // Reset game state
        readyPlayers.clear();
        turnOrder.clear();
        currentPlayerIndex = 0;
        round = 0;
        
        syncGameState();
    }
    
    private void sendScoreboard(boolean isFinal) {
        List<Player> sortedPlayers = new ArrayList<>(players.values());
        sortedPlayers.removeIf(p -> spectators.contains(p.getId()));
        sortedPlayers.sort((a, b) -> Integer.compare(b.getPoints(), a.getPoints()));
        
        StringBuilder scoreboard = new StringBuilder();
        scoreboard.append(isFinal ? "FINAL SCOREBOARD:\n" : "ROUND SCOREBOARD:\n");
        
        for (int i = 0; i < sortedPlayers.size(); i++) {
            Player player = sortedPlayers.get(i);
            ServerThread client = clients.get(player.getId());
            String playerName = client != null ? client.getClientName() : "Unknown";
            scoreboard.append((i + 1)).append(". ").append(playerName)
                     .append(": ").append(player.getPoints()).append(" points\n");
        }
        
        sendToAll(PayloadType.SCOREBOARD, scoreboard.toString());
    }
    
    private void syncPoints() {
        for (Player player : players.values()) {
            PointsPayload pointsPayload = new PointsPayload(player.getId(), player.getPoints());
            for (ServerThread client : clients.values()) {
                client.sendPointsPayload(pointsPayload);
            }
        }
    }
    
    private void syncGameState() {
        GameStatePayload gameState = new GameStatePayload();
        gameState.setCurrentWord(spectators.size() > 0 ? currentWord : null);
        gameState.setWordBlanks(wordBlanks != null ? wordBlanks.toString() : "");
        gameState.setStrikes(strikes);
        gameState.setMaxStrikes(maxStrikes);
        gameState.setRound(round);
        gameState.setMaxRounds(maxRounds);
        gameState.setGameActive(gameActive);
        gameState.setTurnTimeRemaining(currentTurnTime);
        gameState.setGuessedLetters(new ArrayList<>(guessedLetters));
        
        if (!turnOrder.isEmpty() && currentPlayerIndex < turnOrder.size()) {
            gameState.setCurrentPlayerId(turnOrder.get(currentPlayerIndex));
        }
        
        for (ServerThread client : clients.values()) {
            // Send current word only to spectators
            if (spectators.contains(client.getClientId())) {
                gameState.setCurrentWord(currentWord);
            } else {
                gameState.setCurrentWord(null);
            }
            client.sendGameStatePayload(gameState);
        }
    }
    
    private boolean isPlayerTurn(long clientId) {
        return gameActive && !turnOrder.isEmpty() && 
               currentPlayerIndex < turnOrder.size() && 
               turnOrder.get(currentPlayerIndex) == clientId;
    }
    
    public void setCorrectGuessRemovesStrike(boolean value) {
        this.correctGuessRemovesStrike = value;
    }
    
    public void setHardMode(boolean value) {
        this.hardMode = value;
    }
}