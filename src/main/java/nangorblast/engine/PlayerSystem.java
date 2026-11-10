package nangorblast.engine;

import java.util.Optional;
import nangorblast.core.Direction;
import nangorblast.core.GameState;
import nangorblast.core.PlayerDamaged;
import nangorblast.core.Position;
import nangorblast.input.Command;
import nangorblast.model.Damage;
import nangorblast.model.DamageSource;
import nangorblast.model.Entity;
import nangorblast.model.GameWorld;
import nangorblast.model.Hostile;
import nangorblast.model.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Moves the player one tile per step (F01.2.1) under the "Who blocks whom" table, and
 * reports contact when the player steps into an enemy (F01.2.3).
 *
 * <p>A handler and a system: {@link #handle} receives move commands at the start of the
 * tick, {@link #update} counts the step cooldown down. After a step the player waits
 * {@code 1 / speed} seconds. A move that arrives during the cooldown is kept for up to
 * {@link #BUFFER_SECONDS} and applied when the cooldown ends; a newer move replaces it.
 */
public final class PlayerSystem implements CommandHandler, GameSystem {

    private static final Logger LOG = LoggerFactory.getLogger(PlayerSystem.class);

    public static final double BUFFER_SECONDS = 0.15;
    private static final double EPSILON = 1e-9; // absorbs rounding in repeated dt sums

    // Game-loop thread only.
    private double cooldown;
    private Direction buffered;
    private double bufferLeft;

    @Override
    public void handle(GameWorld world, Command command) {
        Optional<Direction> direction = directionOf(command);
        Player player = world.player();
        if (direction.isEmpty() || world.state() != GameState.PLAYING
                || command.playerIndex() != player.playerIndex() || !player.isAlive()) {
            return;
        }
        if (cooldown <= EPSILON) {
            step(world, player, direction.get());
        } else {
            buffered = direction.get();
            bufferLeft = BUFFER_SECONDS;
        }
    }

    @Override
    public void update(GameWorld world, double dt) {
        // Keeping at most one tick of credit lets speeds that are not a whole number of
        // ticks average out exactly, without an idle player banking extra steps.
        cooldown = Math.max(cooldown - dt, -dt);
        if (buffered == null) {
            return;
        }
        bufferLeft -= dt;
        if (cooldown <= EPSILON && bufferLeft >= -EPSILON) {
            Direction direction = buffered;
            buffered = null;
            step(world, world.player(), direction);
        } else if (bufferLeft < -EPSILON) {
            buffered = null;
        }
    }

    private void step(GameWorld world, Player player, Direction direction) {
        player.face(direction);
        Position target = player.position().translate(direction);
        if (!world.isBlockedFor(player, target)) {
            player.moveTo(target);
            cooldown += 1.0 / player.speed();
            return;
        }
        // Blocked: no step, so no cooldown. Stepping into an enemy is contact.
        for (Entity other : world.entitiesAt(target)) {
            if (other instanceof Hostile) {
                Damage contact = new Damage(1, Damage.newActivationId(), DamageSource.ENEMY_CONTACT);
                if (player.takeDamage(contact)) {
                    LOG.info("Player {} stepped into {} at {}, {} lives left",
                            player.playerIndex(), other.kind(), target, player.lives());
                    world.bus().publish(new PlayerDamaged(player.playerIndex(), player.lives()));
                }
                return;
            }
        }
    }

    private static Optional<Direction> directionOf(Command command) {
        return Optional.ofNullable(switch (command.action()) {
            case MOVE_UP -> Direction.UP;
            case MOVE_DOWN -> Direction.DOWN;
            case MOVE_LEFT -> Direction.LEFT;
            case MOVE_RIGHT -> Direction.RIGHT;
            default -> null;
        });
    }
}
