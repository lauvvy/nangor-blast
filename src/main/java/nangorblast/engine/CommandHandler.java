package nangorblast.engine;

import nangorblast.input.Command;
import nangorblast.model.GameWorld;

/**
 * Receives every command on the game-loop thread at the start of each tick, in the order the
 * keys were pressed. Handlers run in every state, including PAUSED, so a pause command can
 * resume the game. A handler that applies player actions checks the state itself.
 */
@FunctionalInterface
public interface CommandHandler {

    void handle(GameWorld world, Command command);
}
