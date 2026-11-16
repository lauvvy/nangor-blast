package nangorblast.input;

import java.util.function.Consumer;

/** Reads a device and produces {@link Command}s. Implemented by the CLI and the GUI. */
public interface InputSource extends AutoCloseable {

    /**
     * Reads until {@link #close()} and sends every command to {@code sink}, which is
     * thread-safe. A blocking device such as the terminal returns only after {@code close()},
     * so the caller runs it on the input thread. An event-driven device may return at once.
     */
    void start(Consumer<Command> sink);

    /** Stops reading and releases the device. */
    @Override
    void close();
}
