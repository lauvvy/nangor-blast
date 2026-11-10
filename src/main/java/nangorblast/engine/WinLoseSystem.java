package nangorblast.engine;

import nangorblast.core.GameState;
import nangorblast.model.GameWorld;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Checks the end of the game after every cycle (F09.4), as the last system of the tick.
 * LOSE when the timer reaches 0 or the player has no lives left. C-08 adds WIN.
 */
public final class WinLoseSystem implements GameSystem {

    private static final Logger LOG = LoggerFactory.getLogger(WinLoseSystem.class);

    @Override
    public void update(GameWorld world, double dt) {
        if (world.state() != GameState.PLAYING) {
            return;
        }
        if (world.timeLeft() <= 0 || !world.player().isAlive()) {
            LOG.info("LOSE: {} s left, {} lives", world.timeLeft(), world.player().lives());
            world.setState(GameState.LOSE);
        }
    }
}
