package nangorblast.model.enemy;

import nangorblast.core.EntityKind;
import nangorblast.core.Position;

public final class NormalEnemy extends Enemy {
    public static final double DEFAULT_SPEED = 2.0;

    private final MovementBehavior behavior;

    public NormalEnemy(Position position, MovementBehavior behavior) {
        super(position, DEFAULT_SPEED);
        this.behavior = behavior;
    }

    public MovementBehavior getBehavior() {
        return behavior;
    } 

    @Override
    public EntityKind kind() {
        return EntityKind.NORMAL_ENEMY;
    }
}
