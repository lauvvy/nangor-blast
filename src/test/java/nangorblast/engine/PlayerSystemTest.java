package nangorblast.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import nangorblast.TestWorlds;
import nangorblast.core.Direction;
import nangorblast.core.EntityKind;
import nangorblast.core.GameState;
import nangorblast.core.PlayerDamaged;
import nangorblast.core.Position;
import nangorblast.input.Action;
import nangorblast.input.Command;
import nangorblast.model.Entity;
import nangorblast.model.GameWorld;
import nangorblast.model.Hostile;
import org.junit.jupiter.api.Test;

class PlayerSystemTest {

    private static final double DT = 0.05;

    /** Stand-in for a Normal Enemy until model/enemy exists. */
    static final class Foe extends Entity implements Hostile {
        Foe(Position position) {
            super(position);
        }

        @Override
        public EntityKind kind() {
            return EntityKind.NORMAL_ENEMY;
        }

        @Override
        public boolean blocksMovementOf(Entity mover) {
            return mover != this;
        }
    }

    /** Stand-in for an active bomb the player is not standing on. */
    static final class ActiveBomb extends Entity {
        ActiveBomb(Position position) {
            super(position);
        }

        @Override
        public EntityKind kind() {
            return EntityKind.BOMB_NORMAL;
        }

        @Override
        public boolean blocksMovementOf(Entity mover) {
            return !mover.position().equals(position());
        }
    }

    private final PlayerSystem system = new PlayerSystem();

    private static GameWorld world(String... rows) {
        return TestWorlds.fromAscii(Map.of('E', Foe::new, 'o', ActiveBomb::new), rows);
    }

    /** One tick as the loop runs it: commands first, then the system. */
    private void tick(GameWorld world, Action... actions) {
        for (Action action : actions) {
            system.handle(world, new Command(0, action));
        }
        system.update(world, DT);
    }

    private static Position at(GameWorld world) {
        return world.player().position();
    }

    @Test
    void heldMoveFollowsThePlayersSpeed() {
        GameWorld world = world("P" + ".".repeat(79));
        for (int i = 0; i < 200; i++) { // 10 seconds, a move command every tick
            tick(world, Action.MOVE_RIGHT);
        }
        assertEquals(40, at(world).col(), 1, "speed 4 for 10 s");

        GameWorld fast = world("P" + ".".repeat(79));
        fast.player().increaseSpeed(); // 6 tiles per second, not a whole number of ticks
        for (int i = 0; i < 200; i++) {
            tick(fast, Action.MOVE_RIGHT);
        }
        assertEquals(60, at(fast).col(), 1, "speed 6 for 10 s");
    }

    @Test
    void oneMovePerStepNoMatterHowManyCommands() {
        GameWorld world = world("P....");
        tick(world, Action.MOVE_RIGHT, Action.MOVE_RIGHT, Action.MOVE_RIGHT);
        assertEquals(new Position(1, 0), at(world));
        assertEquals(Direction.RIGHT, world.player().direction());
    }

    @Test
    void stoppedByBlocksEnemiesActiveBombsAndTheMapEdge() {
        GameWorld world = world(
                "#####",
                "#.o.#",
                "#EP.#",
                "#####");
        tick(world, Action.MOVE_DOWN);  // wall
        tick(world, Action.MOVE_UP);    // active bomb
        assertEquals(new Position(2, 2), at(world));
        assertEquals(Direction.UP, world.player().direction(), "a blocked move still turns the player");

        GameWorld edge = world("P.");
        tick(edge, Action.MOVE_LEFT);
        tick(edge, Action.MOVE_UP);
        assertEquals(new Position(0, 0), at(edge));
    }

    @Test
    void blockedMoveCostsNoCooldown() {
        GameWorld world = world("#P..");
        tick(world, Action.MOVE_LEFT);           // wall, no step
        tick(world, Action.MOVE_RIGHT);          // the very next tick may step
        assertEquals(new Position(2, 0), at(world));
    }

    @Test
    void anEarlyMoveIsKeptFor015Seconds() {
        // Speed 4: a step every 0.25 s, so the cooldown is 5 ticks.
        GameWorld world = world("P.......");
        tick(world, Action.MOVE_RIGHT);          // tick 1, t=0: step, cooldown 0.25
        tick(world);
        tick(world, Action.MOVE_RIGHT);          // tick 3, t=0.10: pressed 0.15 s early
        tick(world);
        assertEquals(1, at(world).col());
        tick(world);                             // tick 5 ends at t=0.25: the kept move is applied
        assertEquals(2, at(world).col());
    }

    @Test
    void aMoveTooEarlyIsDropped() {
        GameWorld world = world("P.......");
        tick(world, Action.MOVE_RIGHT);
        tick(world, Action.MOVE_RIGHT);          // 0.20 s early, more than 0.15 s
        for (int i = 0; i < 10; i++) {
            tick(world);
        }
        assertEquals(1, at(world).col());
    }

    @Test
    void theLatestEarlyMoveWins() {
        GameWorld world = world(
                "P.......",
                "........");
        tick(world, Action.MOVE_RIGHT);
        tick(world);
        tick(world, Action.MOVE_UP);
        tick(world, Action.MOVE_DOWN);
        tick(world);
        tick(world);
        assertEquals(new Position(1, 1), at(world));
    }

    @Test
    void steppingIntoAnEnemyIsContactAndCostsOneLife() {
        GameWorld world = world("EP.");
        List<PlayerDamaged> events = new ArrayList<>();
        world.bus().subscribe(PlayerDamaged.class, events::add);

        tick(world, Action.MOVE_LEFT);

        assertEquals(new Position(1, 0), at(world), "player and enemy never share a tile");
        assertEquals(2, world.player().lives());
        assertEquals(List.of(new PlayerDamaged(0, 2)), events);
    }

    @Test
    void wallIsNotContact() {
        GameWorld world = world("#P.");
        tick(world, Action.MOVE_LEFT);
        assertEquals(3, world.player().lives());
    }

    @Test
    void movesAreIgnoredUnlessPlaying() {
        GameWorld world = world("P..");
        world.setState(GameState.PAUSED);
        system.handle(world, new Command(0, Action.MOVE_RIGHT));
        assertEquals(0, at(world).col());

        world.setState(GameState.PLAYING);
        system.handle(world, new Command(0, Action.PLACE_BOMB));
        system.handle(world, new Command(1, Action.MOVE_RIGHT)); // no second player yet
        assertEquals(0, at(world).col());
        assertTrue(world.player().isAlive());
    }
}
