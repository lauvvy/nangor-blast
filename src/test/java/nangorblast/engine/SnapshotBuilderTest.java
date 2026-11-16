package nangorblast.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Set;
import nangorblast.TestWorlds;
import nangorblast.core.BombType;
import nangorblast.core.EntityKind;
import nangorblast.core.GameState;
import nangorblast.core.Position;
import nangorblast.model.GameWorld;
import nangorblast.model.Player;
import nangorblast.view.WorldSnapshot;
import org.junit.jupiter.api.Test;

class SnapshotBuilderTest {

    @Test
    void copiesEntitiesHudAndWarnings() {
        GameWorld world = TestWorlds.fromAscii("P.X");
        world.addScore(100);
        world.reduceTime(0.4);
        world.setWarnings(this, Set.of(new Position(1, 0)));

        WorldSnapshot snapshot = SnapshotBuilder.build(world);

        assertEquals(3, snapshot.width());
        assertEquals(2, snapshot.sprites().size());
        assertEquals(EntityKind.EXIT_LOCKED, snapshot.sprites().get(1).kind());
        assertEquals(new WorldSnapshot.Hud(Player.START_LIVES, 100, 300, BombType.NORMAL, false), snapshot.hud());
        assertEquals(Set.of(new Position(1, 0)), snapshot.warnings());
        assertEquals(GameState.PLAYING, snapshot.state());
    }

    @Test
    void snapshotDoesNotChangeWithTheWorld() {
        GameWorld world = TestWorlds.fromAscii("P.X");
        WorldSnapshot snapshot = SnapshotBuilder.build(world);

        world.player().moveTo(new Position(1, 0));

        assertEquals(new Position(0, 0), snapshot.sprites().get(0).position());
        assertThrows(UnsupportedOperationException.class, () -> snapshot.sprites().clear());
    }
}
