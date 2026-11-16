package nangorblast.engine;

import java.util.Objects;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Supplier;
import nangorblast.view.Renderer;
import nangorblast.view.WorldSnapshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Runs on the {@code render} thread: draws the latest published snapshot whenever it changes.
 * It only reads snapshots, so it shares no mutable state with the game loop.
 */
public final class RenderLoop implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(RenderLoop.class);

    private final Supplier<WorldSnapshot> source;
    private final Renderer renderer;
    private final long frameNanos;
    private volatile boolean stopRequested;

    public RenderLoop(Supplier<WorldSnapshot> source, Renderer renderer, int framesPerSecond) {
        if (framesPerSecond <= 0) {
            throw new IllegalArgumentException("Frames per second must be positive, got " + framesPerSecond);
        }
        this.source = Objects.requireNonNull(source, "source");
        this.renderer = Objects.requireNonNull(renderer, "renderer");
        this.frameNanos = 1_000_000_000L / framesPerSecond;
    }

    public void stop() {
        stopRequested = true;
    }

    @Override
    public void run() {
        WorldSnapshot drawn = null;
        while (!stopRequested && !Thread.currentThread().isInterrupted()) {
            WorldSnapshot latest = source.get();
            if (latest != drawn) {
                renderer.render(latest);
                drawn = latest;
            }
            LockSupport.parkNanos(frameNanos);
        }
        LOG.info("Render loop stopped");
    }
}
