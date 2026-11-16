package nangorblast.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import nangorblast.TestWorlds;
import nangorblast.core.GameState;
import nangorblast.engine.GameFlow.Outcome;
import nangorblast.input.Action;
import nangorblast.input.Command;
import nangorblast.model.GameWorld;
import nangorblast.view.WorldSnapshot.Panel;
import org.junit.jupiter.api.Test;

class GameFlowTest {

    private static final double DT = 0.05;

    private final List<Outcome> outcomes = new ArrayList<>();
    private final GameFlow flow = new GameFlow(Action::name, outcomes::add);
    private final GameWorld world = TestWorlds.fromAscii("P..");

    private void press(Action action) {
        flow.handle(world, new Command(0, action));
    }

    /** Lets game time pass, as the loop does at the end of every tick. */
    private void pass(double seconds) {
        for (int i = 0; i < Math.round(seconds / DT); i++) {
            flow.afterTick(world, DT);
        }
    }

    private GameFlow inMenu() {
        world.setState(GameState.MENU);
        flow.afterTick(world, DT);
        return flow;
    }

    @Test
    void mainMenuOpensWithStartSelected() {
        Panel panel = inMenu().panel();
        assertEquals("NANGOR BLAST", panel.title());
        assertEquals(List.of("Start Game", "How to Play", "Exit"), panel.items());
        assertEquals(0, panel.selected());
    }

    @Test
    void confirmOnStartPlaysAndClosesTheMenu() {
        inMenu();
        press(Action.CONFIRM);
        assertEquals(GameState.PLAYING, world.state());
        assertNull(flow.panel());
    }

    @Test
    void upAndDownMoveTheSelectionAndWrap() {
        inMenu();
        press(Action.MOVE_DOWN);
        assertEquals(1, flow.panel().selected());
        press(Action.MOVE_UP);
        press(Action.MOVE_UP);
        assertEquals(2, flow.panel().selected());
    }

    @Test
    void howToPlayIsPushedAndBackReturnsToTheMenu() {
        inMenu();
        press(Action.CHOICE_2);
        Panel help = flow.panel();
        assertEquals("How to Play", help.title());
        assertTrue(help.lines().stream().anyMatch(l -> l.contains("MOVE_UP MOVE_LEFT MOVE_DOWN MOVE_RIGHT")),
                "controls come from the key bindings: " + help.lines());

        press(Action.BACK);
        assertEquals("NANGOR BLAST", flow.panel().title());
    }

    @Test
    void invalidInputShowsAnErrorAndTheMenuStaysOpen() {
        inMenu();
        press(Action.PLACE_BOMB);
        assertEquals("Invalid key. Use MOVE_UP/MOVE_DOWN and CONFIRM, or 1-3", flow.panel().message());
        assertEquals(GameState.MENU, world.state());

        press(Action.MOVE_DOWN);
        assertEquals("", flow.panel().message(), "a valid key clears the error");

        press(Action.UNBOUND);
        assertTrue(flow.panel().message().startsWith("Invalid key"), "a key with no binding is invalid too");
        pass(1);
        press(Action.BACK);
        assertTrue(flow.panel().message().startsWith("Invalid key"), "back on the main menu has nowhere to go");
    }

    @Test
    void pauseOffersResumeRestartAndMenu() {
        press(Action.PAUSE);
        assertEquals(GameState.PAUSED, world.state());
        assertEquals("Paused", flow.panel().title());
        assertEquals(List.of("Resume", "Restart", "Main Menu"), flow.panel().items());

        press(Action.CONFIRM);
        assertEquals(GameState.PLAYING, world.state());
        assertNull(flow.panel());
    }

    @Test
    void holdingThePauseKeyTogglesOnce() {
        for (int i = 0; i < 40; i++) { // a key repeat every 0.05 s for 2 seconds
            press(Action.PAUSE);
            pass(DT);
        }
        assertEquals(GameState.PAUSED, world.state());

        pass(GameFlow.REPEAT_GUARD_SECONDS);
        press(Action.PAUSE); // released, then pressed again
        assertEquals(GameState.PLAYING, world.state());
    }

    @Test
    void restartAndMainMenuEndTheSession() {
        press(Action.PAUSE);
        press(Action.CHOICE_2);
        press(Action.CHOICE_3); // ignored: the session already ended
        assertEquals(List.of(Outcome.RESTART), outcomes);

        GameFlow other = new GameFlow(Action::name, outcomes::add);
        other.handle(world, new Command(0, Action.MOVE_LEFT));
        world.setState(GameState.PLAYING);
        other.handle(world, new Command(0, Action.PAUSE));
        other.handle(world, new Command(0, Action.CHOICE_3));
        assertEquals(List.of(Outcome.RESTART, Outcome.MENU), outcomes);
    }

    @Test
    void gameOverShowsTheFinalScore() {
        world.addScore(450);
        world.setState(GameState.LOSE);
        flow.afterTick(world, DT);
        Panel panel = flow.panel();
        assertEquals("Game over", panel.title());
        assertEquals(List.of("Final score  450"), panel.lines());
        assertEquals(List.of("Play Again", "Main Menu", "Exit"), panel.items());

        press(Action.CHOICE_3);
        assertEquals(List.of(Outcome.QUIT), outcomes);
    }

    @Test
    void winHasItsOwnTitle() {
        world.setState(GameState.WIN);
        flow.afterTick(world, DT);
        assertEquals("You win!", flow.panel().title());
    }

    @Test
    void movesWhilePlayingAreLeftToPlayerSystem() {
        press(Action.MOVE_LEFT);
        press(Action.CONFIRM);
        assertEquals(GameState.PLAYING, world.state());
        assertNull(flow.panel());
    }
}
