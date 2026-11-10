package nangorblast.engine;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import nangorblast.core.Direction;
import nangorblast.core.PlayerDamaged;
import nangorblast.core.Position;
import nangorblast.model.GameWorld;
import nangorblast.model.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Lives, invulnerability, and respawn (F01.2.2, F01.2.4). Runs after every damage source
 * in the tick, so a hit from any system is handled the same way:
 *
 * <ul>
 *   <li>a lost life publishes {@link PlayerDamaged} once;
 *   <li>with lives left, the player goes back to the spawn point, or the nearest free tile
 *       when something stands there, with 1.5 seconds of invulnerability;
 *   <li>otherwise invulnerability counts down by {@code dt}.
 * </ul>
 *
 * LOSE at 0 lives is {@link WinLoseSystem}'s job.
 */
public final class LifeSystem implements GameSystem {

    private static final Logger LOG = LoggerFactory.getLogger(LifeSystem.class);

    private int lastLives = Player.START_LIVES;

    @Override
    public void update(GameWorld world, double dt) {
        Player player = world.player();
        if (player.lives() < lastLives) {
            world.bus().publish(new PlayerDamaged(player.playerIndex(), player.lives()));
        }
        lastLives = player.lives();

        if (player.isRespawnPending()) {
            Position tile = respawnTile(world, player).orElse(player.position());
            player.respawnAt(tile);
            LOG.info("Player {} respawned at {} with {} lives", player.playerIndex(), tile, player.lives());
        } else {
            player.tickInvulnerability(dt);
        }
    }

    /**
     * The spawn point if it is free, otherwise the free tile closest to it by walking
     * distance, searched breadth-first over tiles the player can enter. A free tile is on
     * the map and holds no entity besides the player, so the player never lands on lava,
     * a bomb, a power-up, or the exit.
     */
    static Optional<Position> respawnTile(GameWorld world, Player player) {
        Position spawn = player.spawn();
        Deque<Position> queue = new ArrayDeque<>();
        Set<Position> seen = new HashSet<>();
        queue.add(spawn);
        seen.add(spawn);
        while (!queue.isEmpty()) {
            Position tile = queue.poll();
            if (isFree(world, player, tile)) {
                return Optional.of(tile);
            }
            for (Direction direction : Direction.values()) {
                Position next = tile.translate(direction);
                if (seen.add(next) && !world.isBlockedFor(player, next)) {
                    queue.add(next);
                }
            }
        }
        return Optional.empty();
    }

    private static boolean isFree(GameWorld world, Player player, Position tile) {
        return world.inBounds(tile) && world.entitiesAt(tile).stream().allMatch(e -> e == player);
    }
}
