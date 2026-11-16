package nangorblast.model.enemy;

import nangorblast.core.Position;
import nangorblast.model.Entity;
import nangorblast.model.Hostile;
import nangorblast.model.Player;

public abstract class Enemy extends Entity implements Hostile {
    private final double speed;
    private double cooldown = 0;

    protected Enemy(Position position, double speed) {
        super(position);
        this.speed = speed;
    }

    public double getSpeed() {
        return speed;
    }

    public boolean isReadyToMove() {
        return cooldown <= 1e-9;
    }

    public void tick(double dt) {
        cooldown = Math.max(0, cooldown - dt);
    }

    public void startCooldown() {
        cooldown += 1.0/speed;
    }

    @Override
    public boolean blocksMovementOf(Entity mover) {
        if (mover instanceof Player || mover instanceof Enemy) {
            return true;
        }
        return false;
    }
}
