package nangorblast.engine.bomb;

import nangorblast.core.GameState;
import nangorblast.core.Position;
import nangorblast.engine.CommandHandler;
import nangorblast.engine.GameSystem;
import nangorblast.input.Action;
import nangorblast.input.Command;
import nangorblast.model.GameWorld;
import nangorblast.model.Player;
import nangorblast.model.bomb.Bomb;
import nangorblast.model.bomb.BombFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Places bombs (F01.2.5, F02.3.1) and counts their fuses down (F02.2.1).
 *
 * <p>A handler and a system with no state of its own, so separate instances in the handler
 * list and the system list behave the same.
 */
public final class BombSystem implements CommandHandler, GameSystem {

    private static final Logger LOG = LoggerFactory.getLogger(BombSystem.class);

    @Override
    public void handle(GameWorld world, Command command) {
        Player player = world.player();
        if (command.action() != Action.PLACE_BOMB || world.state() != GameState.PLAYING
                || command.playerIndex() != player.playerIndex() || !player.isAlive()) {
            return;
        }
        Position tile = player.position();
        if (!player.canPlaceBomb()) {
            LOG.debug("Player {} cannot place a bomb: capacity {} is full", player.playerIndex(),
                    player.bombCapacity());
            return;
        }
        if (world.firstAt(tile, Bomb.class).isPresent()) {
            LOG.debug("Player {} cannot place a bomb: {} already holds one", player.playerIndex(), tile);
            return;
        }
        Bomb bomb = BombFactory.create(player.selectedBomb(), tile, player);
        world.add(bomb);
        player.bombPlaced();
        LOG.debug("Player {} placed a {} bomb at {}", player.playerIndex(), bomb.type(), tile);
    }

    @Override
    public void update(GameWorld world, double dt) {
        for (Bomb bomb : world.entities(Bomb.class)) {
            bomb.tickFuse(dt);
            if (bomb.isDue()) {
                bomb.deactivate();
                // shortcut: capacity comes back when the bomb goes off; B-02 moves this to the end
                // of its explosion, 0.5 seconds later (F01.2.5).
                bomb.owner().bombFinished();
                LOG.debug("{} bomb at {} exploded", bomb.type(), bomb.position());
            }
        }
    }
}
