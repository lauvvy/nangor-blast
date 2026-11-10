package nangorblast.core;

/** A configuration file is missing, unreadable, or holds an invalid value. The message names the key. */
public class ConfigLoadException extends GameException {

    private static final long serialVersionUID = 1L;

    public ConfigLoadException(String message) {
        super(message);
    }

    public ConfigLoadException(String message, Throwable cause) {
        super(message, cause);
    }
}
