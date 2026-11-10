package nangorblast.input;

import java.util.function.Consumer;

/** Reads a device and produces {@link Command}s. Implemented by the CLI and the GUI. */
public interface InputSource extends AutoCloseable {

    /** Starts reading. Every command goes to {@code sink}, which must be thread-safe. */
    void start(Consumer<Command> sink);

    /** Stops reading and releases the device. */
    @Override
    void close();
}
