package nangorblast.engine;

import nangorblast.core.GameEnded;
import nangorblast.core.GameState;
import nangorblast.model.Exit;
import nangorblast.model.GameWorld;
import nangorblast.model.Hostile;
import nangorblast.model.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Checks the end of the game after every cycle (F09.4), as the last system of the tick.
 *
 * <ul>
 *   <li>The exit opens once no active {@link Hostile} entity is left: every Normal Enemy
 *       and the boss are defeated. A defeated enemy is deactivated by its owner's system.
 *   <li>WIN when the exit is open and the player stands on it.
 *   <li>LOSE when the player has no lives left or the timer reaches 0. LOSE is checked
 *       first, so reaching the exit in the tick the last life is lost is still LOSE.
 * </ul>
 *
 * Publishes {@link GameEnded} once, in the tick the state changes.
 */
public final class WinLoseSystem implements GameSystem {

    private static final Logger LOG = LoggerFactory.getLogger(WinLoseSystem.class);

    @Override
    public void update(GameWorld world, double dt) {
        if (world.state() != GameState.PLAYING) {
            return;
        }
        Player player = world.player();
        if (!player.isAlive() || world.timeLeft() <= 0) {
            end(world, GameState.LOSE);
            return;
        }
        boolean enemiesLeft = world.entities().stream().anyMatch(Hostile.class::isInstance);
        for (Exit exit : world.entities(Exit.class)) {
            if (!enemiesLeft && !exit.isOpen()) {
                exit.open();
                LOG.info("Every enemy is defeated, the exit at {} is open", exit.position());
            }
            if (exit.isOpen() && exit.position().equals(player.position())) {
                end(world, GameState.WIN);
                return;
            }
        }
    }

    private static void end(GameWorld world, GameState result) {
        Player player = world.player();
        int secondsLeft = (int) Math.ceil(world.timeLeft());
        LOG.info("{}: {} lives, {} s left, score {}", result, player.lives(), secondsLeft, world.score());
        world.setState(result);
        world.bus().publish(new GameEnded(result, player.lives(), secondsLeft));
    }
}
