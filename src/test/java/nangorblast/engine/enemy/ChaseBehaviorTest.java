package nangorblast.engine.enemy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.List;

import nangorblast.TestWorlds;
import nangorblast.core.Position;
import nangorblast.engine.enemy.EnemySystem;
import nangorblast.model.GameWorld;
import nangorblast.model.enemy.ChaseBehavior;
import nangorblast.model.enemy.ChaseState;
import nangorblast.model.enemy.NormalEnemy;
import nangorblast.model.enemy.PatrolBehavior;
import org.junit.jupiter.api.Test;

class ChaseBehaviorTest {
    private static final double DT = 0.05;
    
    private static ChaseBehavior addChaser(GameWorld world, Position at) {
        ChaseBehavior chase = new ChaseBehavior(new PatrolBehavior(List.of(at)));
        world.add(new NormalEnemy(at, chase));
        return chase;
    }

    @Test 
    void switchesToChaseWithinThreeTiles() {
        GameWorld world = TestWorlds.fromAscii("P.......");
        Position at = new Position(5, 0);
        ChaseBehavior chase = new ChaseBehavior(new PatrolBehavior(List.of(at)));
        NormalEnemy enemy = new NormalEnemy(at, chase);
        world.add(enemy);

        chase.update(enemy, world, DT);
        assertEquals(ChaseState.PATROL, chase.state());

        world.player().moveTo(new Position(2, 0));
        chase.update(enemy, world, DT);
        assertEquals(ChaseState.CHASE, chase.state());
    }

    @Test 
    void leaveChaseBeyondThreeTiles() {
        GameWorld world = TestWorlds.fromAscii("P.......");
        Position at = new Position(3, 0);
        ChaseBehavior chase = new ChaseBehavior(new PatrolBehavior(List.of(at)));
        NormalEnemy enemy = new NormalEnemy(at, chase);
        world.add(enemy);

        chase.update(enemy, world, DT);
        assertEquals(ChaseState.CHASE, chase.state());
    }

    @Test 
    void returnToPatrolAfterOneSecondWithNoPath() {
        GameWorld world = TestWorlds.fromAscii("P....");
        Position at = new Position(3, 0);
        ChaseBehavior chase = new ChaseBehavior(new PatrolBehavior(List.of(at)));
        NormalEnemy enemy = new NormalEnemy(at, chase);
        world.add(enemy);
        chase.update(enemy, world, DT);
        assertEquals(ChaseState.CHASE, chase.state());
        
        Position blockerAt = new Position(2, 0);
        world.add(new NormalEnemy(blockerAt, new PatrolBehavior(List.of(blockerAt))));

        for (int i = 0; i < 19; i++) {
            chase.update(enemy, world, DT);
        }
        assertEquals(ChaseState.CHASE, chase.state());
        chase.update(enemy, world, DT);
        assertEquals(ChaseState.PATROL, chase.state());        
    }

    @Test 
    void recomputesPathOnlyWhenPlayerChangesTile() {
        GameWorld world = TestWorlds.fromAscii("P.....", "......");
        Position at = new Position(3, 0);
        ChaseBehavior chase = addChaser(world, at);
        EnemySystem system = new EnemySystem();

        for (int i = 0; i < 40; i++) {
            system.update(world, DT);
        }
        assertEquals(1, chase.pathComputations());

        world.player().moveTo(new Position(0, 1));
        system.update(world, DT);
        assertEquals(2, chase.pathComputations());
    }

    @Test 
    void walksArroundsABlockToReachThePlayer() {
        GameWorld world = TestWorlds.fromAscii("....", "P#..", "....");
        Position at = new Position(2, 1);
        ChaseBehavior chase = new ChaseBehavior(new PatrolBehavior(List.of(at)));
        NormalEnemy enemy = new NormalEnemy(at, chase);
        world.add(enemy);
        EnemySystem system = new EnemySystem();

        for (int i = 0; i < 80; i++) {
            system.update(world, DT);
            assertNotEquals(new Position(1, 1), enemy.position());
        }
        assertEquals(1, enemy.position().manhattanDistance(world.player().position()));
    }
}
