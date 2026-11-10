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

    /**
     * Called once at the end of every tick in every state, after the systems. Lets a handler
     * count time with {@code dt} while paused or in a menu, or react to a state a system set.
     */
    default void afterTick(GameWorld world, double dt) {
    }
}
