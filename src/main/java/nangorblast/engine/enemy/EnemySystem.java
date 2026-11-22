package nangorblast.engine.enemy;

import java.util.Optional;
import nangorblast.core.EnemyDefeated;
import nangorblast.core.Position;
import nangorblast.engine.GameSystem;
import nangorblast.model.Damage;
import nangorblast.model.DamageSource;
import nangorblast.model.GameWorld;
import nangorblast.model.Player;
import nangorblast.model.enemy.NormalEnemy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class EnemySystem implements GameSystem {
    private static final Logger LOG = LoggerFactory.getLogger(EnemySystem.class);
    
    @Override 
    public void update(GameWorld world, double dt) {
        for (NormalEnemy enemy : world.entities(NormalEnemy.class)) {
            if (enemy.isDeafeted()) {
                defeat(world, enemy);
                continue;
            }
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
                blocked(world, enemy, target);
            } else {
                enemy.moveTo(target);
                enemy.startCooldown();
            }
        }
    }

    private static void defeat(GameWorld world, NormalEnemy enemy) {
        enemy.deactivate();
        world.bus().publish(new EnemyDefeated(enemy.position(), NormalEnemy.SCORE_VALUE, false));
        LOG.info("Normal Enemy defeated at {}", enemy.position());
    }

    private static void blocked(GameWorld world, NormalEnemy enemy, Position target) {
        Optional<Player> player = world.firstAt(target, Player.class);
        if (player.isPresent()) {
            Damage contact = new Damage(1, Damage.newActivationId(), DamageSource.ENEMY_CONTACT);
            if (player.get().takeDamage(contact)) {
                LOG.info("Normal Enemy stepped into the player at {}, {} lives left", target, player.get().lives());
            }
            enemy.startCooldown();
        } else {
            enemy.getBehavior().onBlocked();
        }
    }
}
