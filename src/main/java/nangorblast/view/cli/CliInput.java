package nangorblast.view.cli;

import java.io.IOException;
import java.util.Objects;
import java.util.function.Consumer;
import nangorblast.input.Action;
import nangorblast.input.Command;
import nangorblast.input.InputSource;
import nangorblast.input.KeyBindings;
import org.jline.keymap.BindingReader;
import org.jline.keymap.KeyMap;
import org.jline.terminal.Terminal;
import org.jline.utils.InfoCmp.Capability;
import org.jline.utils.NonBlockingReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Reads key presses from the terminal on the {@code input} thread and turns each one into a
 * command. The terminal reports presses only: no key release, and a held key arrives through
 * the operating system's key repeat.
 */
public final class CliInput implements InputSource {

    private static final Logger LOG = LoggerFactory.getLogger(CliInput.class);
    private static final long POLL_MILLIS = 100; // how quickly close() takes effect
    private static final String UNKNOWN = "";

    private final Terminal terminal;
    private final KeyBindings bindings;
    private volatile boolean closed;

    public CliInput(Terminal terminal, KeyBindings bindings) {
        this.terminal = Objects.requireNonNull(terminal, "terminal");
        this.bindings = Objects.requireNonNull(bindings, "bindings");
    }

    /** Reads until {@link #close()} is called or the input ends. Run it on the input thread. */
    @Override
    public void start(Consumer<Command> sink) {
        KeyMap<String> keys = keyMap();
        NonBlockingReader in = terminal.reader();
        BindingReader reader = new BindingReader(in);
        try {
            while (!closed) {
                int next = in.peek(POLL_MILLIS);
                if (next == NonBlockingReader.READ_EXPIRED) {
                    continue;
                }
                if (next == NonBlockingReader.EOF) {
                    break;
                }
                String key = reader.readBinding(keys);
                if (key == null) {
                    break;
                }
                bindings.commandFor(key).ifPresentOrElse(sink, () -> {
                    LOG.debug("Unbound key '{}'", key);
                    sink.accept(new Command(0, Action.UNBOUND)); // menus report it as invalid input
                });
            }
        } catch (IOException e) {
            LOG.warn("Terminal input stopped", e);
        }
        LOG.info("Input stopped");
    }

    @Override
    public void close() {
        closed = true;
    }

    /** Raw terminal sequences to the key names used in {@code keybindings.properties}. */
    private KeyMap<String> keyMap() {
        KeyMap<String> keys = new KeyMap<>();
        for (char c = '!'; c <= '~'; c++) {
            keys.bind(String.valueOf(Character.toLowerCase(c)), String.valueOf(c));
        }
        keys.bind("SPACE", " ");
        keys.bind("ENTER", "\r", "\n");
        keys.bind("TAB", "\t");
        keys.bind("BACKSPACE", "\177", "\b");
        keys.bind("ESCAPE", "\033");
        bindArrow(keys, "UP", 'A', Capability.key_up);
        bindArrow(keys, "DOWN", 'B', Capability.key_down);
        bindArrow(keys, "RIGHT", 'C', Capability.key_right);
        bindArrow(keys, "LEFT", 'D', Capability.key_left);
        keys.setNomatch(UNKNOWN);
        keys.setAmbiguousTimeout(50); // a lone ESC, not the start of an arrow sequence
        return keys;
    }

    private void bindArrow(KeyMap<String> keys, String name, char ansiSuffix, Capability capability) {
        keys.bind(name, "\033[" + ansiSuffix, "\033O" + ansiSuffix);
        String fromTerminfo = KeyMap.key(terminal, capability);
        if (fromTerminfo != null && !fromTerminfo.isEmpty()) {
            keys.bind(name, fromTerminfo);
        }
    }
}
