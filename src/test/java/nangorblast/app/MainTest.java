package nangorblast.app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import nangorblast.input.KeyBindings;
import org.jline.terminal.Size;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.junit.jupiter.api.Test;

class MainTest {

    private static final String CLEAR_SCREEN = "\033[H\033[2J";

    @Test
    void refusesATerminalItCannotControl() {
        // Gradle test workers have no console, so JLine falls back to a dumb terminal.
        assertEquals(1, Main.run());
    }

    @Test
    void playsMenuToPauseToMenuToExitAndRestoresTheTerminal() throws Exception {
        PipedOutputStream keyboard = new PipedOutputStream();
        ByteArrayOutputStream screen = new ByteArrayOutputStream();
        try (Terminal terminal = TerminalBuilder.builder().system(false).type("xterm-256color")
                .streams(new PipedInputStream(keyboard), screen).build()) {
            terminal.setSize(new Size(80, 24));
            KeyBindings bindings = KeyBindings.load(GameFactory.KEY_BINDINGS);
            CompletableFuture<Void> game = CompletableFuture.runAsync(() -> {
                try {
                    Main.play(terminal, bindings);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });

            type(keyboard, "\rdp");                     // Start Game, step right, pause
            awaitScreen(screen, "Paused", 1);
            type(keyboard, "3");                        // pause menu: Main Menu starts a new session
            awaitScreen(screen, CLEAR_SCREEN, 2);       // each session's renderer starts with a clear
            type(keyboard, "3");                        // main menu: Exit
            game.get(5, TimeUnit.SECONDS);
        }
        String drawn = screen.toString(StandardCharsets.UTF_8);
        assertTrue(drawn.contains("Lives 3"), "the HUD was drawn");
        assertTrue(drawn.contains("\033[?1049h"), "the alternate screen was entered");
        assertTrue(drawn.contains("\033[?1049l"), "the terminal was given back");
    }

    private static void type(PipedOutputStream keyboard, String keys) throws Exception {
        keyboard.write(keys.getBytes(StandardCharsets.US_ASCII));
        keyboard.flush();
    }

    /** Waits until {@code text} has been drawn at least {@code times} times. */
    private static void awaitScreen(ByteArrayOutputStream screen, String text, int times) throws Exception {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (screen.toString(StandardCharsets.UTF_8).split(Pattern.quote(text), -1).length - 1 < times) {
            assertTrue(System.nanoTime() < deadline, "timed out waiting for " + text);
            Thread.sleep(20);
        }
    }
}
