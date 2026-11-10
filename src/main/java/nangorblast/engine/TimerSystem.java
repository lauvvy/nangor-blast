package nangorblast.engine;

import nangorblast.model.GameWorld;

/** Counts the game timer down (step 6 of the tick). Reaching 0 is handled by WinLoseSystem. */
public final class TimerSystem implements GameSystem {

    @Override
    public void update(GameWorld world, double dt) {
        world.reduceTime(dt);
    }
}
