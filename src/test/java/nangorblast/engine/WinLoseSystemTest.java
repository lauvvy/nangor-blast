package nangorblast.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import nangorblast.TestWorlds;
import nangorblast.core.EntityKind;
import nangorblast.core.GameEnded;
import nangorblast.core.GameState;
import nangorblast.core.Position;
import nangorblast.model.Damage;
import nangorblast.model.DamageSource;
import nangorblast.model.Entity;
import nangorblast.model.Exit;
import nangorblast.model.GameWorld;
import nangorblast.model.Hostile;
import org.junit.jupiter.api.Test;

class WinLoseSystemTest {

    private static final double DT = 0.05;

    /** Stand-in for a Normal Enemy or the boss until model/enemy and model/boss exist. */
    static final class Foe extends Entity implements Hostile {
        private final EntityKind kind;

        Foe(Position position, EntityKind kind) {
            super(position);
            this.kind = kind;
        }

        @Override
        public EntityKind kind() {
            return kind;
        }

        @Override
        public boolean blocksMovementOf(Entity mover) {
            return mover != this;
        }
    }

    private final WinLoseSystem system = new WinLoseSystem();
    private final List<GameEnded> ended = new ArrayList<>();

    /** Row 0: the player and the exit. Row 1: five Normal Enemies and the boss, as in F00.3. */
    private GameWorld world() {
        GameWorld world = TestWorlds.fromAscii(Map.of(
                        'E', p -> new Foe(p, EntityKind.NORMAL_ENEMY),
                        'B', p -> new Foe(p, EntityKind.BOSS)),
                "P.X...",
                "EEEEEB");
        world.bus().subscribe(GameEnded.class, ended::add);
        return world;
    }

    private static Exit exit(GameWorld world) {
        return world.entities(Exit.class).get(0);
    }

    private static void defeat(GameWorld world, EntityKind kind, int count) {
        world.entities().stream().filter(e -> e.kind() == kind).limit(count).forEach(Entity::deactivate);
        world.removeInactive();
    }

    @Test
    void exitStaysLockedUntilEveryEnemyAndTheBossAreDefeated() {
        GameWorld world = world();
        world.player().moveTo(new Position(2, 0)); // on the locked exit

        defeat(world, EntityKind.NORMAL_ENEMY, 5);
        system.update(world, DT);
        assertFalse(exit(world).isOpen(), "the boss is still alive");
        assertEquals(GameState.PLAYING, world.state(), "standing on a locked exit is not WIN");

        defeat(world, EntityKind.BOSS, 1);
        system.update(world, DT);
        assertTrue(exit(world).isOpen());
        assertEquals(GameState.WIN, world.state());
    }

    @Test
    void winNeedsThePlayerOnTheOpenExit() {
        GameWorld world = world();
        defeat(world, EntityKind.NORMAL_ENEMY, 5);
        defeat(world, EntityKind.BOSS, 1);

        system.update(world, DT);
        assertTrue(exit(world).isOpen());
        assertEquals(GameState.PLAYING, world.state(), "the player is not on the exit yet");

        world.player().moveTo(new Position(2, 0));
        world.reduceTime(100.3);
        system.update(world, DT);
        assertEquals(GameState.WIN, world.state());
        assertEquals(List.of(new GameEnded(GameState.WIN, 3, 200)), ended, "published once with the bonus inputs");

        system.update(world, DT);
        assertEquals(1, ended.size(), "nothing more after the game ended");
    }

    @Test
    void loseWhenTheTimerReachesZero() {
        GameWorld world = world();
        world.reduceTime(299.99);
        system.update(world, DT);
        assertEquals(GameState.PLAYING, world.state());

        world.reduceTime(DT);
        system.update(world, DT);
        assertEquals(GameState.LOSE, world.state());
        assertEquals(List.of(new GameEnded(GameState.LOSE, 3, 0)), ended);
    }

    @Test
    void loseAtZeroLivesEvenOnTheOpenExit() {
        GameWorld world = world();
        defeat(world, EntityKind.NORMAL_ENEMY, 5);
        defeat(world, EntityKind.BOSS, 1);
        world.player().moveTo(new Position(2, 0));
        for (int i = 0; i < 3; i++) {
            world.player().takeDamage(new Damage(1, Damage.newActivationId(), DamageSource.HAZARD));
        }
        system.update(world, DT);
        assertEquals(GameState.LOSE, world.state());
    }

    @Test
    void doesNothingOutsidePlaying() {
        GameWorld world = world();
        world.reduceTime(300);
        world.setState(GameState.PAUSED);
        system.update(world, DT);
        assertEquals(GameState.PAUSED, world.state());
        assertTrue(ended.isEmpty());
    }

    @Test
    void gameEndedRefusesStatesThatAreNotAnEnd() {
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class,
                () -> new GameEnded(GameState.PAUSED, 3, 10));
    }
}
