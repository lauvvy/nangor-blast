package nangorblast.engine.bomb;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import nangorblast.core.Direction;
import nangorblast.core.Position;
import nangorblast.engine.GameSystem;
import nangorblast.model.Damage;
import nangorblast.model.DamageSource;
import nangorblast.model.Damageable;
import nangorblast.model.Entity;
import nangorblast.model.GameWorld;
import nangorblast.model.block.Block;
import nangorblast.model.bomb.Bomb;
import nangorblast.model.explosion.Explosion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Turns due bombs into explosions, ends explosions after 0.5 seconds, and hurts what stands
 * in them (F03). Runs after {@link BombSystem} in the tick. Keeps no state of its own.
 */
public final class ExplosionSystem implements GameSystem {

    private static final Logger LOG = LoggerFactory.getLogger(ExplosionSystem.class);

    @Override
    public void update(GameWorld world, double dt) {
        for (Explosion explosion : world.entities(Explosion.class)) {
            explosion.tick(dt);
            if (explosion.isFinished()) {
                explosion.deactivate();
                if (explosion.isCenter()) {
                    explosion.owner().bombFinished(); // F01.2.5: capacity returns when the explosion ends
                }
            }
        }
        // Resolve the whole chain in this tick, including bombs earlier in insertion order.
        for (Explosion explosion : world.entities(Explosion.class)) {
            hit(world, explosion);
        }
        boolean detonated;
        do {
            detonated = false;
            for (Bomb bomb : world.entities(Bomb.class)) {
                if (bomb.isDue()) {
                    detonate(world, bomb);
                    detonated = true;
                }
            }
            for (Explosion explosion : world.entities(Explosion.class)) {
                hit(world, explosion);
            }
        } while (detonated);
    }

    private static void detonate(GameWorld world, Bomb bomb) {
        bomb.deactivate();
        long activationId = Damage.newActivationId();
        Set<Long> hurt = new HashSet<>();
        for (Position tile : area(world, bomb)) {
            world.add(new Explosion(tile, bomb.position(), bomb.blastRange(), bomb.owner(), bomb.type(),
                    bomb.damage(), activationId, hurt));
        }
        LOG.debug("{} bomb at {} exploded with range {}", bomb.type(), bomb.position(), bomb.blastRange());
    }

    /**
     * The center and four arms (F03.2.1). An arm stops at the map edge or at any block, and the
     * block's tile is not part of the area; a block the bomb can destroy is destroyed (F03.2.2 to .11).
     */
    private static List<Position> area(GameWorld world, Bomb bomb) {
        List<Position> tiles = new ArrayList<>();
        tiles.add(bomb.position());
        for (Direction direction : Direction.values()) {
            Position tile = bomb.position();
            for (int step = 0; step < bomb.blastRange(); step++) {
                tile = tile.translate(direction);
                if (!world.inBounds(tile)) {
                    break;
                }
                Optional<Block> block = world.firstAt(tile, Block.class);
                if (block.isPresent()) {
                    if (block.get().isDestroyedBy(bomb.type())) {
                        block.get().deactivate();
                        LOG.debug("{} at {} destroyed", block.get().kind(), tile);
                    }
                    break;
                }
                tiles.add(tile);
            }
        }
        return tiles;
    }

    /** Hurts each target in the flames once per activation (F03.2.3 to .5) and triggers bombs (F02.3.7). */
    private static void hit(GameWorld world, Explosion explosion) {
        for (Entity entity : world.entitiesAt(explosion.position())) {
            if (entity instanceof Bomb other) {
                other.trigger();
            } else if (entity instanceof Damageable target && !explosion.hasHurt(entity)) {
                Damage damage = new Damage(explosion.damage(), explosion.activationId(), DamageSource.EXPLOSION);
                // An invulnerable player or a boss that is not vulnerable is tried again next tick.
                if (target.takeDamage(damage)) {
                    explosion.markHurt(entity);
                    LOG.debug("Explosion at {} hit {}", explosion.position(), entity.kind());
                }
            }
        }
    }
}
