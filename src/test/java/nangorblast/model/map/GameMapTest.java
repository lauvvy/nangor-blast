package nangorblast.model.map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Random;
import nangorblast.core.EntityKind;
import nangorblast.core.Position;
import nangorblast.model.GameWorld;
import nangorblast.model.Player;
import nangorblast.model.block.IndestructibleBlock;
import org.junit.jupiter.api.Test;

class GameMapTest {

    @Test
    void newMapOwnsAWalkableFloorTileAtEveryGridPosition() {
        GameMap map = new GameMap("Terrain", 3, 2);
        assertEquals("Terrain", map.name());
        assertEquals(3, map.width());
        assertEquals(2, map.height());
        for (int row = 0; row < map.height(); row++) {
            for (int col = 0; col < map.width(); col++) {
                Position at = new Position(col, row);
                FloorTile floor = assertInstanceOf(FloorTile.class, map.tileAt(at));
                assertEquals(at, floor.position());
                assertEquals(EntityKind.FLOOR, floor.kind());
                assertTrue(floor.isWalkable());
                assertTrue(map.isWalkable(at));
            }
        }
        assertTrue(map.lavaTiles().isEmpty());
    }

    @Test
    void lavaQueryIncludesEveryStateInRowOrderAndReturnsAnImmutableSnapshot() {
        GameMap map = new GameMap("Lava query", 3, 2);
        Tile dormant = new TestTerrain(new Position(2, 0), EntityKind.LAVA_DORMANT, true);
        Tile warning = new TestTerrain(new Position(0, 1), EntityKind.LAVA_WARNING, true);
        Tile erupting = new TestTerrain(new Position(1, 1), EntityKind.LAVA_ERUPTING, true);
        map.setTile(erupting);
        map.setTile(warning);
        map.setTile(dormant);
        List<Tile> snapshot = map.lavaTiles();
        assertEquals(List.of(dormant, warning, erupting), snapshot);
        assertThrows(UnsupportedOperationException.class, () -> snapshot.add(new FloorTile(new Position(0, 0))));
        map.setTile(new FloorTile(dormant.position()));
        assertEquals(3, snapshot.size());
        assertEquals(List.of(warning, erupting), map.lavaTiles());
    }

    @Test
    void outOfBoundsTerrainIsNotWalkableAndCannotBeReadOrInstalled() {
        GameMap map = new GameMap("Bounds", 2, 1);
        for (Position outside : List.of(new Position(-1, 0), new Position(2, 0), new Position(0, 1))) {
            assertFalse(map.isWalkable(outside));
            assertThrows(IndexOutOfBoundsException.class, () -> map.tileAt(outside));
            assertThrows(IndexOutOfBoundsException.class, () -> map.setTile(new FloorTile(outside)));
        }
    }

    @Test
    void tilesStayAtTheirGridCoordinatesAndInactiveTerrainIsRejected() {
        GameMap map = new GameMap("Fixed tiles", 2, 1);
        Tile floor = map.tileAt(new Position(0, 0));
        floor.moveTo(new Position(0, 0));
        assertThrows(IllegalArgumentException.class, () -> floor.moveTo(new Position(1, 0)));
        assertEquals(new Position(0, 0), floor.position());
        Tile inactive = new FloorTile(new Position(1, 0));
        inactive.deactivate();
        assertThrows(IllegalArgumentException.class, () -> map.setTile(inactive));
    }

    @Test
    void worldChecksBothTerrainAndOccupantsWhileKeepingFloorOutOfOccupancyQueries() {
        GameMap map = new GameMap("Layers", 3, 1);
        Player player = new Player(new Position(0, 0), 0, 4);
        GameWorld world = new GameWorld(map, player, 300, new Random(1));
        assertSame(map, world.map());
        assertEquals("Layers", world.mapName());
        assertEquals(3, world.width());
        assertEquals(1, world.height());
        assertEquals(List.of(player), world.entities());
        Position blocked = new Position(1, 0);
        map.setTile(new TestTerrain(blocked, EntityKind.INDESTRUCTIBLE_BLOCK, false));
        assertTrue(world.entitiesAt(blocked).isEmpty());
        assertTrue(world.isBlockedFor(player, blocked), "unwalkable terrain");
        map.setTile(new FloorTile(blocked));
        world.add(new IndestructibleBlock(blocked));
        assertTrue(map.isWalkable(blocked), "terrain remains walkable beneath a block");
        assertTrue(world.isBlockedFor(player, blocked), "occupying block");
        assertFalse(world.isBlockedFor(player, new Position(2, 0)), "unoccupied floor");
    }

    private static final class TestTerrain extends Tile {
        private final EntityKind kind;
        private final boolean walkable;

        TestTerrain(Position position, EntityKind kind, boolean walkable) {
            super(position);
            this.kind = kind;
            this.walkable = walkable;
        }

        @Override
        public EntityKind kind() {
            return kind;
        }

        @Override
        public boolean isWalkable() {
            return walkable;
        }
    }
}
