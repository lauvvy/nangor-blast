package nangorblast.app;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import nangorblast.core.GameException;
import nangorblast.engine.GameLoop;
import nangorblast.engine.PlayerSystem;
import nangorblast.engine.RenderLoop;
import nangorblast.input.Command;
import nangorblast.input.KeyBindings;
import nangorblast.model.GameWorld;
import nangorblast.view.cli.CliInput;
import nangorblast.view.cli.CliRenderer;
import org.jline.terminal.Attributes;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.InfoCmp.Capability;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Entry point of the CLI game ({@code make run}). Takes over the terminal, runs one game
 * until the player quits, and gives the terminal back as it was.
 */
public final class Main {

    private static final Logger LOG = LoggerFactory.getLogger(Main.class);
    private static final int FRAMES_PER_SECOND = 20;

    private Main() {
    }

    public static void main(String[] args) {
        int status = run();
        if (status != 0) {
            System.exit(status);
        }
    }

    /** Runs the game in the system terminal. Returns the process exit status. */
    static int run() {
        LOG.info("Nangor Blast starting");
        int status = 0;
        try {
            KeyBindings bindings = KeyBindings.load(GameFactory.KEY_BINDINGS);
            try (Terminal terminal = TerminalBuilder.builder().system(true).build()) {
                LOG.info("Terminal type={} size={}", terminal.getType(), terminal.getSize());
                if (terminal.getType().startsWith(Terminal.TYPE_DUMB)) {
                    fail("This terminal cannot be controlled (type " + terminal.getType()
                            + "). Run the game in Windows Terminal, PowerShell, or a Unix terminal.");
                    return 1;
                }
                play(terminal, bindings);
            }
        } catch (GameException e) {
            fail(e.getMessage()); // raised before play starts or after the terminal is restored
            status = 1;
        } catch (IOException e) {
            fail("Cannot open the terminal: " + e.getMessage());
            status = 1;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        LOG.info("Nangor Blast exiting");
        return status;
    }

    /** Plays one game on {@code terminal} until the player quits. Package-private for tests. */
    static void play(Terminal terminal, KeyBindings bindings) throws InterruptedException {
        CountDownLatch quit = new CountDownLatch(1);
        terminal.handle(Terminal.Signal.INT, signal -> quit.countDown());
        Attributes saved = terminal.enterRawMode();
        terminal.puts(Capability.enter_ca_mode);
        terminal.puts(Capability.cursor_invisible);
        terminal.flush();
        try {
            GameWorld world = GameFactory.newWorld();
            BlockingQueue<Command> commands = new LinkedBlockingQueue<>();
            PlayerSystem players = new PlayerSystem();
            GameLoop loop = new GameLoop(world, commands, GameFactory.handlers(players, quit::countDown),
                    GameFactory.systems(players), GameLoop.DEFAULT_TICKS_PER_SECOND,
                    GameLoop.DEFAULT_MAX_CATCH_UP_TICKS, System::nanoTime);
            RenderLoop render = new RenderLoop(loop::snapshot, new CliRenderer(terminal, bindings), FRAMES_PER_SECOND);
            try (GameSession session = new GameSession(loop, new CliInput(terminal, bindings), render, commands)) {
                session.start();
                quit.await();
            }
        } finally {
            terminal.puts(Capability.cursor_visible);
            terminal.puts(Capability.exit_ca_mode);
            terminal.setAttributes(saved);
            terminal.flush();
        }
    }

    private static void fail(String message) {
        LOG.error(message);
        System.err.println("Nangor Blast: " + message);
    }
}
