package nangorblast.view.cli;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import org.jline.keymap.BindingReader;
import org.jline.keymap.KeyMap;
import org.jline.terminal.Size;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.InfoCmp.Capability;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * S-02 spike: raw key input and a 20 FPS redraw with JLine 3, run with {@code make spike}.
 * It answers three questions for each member's terminal: are keys read without Enter,
 * does the redraw flicker, and are key release or two held keys reported.
 */
public final class TerminalSpike {

    private static final Logger LOG = LoggerFactory.getLogger(TerminalSpike.class);

    private static final int COLS = 14;
    private static final int ROWS = 10;
    private static final long FRAME_NANOS = 50_000_000L; // 20 frames per second
    private static final int HISTORY = 8;

    private record KeyEvent(String action, String raw, long atNanos) {
    }

    private final Terminal terminal;
    private final BlockingQueue<KeyEvent> events = new LinkedBlockingQueue<>();
    private volatile boolean running = true;

    // Owned by the render thread only.
    private int playerCol = 1;
    private int playerRow = 1;
    private long lastEventNanos;
    private int keyCount;
    private final Deque<String> history = new ArrayDeque<>();
    private int frames;
    private int fps;
    private long fpsWindowStart = System.nanoTime();
    private long worstFrameMicros;

    private TerminalSpike(Terminal terminal) {
        this.terminal = terminal;
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        try (Terminal terminal = TerminalBuilder.builder().system(true).build()) {
            LOG.info("Terminal type={} size={}", terminal.getType(), terminal.getSize());
            new TerminalSpike(terminal).run();
        }
    }

    private void run() throws InterruptedException {
        terminal.handle(Terminal.Signal.INT, signal -> running = false);
        var savedAttributes = terminal.enterRawMode();
        terminal.puts(Capability.enter_ca_mode);
        terminal.puts(Capability.cursor_invisible);
        terminal.puts(Capability.clear_screen);
        terminal.flush();

        Thread input = new Thread(this::readKeys, "input");
        input.setDaemon(true); // blocked in read() at exit; closing the terminal ends it
        Thread render = new Thread(this::renderLoop, "render");
        input.start();
        render.start();
        render.join();

        terminal.puts(Capability.cursor_visible);
        terminal.puts(Capability.exit_ca_mode);
        terminal.setAttributes(savedAttributes);
        terminal.flush();
        LOG.info("Spike finished: {} keys, worst frame {} us", keyCount, worstFrameMicros);
        System.out.printf("Spike finished. Terminal: %s, keys read: %d, worst frame: %d us%n",
                terminal.getType(), keyCount, worstFrameMicros);
    }

    private void readKeys() {
        KeyMap<String> keys = new KeyMap<>();
        bind(keys, "UP", "w", "W", "\033[A", "\033OA", KeyMap.key(terminal, Capability.key_up));
        bind(keys, "DOWN", "s", "S", "\033[B", "\033OB", KeyMap.key(terminal, Capability.key_down));
        bind(keys, "RIGHT", "d", "D", "\033[C", "\033OC", KeyMap.key(terminal, Capability.key_right));
        bind(keys, "LEFT", "a", "A", "\033[D", "\033OD", KeyMap.key(terminal, Capability.key_left));
        bind(keys, "QUIT", "q", "Q");
        keys.setNomatch("OTHER");
        keys.setAmbiguousTimeout(100);

        BindingReader reader = new BindingReader(terminal.reader());
        try {
            while (running) {
                String action = reader.readBinding(keys);
                if (action == null) { // end of input
                    running = false;
                    break;
                }
                events.add(new KeyEvent(action, reader.getLastBinding(), System.nanoTime()));
                if (action.equals("QUIT")) {
                    running = false;
                }
            }
        } catch (RuntimeException e) {
            LOG.warn("Input thread stopped", e);
            running = false;
        }
    }

    private static void bind(KeyMap<String> keys, String action, String... sequences) {
        // KeyMap.key returns null when the terminal lacks the capability, such as a dumb terminal.
        keys.bind(action, Arrays.stream(sequences).filter(Objects::nonNull).toArray(String[]::new));
    }

    private void renderLoop() {
        while (running) {
            long start = System.nanoTime();
            applyEvents();
            draw();
            long used = System.nanoTime() - start;
            worstFrameMicros = Math.max(worstFrameMicros, used / 1000);
            long sleepNanos = FRAME_NANOS - used;
            if (sleepNanos > 0) {
                try {
                    Thread.sleep(sleepNanos / 1_000_000, (int) (sleepNanos % 1_000_000));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    running = false;
                }
            }
        }
        applyEvents(); // count keys that arrived after the last frame, such as the q that quit
    }

    private void applyEvents() {
        KeyEvent event;
        while ((event = events.poll()) != null) {
            keyCount++;
            long gapMs = lastEventNanos == 0 ? 0 : (event.atNanos() - lastEventNanos) / 1_000_000;
            lastEventNanos = event.atNanos();
            history.addFirst(String.format("%-6s %-10s +%d ms", event.action(), printable(event.raw()), gapMs));
            if (history.size() > HISTORY) {
                history.removeLast();
            }
            switch (event.action()) {
                case "UP" -> move(0, -1);
                case "DOWN" -> move(0, 1);
                case "LEFT" -> move(-1, 0);
                case "RIGHT" -> move(1, 0);
                default -> { }
            }
        }
    }

    private void move(int dCol, int dRow) {
        int col = playerCol + dCol;
        int row = playerRow + dRow;
        if (col > 0 && col < COLS - 1 && row > 0 && row < ROWS - 1) {
            playerCol = col;
            playerRow = row;
        }
    }

    private void draw() {
        frames++;
        long now = System.nanoTime();
        if (now - fpsWindowStart >= 1_000_000_000L) {
            fps = frames;
            frames = 0;
            fpsWindowStart = now;
        }
        long sinceLastKeyMs = lastEventNanos == 0 ? -1 : (now - lastEventNanos) / 1_000_000;
        Size size = terminal.getSize();

        List<String> lines = new ArrayList<>();
        lines.add("Nangor Blast - terminal spike (S-02)   q quits");
        lines.add(String.format("terminal %s  size %dx%d  fps %d  keys %d  since last key %s",
                terminal.getType(), size.getColumns(), size.getRows(), fps, keyCount,
                sinceLastKeyMs < 0 ? "-" : sinceLastKeyMs + " ms"));
        for (int row = 0; row < ROWS; row++) {
            StringBuilder line = new StringBuilder();
            for (int col = 0; col < COLS; col++) {
                boolean wall = row == 0 || col == 0 || row == ROWS - 1 || col == COLS - 1;
                char glyph = (row == playerRow && col == playerCol) ? 'P' : wall ? '#' : '.';
                line.append(glyph).append(' '); // two characters per tile, as in DESIGN.md
            }
            lines.add(line.toString());
        }
        lines.add("Move with WASD or arrows. Checks:");
        lines.add(" 1. Hold W: repeats appear. Release it: does any event arrive?");
        lines.add(" 2. Hold W, then also hold D: do both repeat, or only D?");
        lines.add(" 3. Watch the map while moving: any flicker?");
        lines.add("Last keys (newest first):");
        lines.addAll(history);

        // Overwrite in place from the top-left corner. Never clear the screen, which flickers.
        terminal.puts(Capability.cursor_home);
        var writer = terminal.writer();
        for (String line : lines) {
            writer.print(line);
            terminal.puts(Capability.clr_eol);
            writer.print("\r\n");
        }
        terminal.puts(Capability.clr_eos);
        terminal.flush();
    }

    private static String printable(String raw) {
        if (raw == null) {
            return "";
        }
        StringBuilder out = new StringBuilder();
        for (char c : raw.toCharArray()) {
            if (c == '\033') {
                out.append("ESC");
            } else if (c < 32) {
                out.append('^').append((char) (c + 64));
            } else {
                out.append(c);
            }
        }
        return out.toString();
    }
}
