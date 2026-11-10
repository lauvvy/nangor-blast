package nangorblast.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import nangorblast.TestWorlds;
import nangorblast.core.Position;
import org.junit.jupiter.api.Test;

class GameWorldTest {

    private final GameWorld world = TestWorlds.fromAscii(
            "#####",
            "#P.X#",
            "#####");

    @Test
    void typedQueriesReturnOnlyActiveEntitiesOfThatType() {
        assertEquals(1, world.entities(Player.class).size());
        assertEquals(1, world.entities(Exit.class).size());

        world.entities(Exit.class).get(0).deactivate();
        assertTrue(world.entities(Exit.class).isEmpty());
    }

    @Test
    void queriesAreSafeToIterateWhileAdding() {
        for (Exit exit : world.entities(Exit.class)) {
            world.add(new Exit(exit.position()));
        }
        assertEquals(2, world.entities(Exit.class).size());
    }

    @Test
    void blockingFollowsEntitiesAndMapEdge() {
        Player player = world.player();
        assertTrue(world.isBlockedFor(player, new Position(0, 1)), "wall");
        assertFalse(world.isBlockedFor(player, new Position(2, 1)), "floor");
        assertFalse(world.isBlockedFor(player, new Position(3, 1)), "exit is walkable");
        assertTrue(world.isBlockedFor(player, new Position(-1, 1)), "outside the map");
        assertFalse(world.isBlockedFor(player, player.position()), "a mover never blocks itself");
    }

    @Test
    void removeInactiveDropsDeactivatedEntities() {
        world.entities(Exit.class).get(0).deactivate();
        int before = world.entities().size();
        world.removeInactive();
        assertEquals(before, world.entities().size());
        assertTrue(world.entitiesAt(new Position(3, 1)).isEmpty());
    }

    @Test
    void warningTilesAreTheUnionOfAllSources() {
        Object boss = new Object();
        Object lava = new Object();
        world.setWarnings(boss, Set.of(new Position(1, 1), new Position(2, 1)));
        world.setWarnings(lava, Set.of(new Position(2, 1), new Position(3, 1)));
        assertEquals(3, world.warningTiles().size());

        world.clearWarnings(boss);
        assertEquals(Set.of(new Position(2, 1), new Position(3, 1)), world.warningTiles());
    }

    @Test
    void timerStopsAtZeroAndScoreAccumulates() {
        world.reduceTime(1000);
        assertEquals(0, world.timeLeft());

        world.addScore(100);
        world.addScore(500);
        assertEquals(600, world.score());
    }

    @Test
    void refusesEntityOutsideTheMap() {
        assertThrows(IllegalArgumentException.class, () -> world.add(new Exit(new Position(9, 9))));
    }
}
