package nangorblast.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.atomic.AtomicInteger;
import nangorblast.TestWorlds;
import nangorblast.core.GameState;
import nangorblast.input.Action;
import nangorblast.input.Command;
import nangorblast.model.GameWorld;
import org.junit.jupiter.api.Test;

class GameFlowTest {

    private final AtomicInteger quits = new AtomicInteger();
    private final GameFlow flow = new GameFlow(quits::incrementAndGet);
    private final GameWorld world = TestWorlds.fromAscii("P..");

    private GameState after(GameState from, Action action) {
        world.setState(from);
        flow.handle(world, new Command(0, action));
        return world.state();
    }

    @Test
    void menuStartsOnConfirmAndIgnoresOtherActions() {
        assertEquals(GameState.PLAYING, after(GameState.MENU, Action.CONFIRM));
        assertEquals(GameState.MENU, after(GameState.MENU, Action.MOVE_UP));
        assertEquals(GameState.MENU, after(GameState.MENU, Action.PAUSE));
        assertEquals(0, quits.get());
    }

    @Test
    void pauseTogglesBetweenPlayingAndPaused() {
        assertEquals(GameState.PAUSED, after(GameState.PLAYING, Action.PAUSE));
        assertEquals(GameState.PLAYING, after(GameState.PAUSED, Action.PAUSE));
        assertEquals(GameState.PLAYING, after(GameState.PAUSED, Action.CONFIRM));
        assertEquals(GameState.PLAYING, after(GameState.PLAYING, Action.BACK), "no quitting mid-game by accident");
    }

    @Test
    void quitsFromMenuPauseAndTheEndScreens() {
        after(GameState.MENU, Action.BACK);
        after(GameState.PAUSED, Action.BACK);
        after(GameState.LOSE, Action.CONFIRM);
        after(GameState.WIN, Action.BACK);
        assertEquals(4, quits.get());
        assertEquals(GameState.LOSE, after(GameState.LOSE, Action.MOVE_LEFT));
    }
}
