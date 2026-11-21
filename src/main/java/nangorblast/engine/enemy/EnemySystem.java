package nangorblast.engine.enemy;

import nangorblast.core.Position;
import nangorblast.engine.GameSystem;
import nangorblast.model.GameWorld;
import nangorblast.model.enemy.NormalEnemy;

public final class EnemySystem implements GameSystem {
    @Override 
    public void update(GameWorld world, double dt) {
        for (NormalEnemy enemy : world.entities(NormalEnemy.class)) {
            enemy.tick(dt);
            enemy.getBehavior().update(enemy, world, dt);
            if (!enemy.isReadyToMove()) {
                continue;
            }
            Position target = enemy.getBehavior().nextTile(enemy);
            if (target.equals(enemy.position())) {
                continue;
            }
            if (world.isBlockedFor(enemy, target)){
                enemy.getBehavior().onBlocked();
            } else {
                enemy.moveTo(target);
                enemy.startCooldown();
            }
        }
    }
}
