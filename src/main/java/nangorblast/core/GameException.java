package nangorblast.core;

/**
 * Base of every game-specific error. Unchecked, so systems deep in a tick need no
 * {@code throws} clauses, but each place that reads external input catches it and shows
 * the message.
 */
public class GameException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public GameException(String message) {
        super(message);
    }

    public GameException(String message, Throwable cause) {
        super(message, cause);
    }
}
