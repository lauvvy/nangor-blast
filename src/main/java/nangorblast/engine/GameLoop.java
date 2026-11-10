package nangorblast.engine;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.locks.LockSupport;
import java.util.function.LongSupplier;
import nangorblast.core.GameState;
import nangorblast.input.Command;
import nangorblast.model.GameWorld;
import nangorblast.view.WorldSnapshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The fixed-timestep game loop, run on the {@code game-loop} thread, the only thread that
 * changes the model. It measures real time and runs one tick of {@code dt} for each
 * {@code dt} that has passed, so game time follows real time. After a stall it runs at most
 * {@code maxCatchUpTicks} ticks and drops the rest, so the game never fast-forwards past the
 * player. This is the only class that reads the clock.
 *
 * <p>One tick: hand queued commands to the handlers, run the systems in order (only while
 * PLAYING, which is what freezes every timer during a pause), remove inactive entities, and
 * publish a new snapshot.
 */
public final class GameLoop implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(GameLoop.class);

    // M-05 moves these into config/game.properties.
    public static final int DEFAULT_TICKS_PER_SECOND = 20;
    public static final int DEFAULT_MAX_CATCH_UP_TICKS = 5;

    private final GameWorld world;
    private final BlockingQueue<Command> commands;
    private final List<CommandHandler> handlers;
    private final List<GameSystem> systems;
    private final double dt;
    private final long dtNanos;
    private final int maxCatchUpTicks;
    private final LongSupplier clock;

    private volatile WorldSnapshot snapshot;
    private volatile boolean stopRequested;

    // Owned by the game-loop thread.
    private boolean started;
    private long lastNanos;
    private long accumulatorNanos;

    /** @param clock nanoseconds, {@code System::nanoTime} in the game and a fake in tests */
    public GameLoop(GameWorld world, BlockingQueue<Command> commands, List<CommandHandler> handlers,
                    List<GameSystem> systems, int ticksPerSecond, int maxCatchUpTicks, LongSupplier clock) {
        if (ticksPerSecond <= 0) {
            throw new IllegalArgumentException("Ticks per second must be positive, got " + ticksPerSecond);
        }
        if (maxCatchUpTicks <= 0) {
            throw new IllegalArgumentException("Max catch-up ticks must be positive, got " + maxCatchUpTicks);
        }
        this.world = Objects.requireNonNull(world, "world");
        this.commands = Objects.requireNonNull(commands, "commands");
        this.handlers = List.copyOf(handlers);
        this.systems = List.copyOf(systems);
        this.dt = 1.0 / ticksPerSecond;
        this.dtNanos = 1_000_000_000L / ticksPerSecond;
        this.maxCatchUpTicks = maxCatchUpTicks;
        this.clock = Objects.requireNonNull(clock, "clock");
        this.snapshot = SnapshotBuilder.build(world);
    }

    /** The latest published snapshot. Safe to call from any thread. */
    public WorldSnapshot snapshot() {
        return snapshot;
    }

    /** Asks the loop to finish its current tick and return from {@link #run()}. Safe from any thread. */
    public void stop() {
        stopRequested = true;
    }

    @Override
    public void run() {
        LOG.info("Game loop started at {} ticks per second", Math.round(1 / dt));
        try {
            while (!stopRequested) {
                advance();
                LockSupport.parkNanos(dtNanos - accumulatorNanos);
                if (Thread.interrupted()) {
                    break;
                }
            }
        } catch (RuntimeException e) {
            LOG.error("Game loop stopped by an error", e);
        } finally {
            LOG.info("Game loop stopped");
        }
    }

    /** Runs every tick owed since the last call and returns how many ran. Game-loop thread only. */
    int advance() {
        long now = clock.getAsLong();
        if (!started) {
            started = true;
            lastNanos = now;
            return 0;
        }
        accumulatorNanos += now - lastNanos;
        lastNanos = now;

        int ticks = 0;
        while (accumulatorNanos >= dtNanos && ticks < maxCatchUpTicks) {
            tick();
            accumulatorNanos -= dtNanos;
            ticks++;
        }
        if (accumulatorNanos >= dtNanos) {
            LOG.warn("Game loop fell behind, dropping {} ticks", accumulatorNanos / dtNanos);
            accumulatorNanos %= dtNanos;
        }
        return ticks;
    }

    private void tick() {
        List<Command> batch = new ArrayList<>();
        commands.drainTo(batch);
        for (Command command : batch) {
            for (CommandHandler handler : handlers) {
                handler.handle(world, command);
            }
        }
        if (world.state() == GameState.PLAYING) {
            for (GameSystem system : systems) {
                system.update(world, dt);
            }
        }
        world.removeInactive();
        snapshot = SnapshotBuilder.build(world);
    }
}
