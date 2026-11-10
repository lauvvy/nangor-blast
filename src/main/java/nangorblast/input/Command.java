package nangorblast.input;

import java.util.Objects;

/**
 * One player action as an object (Command pattern): input sources create commands, the
 * command queue carries them to the game-loop thread, and the engine applies them.
 *
 * @param playerIndex 0 for the first player, 1 for the second (bonus B04)
 */
public record Command(int playerIndex, Action action) {

    public Command {
        if (playerIndex < 0) {
            throw new IllegalArgumentException("Player index must not be negative, got " + playerIndex);
        }
        Objects.requireNonNull(action, "action");
    }
}
