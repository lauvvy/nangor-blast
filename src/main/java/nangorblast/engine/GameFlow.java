package nangorblast.engine;

import java.util.Objects;
import nangorblast.core.GameState;
import nangorblast.input.Action;
import nangorblast.input.Command;
import nangorblast.model.GameWorld;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * State changes driven by the player (F09.2, F09.5): start from the menu, pause and resume,
 * and quit. A handler, so it runs while paused. MVP-0 version: C-07 replaces quitting from
 * pause and from the end screens with the full menus (resume, restart, return to menu).
 *
 * <ul>
 *   <li>MENU: CONFIRM starts the game, BACK quits.
 *   <li>PLAYING: PAUSE pauses.
 *   <li>PAUSED: PAUSE or CONFIRM resumes, BACK quits.
 *   <li>WIN, LOSE: CONFIRM or BACK quits.
 * </ul>
 */
public final class GameFlow implements CommandHandler {

    private static final Logger LOG = LoggerFactory.getLogger(GameFlow.class);

    private final Runnable onQuit;

    /** @param onQuit called on the game-loop thread when the player quits */
    public GameFlow(Runnable onQuit) {
        this.onQuit = Objects.requireNonNull(onQuit, "onQuit");
    }

    @Override
    public void handle(GameWorld world, Command command) {
        GameState state = world.state();
        GameState next = switch (state) {
            case MENU -> switch (command.action()) {
                case CONFIRM -> GameState.PLAYING;
                case BACK -> quit(state);
                default -> state;
            };
            case PLAYING -> command.action() == Action.PAUSE ? GameState.PAUSED : state;
            case PAUSED -> switch (command.action()) {
                case PAUSE, CONFIRM -> GameState.PLAYING;
                case BACK -> quit(state);
                default -> state;
            };
            case WIN, LOSE -> switch (command.action()) {
                case CONFIRM, BACK -> quit(state);
                default -> state;
            };
        };
        if (next != state) {
            LOG.info("State {} -> {}", state, next);
            world.setState(next);
        }
    }

    private GameState quit(GameState state) {
        LOG.info("Player quit from {}", state);
        onQuit.run();
        return state;
    }
}
