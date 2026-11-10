package nangorblast.app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import nangorblast.core.EntityKind;
import nangorblast.core.GameState;
import nangorblast.core.Position;
import nangorblast.engine.CommandHandler;
import nangorblast.engine.GameSystem;
import nangorblast.engine.PlayerSystem;
import nangorblast.input.Action;
import nangorblast.input.Command;
import nangorblast.model.Entity;
import nangorblast.model.GameWorld;
import org.junit.jupiter.api.Test;

/** The C-05 walk-through on the real composition: menu, walk, pause, timer, LOSE, quit. */
class MvpZeroTest {

    private static final double DT = 0.05;

    private final AtomicInteger quits = new AtomicInteger();
    private final GameWorld world = StandInMap.redMountain(new Random(42));
    private final PlayerSystem players = new PlayerSystem();
    private final List<CommandHandler> handlers = GameFactory.handlers(players, quits::incrementAndGet);
    private final List<GameSystem> systems = GameFactory.systems(players);

    /** One tick in GameLoop's order. */
    private void tick(Action... actions) {
        for (Action action : actions) {
            handlers.forEach(h -> h.handle(world, new Command(0, action)));
        }
        if (world.state() == GameState.PLAYING) {
            systems.forEach(s -> s.update(world, DT));
        }
        world.removeInactive();
    }

    private void seconds(double seconds) {
        for (int i = 0; i < Math.round(seconds / DT); i++) {
            tick();
        }
    }

    @Test
    void redMountainLoadsWithTheF0733Layout() {
        assertEquals(14, world.width());
        assertEquals(10, world.height());
        assertEquals(new Position(1, 1), world.player().position());
        assertEquals(GameState.MENU, world.state());
        assertEquals(5, count(EntityKind.NORMAL_ENEMY));
        assertEquals(1, count(EntityKind.BOSS));
        assertEquals(1, count(EntityKind.EXIT_LOCKED));
        assertTrue(world.isBlockedFor(world.player(), new Position(0, 1)), "the border is solid");
    }

    private long count(EntityKind kind) {
        return world.entities().stream().map(Entity::kind).filter(kind::equals).count();
    }

    @Test
    void menuWalkPauseTimerLoseQuit() {
        tick(Action.MOVE_RIGHT);
        seconds(5);
        assertEquals(new Position(1, 1), world.player().position(), "the menu ignores moves");
        assertEquals(300.0, world.timeLeft(), "the timer waits in the menu");

        tick(Action.CONFIRM);
        tick(Action.MOVE_RIGHT);
        seconds(0.5);
        tick(Action.MOVE_RIGHT);
        seconds(0.5);
        tick(Action.MOVE_RIGHT); // Destructible Block at (4, 1)
        assertEquals(new Position(3, 1), world.player().position(), "Deeper walks and stops at a block");

        tick(Action.PAUSE);
        double paused = world.timeLeft();
        tick(Action.MOVE_LEFT);
        seconds(10);
        assertEquals(paused, world.timeLeft(), "pause freezes the timer");
        assertEquals(new Position(3, 1), world.player().position(), "pause ignores moves");

        tick(Action.PAUSE);
        seconds(paused - 1);
        assertEquals(GameState.PLAYING, world.state());
        seconds(1.1);
        assertEquals(0.0, world.timeLeft());
        assertEquals(GameState.LOSE, world.state(), "LOSE at 0 seconds");

        tick(Action.CONFIRM);
        assertEquals(1, quits.get(), "confirm on the end screen quits");
    }
}
