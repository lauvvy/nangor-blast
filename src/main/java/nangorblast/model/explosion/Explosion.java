package nangorblast.model.explosion;

import java.util.Objects;
import java.util.Set;
import nangorblast.core.BombType;
import nangorblast.core.EntityKind;
import nangorblast.core.Position;
import nangorblast.model.Entity;
import nangorblast.model.Player;

/**
 * One tile of a bomb's explosion (F03). Every tile of one activation shares the center,
 * owner, activation id, and the set of entities already hurt, so a target is hurt once
 * however many tiles or ticks it spends in the flames. Walkable, like the floor.
 */
public final class Explosion extends Entity {

    // Specification default. M-05 moves it into config/game.properties.
    public static final double DURATION_SECONDS = 0.5;
    private static final double EPSILON = 1e-9; // absorbs rounding in repeated dt sums

    private final Position center;
    private final int blastRange;
    private final Player owner;
    private final BombType bombType;
    private final int damage;
    private final long activationId;
    private final Set<Long> hurt;
    private double duration = DURATION_SECONDS;

    /**
     * @param hurt ids of entities this activation has already hurt; one mutable set shared
     *             by every tile of the activation
     */
    public Explosion(Position tile, Position center, int blastRange, Player owner, BombType bombType,
            int damage, long activationId, Set<Long> hurt) {
        super(tile);
        this.center = Objects.requireNonNull(center, "center");
        this.blastRange = blastRange;
        this.owner = Objects.requireNonNull(owner, "owner");
        this.bombType = Objects.requireNonNull(bombType, "bombType");
        this.damage = damage;
        this.activationId = activationId;
        this.hurt = Objects.requireNonNull(hurt, "hurt");
    }

    @Override
    public EntityKind kind() {
        return EntityKind.EXPLOSION;
    }

    /** The tile of the bomb that exploded. */
    public Position center() {
        return center;
    }

    public boolean isCenter() {
        return position().equals(center);
    }

    public int blastRange() {
        return blastRange;
    }

    public Player owner() {
        return owner;
    }

    public BombType bombType() {
        return bombType;
    }

    public int damage() {
        return damage;
    }

    public long activationId() {
        return activationId;
    }

    /** Seconds left before the explosion ends. */
    public double duration() {
        return duration;
    }

    public void tick(double dt) {
        duration = Math.max(0, duration - dt);
    }

    public boolean isFinished() {
        return duration <= EPSILON;
    }

    public boolean hasHurt(Entity target) {
        return hurt.contains(target.id());
    }

    public void markHurt(Entity target) {
        hurt.add(target.id());
    }
}
