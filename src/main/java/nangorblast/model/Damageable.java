package nangorblast.model;

/** Anything that can be hit: the player, Normal Enemies, and bosses. */
public interface Damageable {

    /**
     * Applies the damage if the rules allow it.
     *
     * @return true if the target was hurt, false if it ignored the hit (invulnerable,
     *     already hit by this activation, or a boss that is not vulnerable)
     */
    boolean takeDamage(Damage damage);
}
