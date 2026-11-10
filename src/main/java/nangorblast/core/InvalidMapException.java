package nangorblast.core;

/** A map file breaks a rule in F00.3 or uses an unknown character. The message names the rule. */
public class InvalidMapException extends GameException {

    private static final long serialVersionUID = 1L;

    public InvalidMapException(String message) {
        super(message);
    }

    public InvalidMapException(String message, Throwable cause) {
        super(message, cause);
    }
}
