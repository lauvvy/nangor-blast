package nangorblast.input;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.Optional;
import java.util.Properties;
import nangorblast.core.ConfigLoadException;
import org.junit.jupiter.api.Test;

class KeyBindingsTest {

    private static Properties allActionsBound() {
        Properties p = new Properties();
        char key = 'a';
        for (Action action : Action.values()) {
            if (action != Action.UNBOUND) {
                p.setProperty("p1." + action, String.valueOf(key++));
            }
        }
        return p;
    }

    @Test
    void projectFileLoads() {
        KeyBindings bindings = KeyBindings.load(Path.of("config", "keybindings.properties"));

        assertEquals(Optional.of(new Command(0, Action.MOVE_UP)), bindings.commandFor("w"));
        assertEquals(Optional.of(new Command(0, Action.MOVE_UP)), bindings.commandFor("UP"));
        assertEquals(Optional.of(new Command(0, Action.PLACE_BOMB)), bindings.commandFor("SPACE"));
    }

    @Test
    void unboundCannotBeBound() {
        Properties p = allActionsBound();
        p.setProperty("p1.UNBOUND", "z");
        assertThrows(ConfigLoadException.class, () -> KeyBindings.parse(p));
    }

    @Test
    void labelShowsTheFirstKeyAsAPlayerReadsIt() {
        KeyBindings bindings = KeyBindings.load(Path.of("config", "keybindings.properties"));
        assertEquals("P", bindings.label(Action.PAUSE));
        assertEquals("Enter", bindings.label(Action.CONFIRM));
        assertEquals("1", bindings.label(Action.CHOICE_1));
        assertEquals("?", bindings.label(Action.UNBOUND));
    }

    @Test
    void keysForListsKeysInFileOrder() {
        KeyBindings bindings = KeyBindings.load(Path.of("config", "keybindings.properties"));

        assertEquals(java.util.List.of("p", "ESCAPE"), bindings.keysFor(new Command(0, Action.PAUSE)));
        assertEquals(java.util.List.of(), bindings.keysFor(new Command(1, Action.PAUSE)));
    }

    @Test
    void lettersIgnoreCaseAndNamedKeysAreNormalised() {
        Properties p = allActionsBound();
        p.setProperty("p1.MOVE_UP", "W, up");

        KeyBindings bindings = KeyBindings.parse(p);

        assertEquals(Optional.of(new Command(0, Action.MOVE_UP)), bindings.commandFor("w"));
        assertEquals(Optional.of(new Command(0, Action.MOVE_UP)), bindings.commandFor("UP"));
        assertTrue(bindings.commandFor("z").isEmpty());
    }

    @Test
    void secondPlayerGetsIndexOne() {
        Properties p = allActionsBound();
        p.setProperty("p2.MOVE_UP", "UP");

        assertEquals(Optional.of(new Command(1, Action.MOVE_UP)), KeyBindings.parse(p).commandFor("UP"));
    }

    @Test
    void keyBoundTwiceIsRefused() {
        Properties p = allActionsBound();
        p.setProperty("p2.MOVE_UP", "a"); // already p1's first action

        var error = assertThrows(ConfigLoadException.class, () -> KeyBindings.parse(p));
        assertTrue(error.getMessage().contains("'a' is bound twice"), error.getMessage());
    }

    @Test
    void unknownActionKeyOrNameIsRefused() {
        Properties badAction = allActionsBound();
        badAction.setProperty("p1.JUMP", "j");
        assertThrows(ConfigLoadException.class, () -> KeyBindings.parse(badAction));

        Properties badKey = allActionsBound();
        badKey.setProperty("p1.MOVE_UP", "F13");
        var error = assertThrows(ConfigLoadException.class, () -> KeyBindings.parse(badKey));
        assertTrue(error.getMessage().contains("p1.MOVE_UP"), error.getMessage());

        Properties badName = allActionsBound();
        badName.setProperty("player1.MOVE_UP", "j");
        assertThrows(ConfigLoadException.class, () -> KeyBindings.parse(badName));
    }

    @Test
    void everyFirstPlayerActionNeedsAKey() {
        Properties p = allActionsBound();
        p.remove("p1.PAUSE");

        var error = assertThrows(ConfigLoadException.class, () -> KeyBindings.parse(p));
        assertEquals("p1.PAUSE has no key", error.getMessage());
    }

    @Test
    void missingFileIsAConfigError() {
        assertThrows(ConfigLoadException.class, () -> KeyBindings.load(Path.of("no-such-file.properties")));
    }
}
