package nangorblast.engine.enemy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import nangorblast.TestWorlds;
import nangorblast.core.EnemyDefeated;
import nangorblast.core.Position;
import nangorblast.model.Damage;
import nangorblast.model.DamageSource;
import nangorblast.model.GameWorld;
import nangorblast.model.Player;
import nangorblast.model.enemy.NormalEnemy;
import nangorblast.model.enemy.PatrolBehavior;

class EnemySystemTest {
    private static final double DT = 0.05;

    private static void run(EnemySystem system, GameWorld world, int ticks) {
        for (int i = 0; i < ticks; i++) {
            system.update(world, DT);
        }
    }

    private static NormalEnemy patroller(int... cols) {
        List<Position> path = java.util.Arrays.stream(cols).mapToObj(c -> new Position(c, 0)).toList();
        return new NormalEnemy(path.get(0), new PatrolBehavior(path));
    }

    @Test
    void followsPathAndTurnsAtTheEnd() {
        GameWorld world = TestWorlds.fromAscii("P.....");
        NormalEnemy enemy = patroller(2, 3, 4);
        world.add(enemy);
        EnemySystem system = new EnemySystem();

        run(system, world, 1);
        assertEquals(new Position(3, 0), enemy.position());
        run(system, world, 10);
        assertEquals(new Position(4, 0), enemy.position());
        run(system, world, 10);
        assertEquals(new Position(3, 0), enemy.position());
    }

    @Test
    void turnsBackWhenTheNextTileIsAWall() {
        GameWorld world = TestWorlds.fromAscii("P..#..");
        NormalEnemy enemy = patroller(1, 2, 3); // tile 3 adalah tembok
        world.add(enemy);
        EnemySystem system = new EnemySystem();

        boolean returned = false;
        for (int i = 0; i < 60; i++) {
            run(system, world, 1);
            assertNotEquals(new Position(3, 0), enemy.position());
            if (enemy.position().equals(new Position(1, 0))) {
                returned = true;
            }
        }
        assertEquals(true, returned);
    }

    @Test
    void neverSharesATileWithAnotherEnemy() {
        GameWorld world = TestWorlds.fromAscii("P.....");
        NormalEnemy a = patroller(2, 3, 4);
        NormalEnemy b = patroller(3); // diam di kolom 3
        world.add(a);
        world.add(b);
        EnemySystem system = new EnemySystem();

        for (int i = 0; i < 30; i++) {
            run(system, world, 1);
            assertNotEquals(a.position(), b.position());
        }
    }

    @Test
    void enemyStepsIntoPlayerCostOneLifeWithoutSharingTile() {
        GameWorld world = TestWorlds.fromAscii("P.");
        Position at = new Position(1, 0);
        NormalEnemy enemy = new NormalEnemy(at,
                new PatrolBehavior(List.of(at, world.player().position())));
        world.add(enemy);

        new EnemySystem().update(world, DT);

        assertEquals(Player.START_LIVES - 1, world.player().lives());
        assertNotEquals(world.player().position(), enemy.position());
    }

    @Test 
    void defeatedEnemyIsDeactivatedByTheSystem() {
        GameWorld world = TestWorlds.fromAscii("P...");
        Position at = new Position(3, 0);
        NormalEnemy enemy = new NormalEnemy(at, new PatrolBehavior(List.of(at)));
        world.add(enemy);

        assertTrue(enemy.takeDamage(new Damage(1, Damage.newActivationId(), DamageSource.EXPLOSION)));
        assertTrue(enemy.isActive());

        new EnemySystem().update(world, DT);
        assertFalse(enemy.isActive());
    }

    @Test
    void enemyDefeatedEventIsPublishedOnce() {
        GameWorld world = TestWorlds.fromAscii("P...");
        Position at = new Position(3, 0);
        NormalEnemy enemy = new NormalEnemy(at, new PatrolBehavior(List.of(at)));
        world.add(enemy);
        EnemySystem system = new EnemySystem();

        List<EnemyDefeated> events = new ArrayList<>();
        world.bus().subscribe(EnemyDefeated.class, events::add);

        enemy.takeDamage(new Damage(1, Damage.newActivationId(), DamageSource.EXPLOSION));
        run(system, world, 3);

        assertEquals(1, events.size());
        assertEquals(NormalEnemy.SCORE_VALUE, events.get(0).scoreValue());
        assertFalse(events.get(0).boss());
    }
}