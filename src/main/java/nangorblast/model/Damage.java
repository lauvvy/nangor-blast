package nangorblast.model;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

/**
 * One hit. {@code activationId} identifies the explosion, boss attack, contact, or eruption
 * that caused it, so a target that stays inside the same activation is hurt only once
 * (F01.2.2). Create one id per activation with {@link #newActivationId()} and reuse it for
 * every target and every tick of that activation.
 */
public record Damage(int amount, long activationId, DamageSource source) {

    private static final AtomicLong NEXT_ACTIVATION = new AtomicLong(1);

    public Damage {
        if (amount <= 0) {
            throw new IllegalArgumentException("Damage amount must be positive, got " + amount);
        }
        Objects.requireNonNull(source, "source");
    }

    public static long newActivationId() {
        return NEXT_ACTIVATION.getAndIncrement();
    }
}
