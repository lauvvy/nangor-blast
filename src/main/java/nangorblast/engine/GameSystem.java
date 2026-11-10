package nangorblast.engine;

import nangorblast.model.GameWorld;

/**
 * One concern of the game, updated once per tick on the game-loop thread. Count time down
 * with {@code dt}; never read the clock or sleep. Register each system with one line in
 * {@code app/GameFactory}.
 */
@FunctionalInterface
public interface GameSystem {

    /** @param dt seconds of game time in this tick */
    void update(GameWorld world, double dt);
}
