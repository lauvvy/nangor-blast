package nangorblast.view.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import nangorblast.core.EntityKind;
import nangorblast.core.GameState;
import nangorblast.core.Position;
import nangorblast.input.KeyBindings;
import nangorblast.view.WorldSnapshot;
import nangorblast.view.WorldSnapshot.Sprite;
import org.jline.terminal.Size;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CliRendererTest {

    private static final String CLEAR = "\033[H\033[2J";

    private final ByteArrayOutputStream out = new ByteArrayOutputStream();
    private Terminal terminal;
    private CliRenderer renderer;

    @BeforeEach
    void openTerminal() throws Exception {
        terminal = TerminalBuilder.builder().system(false).type("xterm-256color")
                .streams(new ByteArrayInputStream(new byte[0]), out).build();
        terminal.setSize(new Size(80, 24));
        renderer = new CliRenderer(terminal, KeyBindings.load(Path.of("config", "keybindings.properties")));
    }

    @AfterEach
    void closeTerminal() throws Exception {
        terminal.close();
    }

    private static WorldSnapshot playerAt(int x, int y) {
        return CliFrameTest.snapshot(List.of(new Sprite(1, EntityKind.PLAYER, new Position(x, y))),
                Set.of(), 300, GameState.PLAYING);
    }

    private String drain() {
        String written = out.toString(StandardCharsets.UTF_8);
        out.reset();
        return written;
    }

    @Test
    void rewritesOnlyChangedCells() {
        renderer.render(playerAt(1, 1));
        String first = drain();
        assertTrue(first.startsWith(CLEAR), "the first frame clears the screen");
        assertTrue(first.contains("Lives 3"));

        renderer.render(playerAt(1, 1));
        assertEquals("", drain(), "an identical frame writes nothing");

        renderer.render(playerAt(2, 1));
        String move = drain();
        assertFalse(move.contains(CLEAR), "a move never clears the screen");
        assertEquals(2, move.split("\033\\[\\d+;\\d+H", -1).length - 1, "two tiles rewritten: " + move);
        assertTrue(move.length() * 10 < first.length(), "a move writes a small fraction of a full frame");
    }

    @Test
    void helpTextNamesKeysFromTheBindings() {
        renderer.render(CliFrameTest.snapshot(List.of(), Set.of(), 300, GameState.MENU));
        assertTrue(drain().contains("Nangor Blast. Enter to play, Backspace to quit"));
        assertEquals("P", CliRenderer.displayName(List.of("p", "ESCAPE")));
        assertEquals("?", CliRenderer.displayName(List.of()));
    }

    @Test
    void resizeRedrawsEverything() {
        renderer.render(playerAt(1, 1));
        drain();
        terminal.setSize(new Size(100, 30));
        renderer.render(playerAt(1, 1));
        String redraw = drain();
        assertTrue(redraw.startsWith(CLEAR) && redraw.contains("Lives 3"), redraw);
    }

    @Test
    void smallTerminalAsksToEnlargeOnceThenDrawsWhenBigEnough() {
        terminal.setSize(new Size(40, 10));
        renderer.render(playerAt(1, 1));
        String message = drain();
        assertTrue(message.contains("Enlarge the terminal to at least 48 x 13 (now 40 x 10)"), message);
        assertFalse(message.contains("Lives"));

        renderer.render(playerAt(2, 1));
        assertEquals("", drain(), "the message is not rewritten every frame");

        terminal.setSize(new Size(48, 13));
        renderer.render(playerAt(2, 1));
        assertTrue(drain().contains("Lives 3"), "the game is drawn once the terminal is big enough");
    }
}
