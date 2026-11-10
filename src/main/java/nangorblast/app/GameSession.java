package nangorblast.app;

import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import nangorblast.engine.GameLoop;
import nangorblast.engine.RenderLoop;
import nangorblast.input.Command;
import nangorblast.input.InputSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Starts the three named threads and stops them together:
 * {@code input} reads keys into the command queue, {@code game-loop} is the only writer of
 * game state, and {@code render} draws the latest snapshot. They share only the queue and the
 * snapshot reference, so there are no locks and no deadlock.
 */
public final class GameSession implements AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(GameSession.class);
    private static final long SHUTDOWN_SECONDS = 2;

    private final GameLoop loop;
    private final InputSource input;
    private final RenderLoop render;
    private final BlockingQueue<Command> commands;
    private final ExecutorService executor = Executors.newFixedThreadPool(3);

    public GameSession(GameLoop loop, InputSource input, RenderLoop render, BlockingQueue<Command> commands) {
        this.loop = Objects.requireNonNull(loop, "loop");
        this.input = Objects.requireNonNull(input, "input");
        this.render = Objects.requireNonNull(render, "render");
        this.commands = Objects.requireNonNull(commands, "commands");
    }

    public void start() {
        executor.execute(named("input", () -> input.start(commands::offer)));
        executor.execute(named("game-loop", loop));
        executor.execute(named("render", render));
    }

    /** Stops all three threads and waits for them. */
    @Override
    public void close() {
        loop.stop();
        render.stop();
        input.close();
        executor.shutdown();
        try {
            if (!executor.awaitTermination(SHUTDOWN_SECONDS, TimeUnit.SECONDS)) {
                LOG.warn("Threads did not stop within {} s, interrupting them", SHUTDOWN_SECONDS);
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private static Runnable named(String name, Runnable task) {
        return () -> {
            Thread thread = Thread.currentThread();
            String poolName = thread.getName();
            thread.setName(name);
            try {
                task.run();
            } catch (RuntimeException e) {
                LOG.error("Thread {} failed", name, e);
            } finally {
                thread.setName(poolName);
            }
        };
    }
}
