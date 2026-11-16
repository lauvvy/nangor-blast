package nangorblast.engine;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import nangorblast.core.GameState;
import nangorblast.input.Action;
import nangorblast.input.Command;
import nangorblast.model.GameWorld;
import nangorblast.view.WorldSnapshot.Panel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The menus and the state changes the player drives (F09.1, F09.2, F09.5): Main Menu, How
 * to Play, Pause, and Game Over, kept as a stack. The top screen gets the input and is
 * drawn through {@link #panel()}; back returns to the screen below. While playing the stack
 * is empty and only the pause action is read here.
 *
 * <p>A screen ends the session through {@code onExit}: restart and return to the menu build
 * a new world in {@code app/Main}, so every initial value comes back (F00.4).
 *
 * <p>Menu keys: move up and down to select, confirm to choose, a number to choose that item,
 * back to return. Any other key shows an error on the message line and the menu stays open.
 * A discrete action repeated within {@link #REPEAT_GUARD_SECONDS} is ignored, so a held key
 * toggles pause once; the window is longer than the usual 0.5 s key-repeat delay of the
 * operating system. Time is counted with {@code dt} in {@link #afterTick}, never a clock.
 */
public final class GameFlow implements CommandHandler {

    private static final Logger LOG = LoggerFactory.getLogger(GameFlow.class);

    /** How a screen ends the session. */
    public enum Outcome { QUIT, RESTART, MENU }

    public static final double REPEAT_GUARD_SECONDS = 0.6;

    private static final Set<Action> DISCRETE = EnumSet.of(Action.PAUSE, Action.CONFIRM, Action.BACK,
            Action.CHOICE_1, Action.CHOICE_2, Action.CHOICE_3);
    private static final List<Action> CHOICES = List.of(Action.CHOICE_1, Action.CHOICE_2, Action.CHOICE_3);

    private record Item(String label, Consumer<GameWorld> choose) {
    }

    /** {@code onBack} is null when back has nothing to return to. */
    private record Screen(String title, List<String> lines, List<Item> items, Consumer<GameWorld> onBack,
                          boolean pauseResumes) {
    }

    private final Function<Action, String> key;
    private final Consumer<Outcome> onExit;

    // Game-loop thread only.
    private final Deque<Screen> stack = new ArrayDeque<>();
    private int selected;
    private String message = "";
    private double time;
    private final Map<Action, Double> lastSeen = new EnumMap<>(Action.class);
    private boolean exited;

    /**
     * @param key the key a player presses for an action, for help text ({@code KeyBindings::label})
     * @param onExit called on the game-loop thread when a screen ends the session
     */
    public GameFlow(Function<Action, String> key, Consumer<Outcome> onExit) {
        this.key = Objects.requireNonNull(key, "key");
        this.onExit = Objects.requireNonNull(onExit, "onExit");
    }

    @Override
    public void handle(GameWorld world, Command command) {
        openScreenFor(world);
        Action action = command.action();
        if (exited || repeated(action)) {
            return;
        }
        if (stack.isEmpty()) {
            if (world.state() == GameState.PLAYING && action == Action.PAUSE) {
                world.setState(GameState.PAUSED);
                LOG.info("Paused");
                push(pauseMenu());
            }
            return;
        }
        Screen top = stack.peek();
        int count = top.items().size();
        switch (action) {
            case MOVE_UP -> select((selected - 1 + count) % count);
            case MOVE_DOWN -> select((selected + 1) % count);
            case CONFIRM -> choose(world, selected);
            case CHOICE_1, CHOICE_2, CHOICE_3 -> {
                int index = CHOICES.indexOf(action);
                if (index < count) {
                    choose(world, index);
                } else {
                    invalid();
                }
            }
            case BACK -> {
                if (top.onBack() != null) {
                    top.onBack().accept(world);
                } else {
                    invalid();
                }
            }
            case PAUSE -> {
                if (top.pauseResumes()) {
                    resume(world);
                } else {
                    invalid();
                }
            }
            default -> invalid();
        }
    }

    @Override
    public void afterTick(GameWorld world, double dt) {
        time += dt;
        openScreenFor(world);
    }

    /** The open menu for the snapshot, or null while playing. Game-loop thread only. */
    public Panel panel() {
        Screen top = stack.peek();
        if (top == null) {
            return null;
        }
        return new Panel(top.title(), top.lines(), top.items().stream().map(Item::label).toList(), selected, message);
    }

    /** Opens the Main Menu in MENU and the Game Over screen once WIN or LOSE is reached. */
    private void openScreenFor(GameWorld world) {
        if (!stack.isEmpty() || exited) {
            return;
        }
        switch (world.state()) {
            case MENU -> push(mainMenu());
            case WIN, LOSE -> push(gameOver(world));
            case PLAYING, PAUSED -> { }
        }
    }

    /** True for a discrete action seen again within the guard window. Every press restarts the window. */
    private boolean repeated(Action action) {
        if (!DISCRETE.contains(action)) {
            return false;
        }
        Double last = lastSeen.put(action, time);
        return last != null && time - last < REPEAT_GUARD_SECONDS;
    }

    private void select(int index) {
        selected = index;
        message = "";
    }

    private void choose(GameWorld world, int index) {
        select(index);
        stack.peek().items().get(index).choose().accept(world);
    }

    private void invalid() {
        int count = stack.peek().items().size();
        message = "Invalid key. Use " + key.apply(Action.MOVE_UP) + "/" + key.apply(Action.MOVE_DOWN)
                + " and " + key.apply(Action.CONFIRM) + (count > 1 ? ", or 1-" + count : "");
    }

    private void push(Screen screen) {
        stack.push(screen);
        selected = 0;
        message = "";
    }

    private void pop(GameWorld world) {
        stack.pop();
        selected = 0;
        message = "";
    }

    private void resume(GameWorld world) {
        stack.clear();
        world.setState(GameState.PLAYING);
        LOG.info("Resumed");
    }

    private void exit(Outcome outcome) {
        exited = true;
        LOG.info("Session ends: {}", outcome);
        onExit.accept(outcome);
    }

    // --- Screens ---

    private Screen mainMenu() {
        return new Screen("NANGOR BLAST", List.of("Red Mountain"), List.of(
                new Item("Start Game", this::resume),
                new Item("How to Play", w -> push(howToPlay())),
                new Item("Exit", w -> exit(Outcome.QUIT))), null, false);
    }

    private Screen howToPlay() {
        return new Screen("How to Play", List.of(
                "Beat 5 enemies and Hades, then reach X.",
                row("Move", key.apply(Action.MOVE_UP) + " " + key.apply(Action.MOVE_LEFT) + " "
                        + key.apply(Action.MOVE_DOWN) + " " + key.apply(Action.MOVE_RIGHT)),
                row("Bomb", key.apply(Action.PLACE_BOMB)),
                row("Bomb type", key.apply(Action.SELECT_NORMAL) + " " + key.apply(Action.SELECT_PUMPED) + " "
                        + key.apply(Action.SELECT_REMOTE) + "  Normal Pumped Remote"),
                row("Detonate", key.apply(Action.DETONATE) + "  Remote Bomb"),
                row("Pause", key.apply(Action.PAUSE)),
                "Blasts, enemies, lava, and Hades hurt."),
                List.of(new Item("Back", this::pop)), this::pop, false);
    }

    private static String row(String label, String keys) {
        return String.format("%-10s %s", label, keys);
    }

    private Screen pauseMenu() {
        return new Screen("Paused", List.of(), List.of(
                new Item("Resume", this::resume),
                new Item("Restart", w -> exit(Outcome.RESTART)),
                new Item("Main Menu", w -> exit(Outcome.MENU))), this::resume, true);
    }

    private Screen gameOver(GameWorld world) {
        String title = world.state() == GameState.WIN ? "You win!" : "Game over";
        return new Screen(title, List.of("Final score  " + world.score()), List.of(
                new Item("Play Again", w -> exit(Outcome.RESTART)),
                new Item("Main Menu", w -> exit(Outcome.MENU)),
                new Item("Exit", w -> exit(Outcome.QUIT))), null, false);
    }
}
