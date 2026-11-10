package nangorblast.view.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;
import nangorblast.core.BombType;
import nangorblast.core.EntityKind;
import nangorblast.core.GameState;
import nangorblast.core.Position;
import nangorblast.view.WorldSnapshot;
import nangorblast.view.WorldSnapshot.Hud;
import nangorblast.view.WorldSnapshot.Sprite;
import org.junit.jupiter.api.Test;

class CliFrameTest {

    static WorldSnapshot snapshot(List<Sprite> sprites, Set<Position> warnings, int secondsLeft, GameState state) {
        return new WorldSnapshot(14, 10, sprites, warnings, new Hud(3, 120, secondsLeft, BombType.PUMPED), state);
    }

    private static char tile(CliFrame frame, int x, int y) {
        return frame.cell(CliFrame.MAP_TOP + y, 2 * x).ch();
    }

    @Test
    void drawsTheMapWithDesignGlyphsTwoCharactersPerTile() {
        CliFrame frame = CliFrame.compose(snapshot(List.of(
                new Sprite(1, EntityKind.INDESTRUCTIBLE_BLOCK, new Position(0, 0)),
                new Sprite(2, EntityKind.PLAYER, new Position(1, 1)),
                new Sprite(3, EntityKind.LAVA_ERUPTING, new Position(13, 9))), Set.of(), 300, GameState.PLAYING));

        assertEquals(13, frame.rows());
        assertEquals(48, frame.cols());
        assertEquals("# . . . . . . . . . . . . . ", frame.rowText(CliFrame.MAP_TOP).substring(0, 28));
        assertEquals('P', tile(frame, 1, 1));
        assertEquals(0x4FC3F7, frame.cell(CliFrame.MAP_TOP + 1, 2).fg());
        assertEquals('^', tile(frame, 13, 9));
    }

    @Test
    void highestDrawPriorityWinsOnASharedTile() {
        Position shared = new Position(2, 2);
        CliFrame frame = CliFrame.compose(snapshot(List.of(
                new Sprite(1, EntityKind.LAVA_DORMANT, shared),
                new Sprite(2, EntityKind.EXPLOSION, shared),
                new Sprite(3, EntityKind.PLAYER, shared),
                new Sprite(4, EntityKind.BOMB_NORMAL, new Position(3, 3)),
                new Sprite(5, EntityKind.PLAYER, new Position(3, 3)),
                new Sprite(6, EntityKind.BOSS, new Position(4, 4))), Set.of(new Position(4, 4)), 300, GameState.PLAYING));

        assertEquals('*', tile(frame, 2, 2), "explosion over player and lava");
        assertEquals('P', tile(frame, 3, 3), "player over bomb");
        assertEquals('!', tile(frame, 4, 4), "warning over boss");
    }

    @Test
    void everyEntityKindDrawsAboveTheFloor() {
        for (EntityKind kind : EntityKind.values()) {
            CliFrame.Glyph glyph = CliFrame.glyph(kind);
            assertTrue(glyph.priority() >= 1 && glyph.priority() <= 10, kind + " has a priority above floor");
        }
    }

    @Test
    void hudShowsLivesBombScoreAndTimerTurningRedBelow30Seconds() {
        CliFrame calm = CliFrame.compose(snapshot(List.of(), Set.of(), 30, GameState.PLAYING));
        CliFrame low = CliFrame.compose(snapshot(List.of(), Set.of(), 29, GameState.PLAYING));

        assertEquals("Lives 3  Bomb Pumped  Score 120  Time 30", calm.rowText(0).strip());
        int timeCol = calm.rowText(0).indexOf("30");
        assertEquals(CliFrame.TEXT, calm.cell(0, timeCol).fg());
        assertEquals(CliFrame.ERROR, low.cell(0, timeCol).fg());
    }

    @Test
    void messageLineNamesStatesOtherThanPlaying() {
        int messageRow = CliFrame.MAP_TOP + 10;
        assertEquals("", CliFrame.compose(snapshot(List.of(), Set.of(), 300, GameState.PLAYING)).rowText(messageRow).strip());
        assertEquals("Paused", CliFrame.compose(snapshot(List.of(), Set.of(), 300, GameState.PAUSED)).rowText(messageRow).strip());
        assertEquals("Game over", CliFrame.compose(snapshot(List.of(), Set.of(), 0, GameState.LOSE)).rowText(messageRow).strip());
    }

    @Test
    void ignoresSpritesOffTheMap() {
        CliFrame frame = CliFrame.compose(snapshot(List.of(new Sprite(1, EntityKind.PLAYER, new Position(14, 3))),
                Set.of(), 300, GameState.PLAYING));
        assertEquals('.', tile(frame, 13, 3));
    }
}
