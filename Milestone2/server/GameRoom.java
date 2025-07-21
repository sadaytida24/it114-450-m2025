// UCID: ad273
// Date: 07/21/2025
// Description: Handles letter guesses and game state update.

import java.util.HashSet;
import java.util.Set;

public class GameRoom {
    private String currentWord = "HELLO";
    private StringBuilder currentBlanks = new StringBuilder("_ _ _ _ _");
    private Set<Character> guessedLetters = new HashSet<>();
    private int currentStrikes = 0;

    public void handleGuess(ServerThread sender, GuessPayload payload) {
        String clientId = payload.getClientId();
        String guess = payload.getGuess().toUpperCase();

        if (!payload.isWordGuess()) {
            char letter = guess.charAt(0);

            if (guessedLetters.contains(letter)) {
                sender.sendToClient(new Payload(clientId, "Letter '" + letter + "' was already guessed", "message"));
                onTurnEnd();
                return;
            }

            guessedLetters.add(letter);
            int matches = 0;

            for (int i = 0; i < currentWord.length(); i++) {
                if (currentWord.charAt(i) == letter && currentBlanks.charAt(i * 2) == '_') {
                    currentBlanks.setCharAt(i * 2, letter);
                    matches++;
                }
            }

            if (matches > 0) {
                int points = matches;
                sender.sendToClient(new PointsPayload(clientId, points));
                sender.sendToClient(new Payload(clientId, "guessed " + letter + " and found " + matches + " occurrence(s), earned " + points + " points", "message"));

                if (currentBlanks.indexOf("_") == -1) {
                    onRoundEnd();
                } else {
                    onTurnEnd();
                }
            } else {
                currentStrikes++;
                sender.sendToClient(new Payload(clientId, "guessed " + letter + ", which is not in the word", "message"));

                if (currentStrikes >= 6) {
                    onRoundEnd();
                } else {
                    onTurnEnd();
                }
            }
        }
    }

    public void onTurnEnd() {
        System.out.println("Turn ended.");
    }

    public void onRoundEnd() {
        System.out.println("Round ended.");
    }
}
