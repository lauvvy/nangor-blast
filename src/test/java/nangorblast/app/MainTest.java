package nangorblast.app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import nangorblast.input.KeyBindings;
import org.jline.terminal.Size;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.junit.jupiter.api.Test;

class MainTest {

    @Test
    void refusesATerminalItCannotControl() {
        // Gradle test workers have no console, so JLine falls back to a dumb terminal.
        assertEquals(1, Main.run());
    }

    @Test
    void playsFromMenuToQuitAndRestoresTheTerminal() throws Exception {
        // Enter starts, d steps right, p pauses, Backspace quits from the pause.
        byte[] keys = "\rdp\177".getBytes(StandardCharsets.US_ASCII);
        ByteArrayOutputStream screen = new ByteArrayOutputStream();
        try (Terminal terminal = TerminalBuilder.builder().system(false).type("xterm-256color")
                .streams(new ByteArrayInputStream(keys), screen).build()) {
            terminal.setSize(new Size(80, 24));
            KeyBindings bindings = KeyBindings.load(GameFactory.KEY_BINDINGS);
            assertTimeoutPreemptively(Duration.ofSeconds(5), () -> Main.play(terminal, bindings));
        }
        String drawn = screen.toString(StandardCharsets.UTF_8);
        assertTrue(drawn.contains("Lives 3"), "the HUD was drawn");
        assertTrue(drawn.contains("\033[?1049h"), "the alternate screen was entered");
        assertTrue(drawn.contains("\033[?1049l"), "the terminal was given back");
    }
}
