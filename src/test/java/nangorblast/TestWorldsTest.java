package nangorblast;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import nangorblast.core.InvalidMapException;
import nangorblast.core.Position;
import nangorblast.model.Exit;
import nangorblast.model.GameWorld;
import org.junit.jupiter.api.Test;

class TestWorldsTest {

    @Test
    void buildsWorldFromRows() {
        GameWorld world = TestWorlds.fromAscii(
                "#####",
                "#P.X#",
                "#####");

        assertEquals(5, world.width());
        assertEquals(3, world.height());
        assertEquals(new Position(1, 1), world.player().position());
        assertEquals(12, world.entities(TestWorlds.Wall.class).size());
        assertTrue(world.firstAt(new Position(3, 1), Exit.class).isPresent());
    }

    @Test
    void legendAddsCharacters() {
        GameWorld world = TestWorlds.fromAscii(Map.of('W', TestWorlds.Wall::new), "PW");

        assertTrue(world.isBlockedFor(world.player(), new Position(1, 0)));
    }

    @Test
    void refusesUnknownCharacter() {
        var error = assertThrows(InvalidMapException.class, () -> TestWorlds.fromAscii("PZ"));
        assertTrue(error.getMessage().contains("'Z'"));
    }

    @Test
    void refusesUnevenRows() {
        assertThrows(InvalidMapException.class, () -> TestWorlds.fromAscii("P..", ".."));
    }

    @Test
    void needsExactlyOnePlayer() {
        assertThrows(InvalidMapException.class, () -> TestWorlds.fromAscii("..."));
        assertThrows(InvalidMapException.class, () -> TestWorlds.fromAscii("P.P"));
    }
}
