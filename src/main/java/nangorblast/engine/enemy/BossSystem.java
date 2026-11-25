package nangorblast.engine.enemy;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import nangorblast.core.EnemyDefeated;
import nangorblast.core.Position;
import nangorblast.engine.GameSystem;
import nangorblast.model.Damage;
import nangorblast.model.DamageSource;
import nangorblast.model.GameWorld;
import nangorblast.model.Player;
import nangorblast.model.boss.AttackPattern;
import nangorblast.model.boss.Boss;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class BossSystem implements GameSystem {
    private static final Logger LOG = LoggerFactory.getLogger(BossSystem.class);

    @Override
    public void update(GameWorld world, double dt) {
        for (Boss boss : world.entities(Boss.class)) {
            if (boss.isDefeated()) {
                defeat(world, boss);
                continue;
            }
            boss.advance(dt);
            switch (boss.state()) {
                case COOLDOWN -> whileCooldown(world, boss);
                case WARNING -> whileWarning(world, boss);
                case ACTIVE -> whileActive(world, boss);
            }
        }
    }

    private static void whileCooldown(GameWorld world, Boss boss) {
        if (!boss.isTimerDone()) {
            return;
        }
        AttackPattern next = choose(world, boss);
        if (next == null) {
            return;
        }
        Set<Position> tiles = next.tiles(boss, world, boss.phase());
        boss.startWarning(next, tiles);
        world.setWarnings(boss, tiles);
        LOG.debug("Boss at {} warns {} on {} tiles", boss.position(), next.name(), tiles.size());
    }

    private static void whileWarning(GameWorld world, Boss boss) {
        if (!boss.isTimerDone()) {
            return;
        }
        world.clearWarnings(boss);
        boss.startActive(boss.currentPattern().duration(boss.phase()));
        LOG.debug("Boss at {} attacks with {}", boss.position(), boss.currentPattern().name());
    }

    private static void whileActive(GameWorld world, Boss boss) {
        for (Position tile : boss.tiles()) {
            world.firstAt(tile, Player.class).ifPresent(player -> 
                    player.takeDamage(new Damage(1, boss.activationId(), DamageSource.BOSS_ATTACK))); {
            }
        }
        if (boss.isTimerDone()) {
            boss.startCooldown();
            LOG.debug("Boss at {} cools down, vulnerable: {}", boss.activationId(), DamageSource.BOSS_ATTACK);
        }
    }
    private static AttackPattern choose(GameWorld world, Boss boss) {
        List<AttackPattern> usable = new ArrayList<>();
        for (AttackPattern pattern : boss.patterns()) {
            if (pattern.isUsable(boss, world, boss.phase())) {
                usable.add(pattern);
            }
        }
        if (usable.size() > 1) {
            usable.remove(boss.lastPattern());
        }
        if (usable.isEmpty()) {
            return null;
        }
        return usable.get(world.random().nextInt(usable.size()));
    }

    private static void defeat(GameWorld world, Boss boss) {
        world.clearWarnings(boss);
        boss.deactivate();
        world.bus().publish(new EnemyDefeated(boss.position(), Boss.SCORE_VALUE, true));
        LOG.info("Boss defeated at {}", boss.position());
    }
}
