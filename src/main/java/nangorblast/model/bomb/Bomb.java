package nangorblast.model.bomb;

import java.util.Objects;
import nangorblast.core.BombType;
import nangorblast.core.Position;
import nangorblast.model.Entity;
import nangorblast.model.Player;

/**
 * A bomb placed by a player. Attributes from F02.1: position, type, owner, fuse time,
 * blast range, and active. The fuse counts down with {@code dt}, so pause freezes it.
 */
public abstract class Bomb extends Entity {

    private static final double EPSILON = 1e-9; // absorbs rounding in repeated dt sums

    private final Player owner;
    private final int blastRange;
    private double fuseTime;

    protected Bomb(Position position, Player owner, int blastRange, double fuseTime) {
        super(position);
        this.owner = Objects.requireNonNull(owner, "owner");
        if (blastRange < 1) {
            throw new IllegalArgumentException("Blast range must be at least 1, got " + blastRange);
        }
        this.blastRange = blastRange;
        this.fuseTime = fuseTime;
    }

    public abstract BombType type();

    public final Player owner() {
        return owner;
    }

    /** Fixed when the bomb is placed; a later Fire Up does not change it. */
    public final int blastRange() {
        return blastRange;
    }

    /** Damage its explosion deals to each target. A Pumped Bomb deals 2 (F02.2.2). */
    public int damage() {
        return 1;
    }

    /** Seconds left before the bomb explodes. */
    public final double fuseTime() {
        return fuseTime;
    }

    /** Called once per tick by {@code BombSystem}. */
    public void tickFuse(double dt) {
        fuseTime = Math.max(0, fuseTime - dt);
    }

    /** Makes the bomb explode this tick: hit by another explosion (F02.3.7) or detonated. */
    public final void trigger() {
        fuseTime = 0;
    }

    public final boolean isDue() {
        return fuseTime <= EPSILON;
    }

    /**
     * F01.2.5: anyone standing on the bomb's tile may step off it, but nobody can step
     * onto a tile holding an active bomb.
     */
    @Override
    public final boolean blocksMovementOf(Entity mover) {
        return !mover.position().equals(position());
    }
}
