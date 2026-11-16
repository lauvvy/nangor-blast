package nangorblast.core;

/**
 * Published once when the game reaches WIN or LOSE. On WIN, ScoreSystem adds the F08 bonus
 * from {@code livesLeft} and {@code secondsLeft}.
 *
 * @param result {@link GameState#WIN} or {@link GameState#LOSE}
 * @param secondsLeft whole seconds left on the timer, rounded up as the HUD shows them
 */
public record GameEnded(GameState result, int livesLeft, int secondsLeft) implements GameEvent {

    public GameEnded {
        if (result != GameState.WIN && result != GameState.LOSE) {
            throw new IllegalArgumentException("A game ends in WIN or LOSE, not " + result);
        }
    }
}
