package nangorblast.model.enemy;

import nangorblast.core.EntityKind;
import nangorblast.core.Position;
import nangorblast.model.Damage;
import nangorblast.model.Damageable;

public final class NormalEnemy extends Enemy implements Damageable {
    public static final double DEFAULT_SPEED = 2.0;
    public static final int START_HEALTH = 1;
    public static final int SCORE_VALUE = 100;

    private final MovementBehavior behavior;
    private int health = START_HEALTH;

    public NormalEnemy(Position position, MovementBehavior behavior) {
        super(position, DEFAULT_SPEED);
        this.behavior = behavior;
    }

    public MovementBehavior getBehavior() {
        return behavior;
    } 

    public int health() {
        return health;
    }

    public boolean isDeafeted() {
        return health <= 0;
    }

    @Override
    public EntityKind kind() {
        return EntityKind.NORMAL_ENEMY;
    }

    @Override 
    public boolean takeDamage(Damage damage) {
        if (isDeafeted()) {
            return false;
        }
        health -= damage.amount();
        return true;
    }
}
