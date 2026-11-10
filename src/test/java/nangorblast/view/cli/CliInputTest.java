package nangorblast.view.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import nangorblast.input.Action;
import nangorblast.input.Command;
import nangorblast.input.KeyBindings;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.junit.jupiter.api.Test;

class CliInputTest {

    @Test
    void keysFromConfigBecomeCommands() throws Exception {
        byte[] typed = "W\033[Bx \033[D".getBytes(StandardCharsets.US_ASCII);
        KeyBindings bindings = KeyBindings.load(Path.of("config", "keybindings.properties"));
        List<Command> received = new ArrayList<>();

        try (Terminal terminal = TerminalBuilder.builder().system(false).type("xterm")
                .streams(new ByteArrayInputStream(typed), OutputStream.nullOutputStream()).build()) {
            new CliInput(terminal, bindings).start(received::add); // returns at end of input
        }

        assertEquals(List.of(
                new Command(0, Action.MOVE_UP),     // W, case ignored
                new Command(0, Action.MOVE_DOWN),   // down arrow
                new Command(0, Action.UNBOUND),     // x has no binding
                new Command(0, Action.PLACE_BOMB),  // space
                new Command(0, Action.MOVE_LEFT)),  // left arrow
                received);
    }
}
