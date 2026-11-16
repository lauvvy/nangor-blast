package nangorblast.engine.enemy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.List;
import nangorblast.TestWorlds;
import nangorblast.core.Position;
import nangorblast.model.GameWorld;
import nangorblast.model.enemy.NormalEnemy;
import nangorblast.model.enemy.PatrolBehavior;
import org.junit.jupiter.api.Test;

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
}