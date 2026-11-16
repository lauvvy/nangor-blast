package nangorblast.app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import nangorblast.TestWorlds;
import nangorblast.engine.GameLoop;
import nangorblast.engine.PlayerSystem;
import nangorblast.engine.RenderLoop;
import nangorblast.input.Action;
import nangorblast.input.Command;
import nangorblast.input.InputSource;
import nangorblast.model.GameWorld;
import nangorblast.view.WorldSnapshot;
import org.junit.jupiter.api.Test;

class GameSessionTest {

    /** Sends one command, then blocks like a terminal until closed. */
    private static final class FakeInput implements InputSource {
        private final CountDownLatch closed = new CountDownLatch(1);

        @Override
        public void start(Consumer<Command> sink) {
            sink.accept(new Command(0, Action.PAUSE));
            try {
                closed.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        @Override
        public void close() {
            closed.countDown();
        }
    }

    private static Set<String> liveThreadNames() {
        return Thread.getAllStackTraces().keySet().stream()
                .filter(Thread::isAlive).map(Thread::getName).collect(Collectors.toSet());
    }

    @Test
    void runsThreeNamedThreadsAndStopsThemAll() throws Exception {
        GameWorld world = TestWorlds.fromAscii("P..");
        BlockingQueue<Command> commands = new LinkedBlockingQueue<>();
        List<Command> handled = new CopyOnWriteArrayList<>();
        List<WorldSnapshot> drawn = new CopyOnWriteArrayList<>();
        CountDownLatch threeFrames = new CountDownLatch(3);

        GameLoop loop = new GameLoop(world, commands, List.of((w, c) -> handled.add(c)),
                GameFactory.systems(new PlayerSystem()), 20, 5, System::nanoTime);
        RenderLoop render = new RenderLoop(loop::snapshot, s -> {
            drawn.add(s);
            threeFrames.countDown();
        }, 20);

        GameSession session = new GameSession(loop, new FakeInput(), render, commands);
        session.start();
        assertTrue(threeFrames.await(3, TimeUnit.SECONDS), "the render thread drew frames");
        assertTrue(liveThreadNames().containsAll(Set.of("input", "game-loop", "render")),
                "threads are named: " + liveThreadNames());

        long started = System.nanoTime();
        session.close();
        long stopMillis = (System.nanoTime() - started) / 1_000_000;

        assertTrue(stopMillis < 1000, "stopped in " + stopMillis + " ms");
        Set<String> after = liveThreadNames();
        assertFalse(after.contains("input") || after.contains("game-loop") || after.contains("render"),
                "no game thread left: " + after);
        assertEquals(List.of(new Command(0, Action.PAUSE)), handled, "the key reached the loop through the queue");
        assertTrue(world.timeLeft() < 300, "the timer ran on the game-loop thread");
    }
}
