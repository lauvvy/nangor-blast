package nangorblast.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;
import nangorblast.TestWorlds;
import nangorblast.core.GameState;
import nangorblast.input.Action;
import nangorblast.input.Command;
import nangorblast.model.Exit;
import nangorblast.model.GameWorld;
import org.junit.jupiter.api.Test;

class GameLoopTest {

    private static final long MS = 1_000_000L;

    private final GameWorld world = TestWorlds.fromAscii("P.X");
    private final BlockingQueue<Command> commands = new LinkedBlockingQueue<>();
    private final AtomicLong clock = new AtomicLong();
    private final List<Command> handled = new ArrayList<>();
    private int systemTicks;

    private GameLoop loop() {
        GameSystem counter = (w, dt) -> systemTicks++;
        return new GameLoop(world, commands, List.of((w, c) -> handled.add(c)),
                List.of(counter, new TimerSystem()), 20, 5, clock::get);
    }

    @Test
    void gameTimeFollowsRealTimeOverAFullGame() {
        GameLoop loop = loop();
        Random frames = new Random(7);
        loop.advance();
        while (clock.get() < 300_000 * MS) {
            clock.addAndGet((1 + frames.nextInt(40)) * MS); // uneven frames, 1 to 40 ms
            loop.advance();
        }
        double gameSeconds = systemTicks * 0.05;
        assertTrue(Math.abs(gameSeconds - 300) < 1, "300 real seconds gave " + gameSeconds + " game seconds");
        assertEquals(0, world.timeLeft(), 1.0);
    }

    @Test
    void stallRunsAtMostFiveCatchUpTicks() {
        GameLoop loop = loop();
        loop.advance();
        clock.addAndGet(2_000 * MS);

        assertEquals(5, loop.advance());

        clock.addAndGet(50 * MS);
        assertEquals(1, loop.advance(), "after the stall the loop runs at normal speed");
    }

    @Test
    void pausedGameSkipsSystemsButStillHandlesCommands() {
        GameLoop loop = loop();
        world.setState(GameState.PAUSED);
        commands.add(new Command(0, Action.PAUSE));

        loop.advance();
        clock.addAndGet(100 * MS);
        loop.advance();

        assertEquals(0, systemTicks);
        assertEquals(300, world.timeLeft(), 1e-9, "the timer is frozen while paused");
        assertEquals(List.of(new Command(0, Action.PAUSE)), handled);
    }

    @Test
    void commandsReachHandlersOnceInOrder() {
        GameLoop loop = loop();
        commands.add(new Command(0, Action.MOVE_UP));
        commands.add(new Command(0, Action.PLACE_BOMB));

        loop.advance();
        clock.addAndGet(100 * MS);
        loop.advance();

        assertEquals(List.of(new Command(0, Action.MOVE_UP), new Command(0, Action.PLACE_BOMB)), handled);
        assertTrue(commands.isEmpty());
    }

    @Test
    void tickRemovesInactiveEntitiesAndPublishesSnapshot() {
        GameLoop loop = loop();
        var before = loop.snapshot();
        world.entities(Exit.class).get(0).deactivate();

        loop.advance();
        clock.addAndGet(50 * MS);
        loop.advance();

        assertNotSame(before, loop.snapshot());
        assertEquals(1, loop.snapshot().sprites().size());
    }
}
