package nangorblast.app;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import nangorblast.core.GameException;
import nangorblast.core.GameState;
import nangorblast.engine.GameLoop;
import nangorblast.engine.GameFlow;
import nangorblast.engine.GameFlow.Outcome;
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

    /**
     * Plays on {@code terminal} until the player exits. Each session runs one world; restart
     * and return to the menu start a new session with a new world, so nothing carries over.
     * Package-private for tests.
     */
    static void play(Terminal terminal, KeyBindings bindings) throws InterruptedException {
        BlockingQueue<Outcome> outcomes = new LinkedBlockingQueue<>();
        terminal.handle(Terminal.Signal.INT, signal -> outcomes.offer(Outcome.QUIT));
        Attributes saved = terminal.enterRawMode();
        terminal.puts(Capability.enter_ca_mode);
        terminal.puts(Capability.cursor_invisible);
        terminal.flush();
        try {
            Outcome next = Outcome.MENU;
            while (next != Outcome.QUIT) {
                GameWorld world = GameFactory.newWorld();
                if (next == Outcome.RESTART) {
                    world.setState(GameState.PLAYING);
                }
                next = session(terminal, bindings, world, outcomes);
            }
        } finally {
            terminal.puts(Capability.cursor_visible);
            terminal.puts(Capability.exit_ca_mode);
            terminal.setAttributes(saved);
            terminal.flush();
        }
    }

    private static Outcome session(Terminal terminal, KeyBindings bindings, GameWorld world,
                                   BlockingQueue<Outcome> outcomes) throws InterruptedException {
        BlockingQueue<Command> commands = new LinkedBlockingQueue<>();
        GameFlow flow = new GameFlow(bindings::label, outcomes::offer);
        GameLoop loop = GameFactory.newLoop(world, commands, flow);
        RenderLoop render = new RenderLoop(loop::snapshot, new CliRenderer(terminal), FRAMES_PER_SECOND);
        try (GameSession session = new GameSession(loop, new CliInput(terminal, bindings), render, commands)) {
            session.start();
            return outcomes.take();
        }
    }

    private static void fail(String message) {
        LOG.error(message);
        System.err.println("Nangor Blast: " + message);
    }
}
