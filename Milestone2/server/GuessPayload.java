// UCID: ad273
// Date: 07/21/2025
// Description: Payload used for guessing letters or words.

public class GuessPayload extends Payload {
    private String guess;
    private boolean isWord;

    public GuessPayload(String clientId, String guess, boolean isWord) {
        super(clientId, guess, isWord ? "guessWord" : "guessLetter");
        this.guess = guess;
        this.isWord = isWord;
    }

    public String getGuess() {
        return guess;
    }

    public boolean isWordGuess() {
        return isWord;
    }

    @Override
    public String toString() {
        return "[GuessPayload] clientId=" + getClientId() + ", guess=" + guess + ", isWordGuess=" + isWord;
    }
}
