package nangorblast.core;

/** A player action that the rules refuse, such as selecting Remote Bomb without the detonator. */
public class InvalidActionException extends GameException {

    private static final long serialVersionUID = 1L;

    public InvalidActionException(String message) {
        super(message);
    }
}
