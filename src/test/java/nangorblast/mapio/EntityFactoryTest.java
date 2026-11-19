package nangorblast.mapio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import nangorblast.core.EntityKind;
import nangorblast.core.InvalidMapException;
import nangorblast.core.Position;
import nangorblast.model.Entity;
import nangorblast.model.Exit;
import nangorblast.model.GameWorld;
import nangorblast.model.Player;
import nangorblast.model.block.DestructibleBlock;
import nangorblast.model.block.IndestructibleBlock;
import nangorblast.model.block.ReinforcedBlock;
import nangorblast.model.enemy.NormalEnemy;
import nangorblast.model.enemy.PatrolBehavior;
import nangorblast.model.map.FloorTile;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class EntityFactoryTest {

    private final Position at = new Position(3, 2);
    private final EntityFactory factory = MapFixtures.factory(4.0);

    @Test
    void createsProductionBlocksPlayerAndLockedExitAtTheRequestedTile() {
        Player player = assertInstanceOf(Player.class, factory.create('P', at));
        IndestructibleBlock wall = assertInstanceOf(IndestructibleBlock.class, factory.create('#', at));
        DestructibleBlock block = assertInstanceOf(DestructibleBlock.class, factory.create('D', at));
        ReinforcedBlock reinforced = assertInstanceOf(ReinforcedBlock.class, factory.create('R', at));
        Exit exit = assertInstanceOf(Exit.class, factory.create('X', at));
        for (Entity entity : List.of(player, wall, block, reinforced, exit)) {
            assertEquals(at, entity.position());
            assertTrue(entity.isActive());
        }
        assertEquals(at, player.spawn());
        assertEquals(0, player.playerIndex());
        assertEquals(4.0, player.baseSpeed());
        assertEquals(3, player.lives());
        assertEquals(1, player.bombCapacity());
        assertEquals(1, player.blastRange());
        assertFalse(player.isRemoteUnlocked());
        assertFalse(exit.isOpen());
    }

    @Test
    void floorCreatesARealWalkableTileWithoutCallingDomainFactories() {
        AtomicInteger calls = new AtomicInteger();
        Function<Position, Entity> creator = position -> {
            calls.incrementAndGet();
            throw new AssertionError("Floor must not call domain creators");
        };
        EntityFactory floorFactory = new EntityFactory(4, creator, creator, creator);
        FloorTile floor = assertInstanceOf(FloorTile.class, floorFactory.create('.', at));
        assertEquals(EntityKind.FLOOR, floor.kind());
        assertEquals(at, floor.position());
        assertTrue(floor.isWalkable());
        assertFalse(floor.blocksMovementOf(factory.createPlayer(at)));
        assertEquals(0, calls.get());
    }

    @Test
    void delegatesEnemyBossAndLavaToConfiguredConstructors() {
        PatrolBehavior behavior = new PatrolBehavior(List.of(at, new Position(4, 2)));
        NormalEnemy enemy = new NormalEnemy(at, behavior);
        Entity boss = new MapFixtures.TestHostile(at, EntityKind.BOSS);
        Entity lava = new MapFixtures.TestLava(at);
        EntityFactory configured = new EntityFactory(6.5,
                position -> { assertEquals(at, position); return enemy; },
                position -> { assertEquals(at, position); return boss; },
                position -> { assertEquals(at, position); return lava; });

        assertSame(enemy, configured.create('E', at));
        assertSame(behavior, enemy.getBehavior());
        assertSame(boss, configured.create('B', at));
        assertSame(lava, configured.create('H', at));
        assertEquals(6.5, configured.createPlayer(at).speed());
    }

    @ParameterizedTest
    @ValueSource(chars = {'?', ' ', 'W', 'S', 'G', 'g', 'I', 'L', '*', '!'})
    void rejectsUnknownSymbolsAndOtherMapTypes(char symbol) {
        InvalidMapException error = assertThrows(InvalidMapException.class, () -> factory.create(symbol, at));
        assertTrue(error.getMessage().contains("Unknown map character"));
        assertTrue(error.getMessage().contains(at.toString()));
    }

    @ParameterizedTest
    @ValueSource(chars = {'E', 'B', 'H'})
    void rejectsAConstructorReturningTheWrongKind(char symbol) {
        EntityFactory wrong = new EntityFactory(4, Exit::new, Exit::new, Exit::new);
        InvalidMapException error = assertThrows(InvalidMapException.class, () -> wrong.create(symbol, at));
        assertTrue(error.getMessage().contains("Factory for '" + symbol + "'"));
        assertTrue(error.getMessage().contains("must return"));
    }

    @ParameterizedTest
    @ValueSource(chars = {'E', 'B'})
    void enemiesAndBossesMustImplementTheSharedHostileContract(char symbol) {
        EntityFactory wrong = new EntityFactory(4,
                position -> new MapFixtures.TestEntity(position, EntityKind.NORMAL_ENEMY),
                position -> new MapFixtures.TestEntity(position, EntityKind.BOSS),
                position -> new MapFixtures.TestEntity(position, EntityKind.LAVA_DORMANT));
        InvalidMapException error = assertThrows(InvalidMapException.class, () -> wrong.create(symbol, at));
        assertTrue(error.getMessage().contains("Hostile"));
    }

    @Test
    void rejectsMissingInactiveOrMisplacedEntities() {
        assertTrue(assertThrows(InvalidMapException.class,
                () -> withEnemy(position -> null).create('E', at)).getMessage().contains("active entity"));

        NormalEnemy inactive = MapFixtures.enemy(at);
        inactive.deactivate();
        assertTrue(assertThrows(InvalidMapException.class,
                () -> withEnemy(position -> inactive).create('E', at)).getMessage().contains("active entity"));

        assertTrue(assertThrows(InvalidMapException.class,
                () -> withEnemy(position -> MapFixtures.enemy(new Position(0, 0))).create('E', at))
                .getMessage().contains("returned an entity at"));
    }

    @Test
    void failedDomainConstructorKeepsItsCauseAndTileContext() {
        IllegalArgumentException cause = new IllegalArgumentException("Missing patrol path");
        EntityFactory failing = withEnemy(position -> { throw cause; });
        InvalidMapException error = assertThrows(InvalidMapException.class, () -> failing.create('E', at));
        assertSame(cause, error.getCause());
        assertTrue(error.getMessage().contains("Missing patrol path"));
        assertTrue(error.getMessage().contains(at.toString()));
    }

    @Test
    void lavaMustImplementTheTerrainContract() {
        EntityFactory wrong = new EntityFactory(4, MapFixtures::enemy,
                position -> new MapFixtures.TestHostile(position, EntityKind.BOSS),
                position -> new MapFixtures.TestEntity(position, EntityKind.LAVA_DORMANT));
        assertTrue(assertThrows(InvalidMapException.class, () -> wrong.create('H', at))
                .getMessage().contains("must return a Tile"));
    }

    @Test
    void populatePlacesTerrainAndOccupantsWithoutDuplicatingThePlayer() {
        GameWorld world = freshWorld(4, new Position(0, 0));
        factory.populate(world, List.of("P.HX"));
        assertEquals(3, world.entities().size());
        assertEquals(1, world.entities(Player.class).size());
        assertInstanceOf(FloorTile.class, world.map().tileAt(new Position(1, 0)));
        assertTrue(world.entitiesAt(new Position(1, 0)).isEmpty(), "floor is terrain, not an occupant");
        assertSame(world.entitiesAt(new Position(2, 0)).get(0), world.map().lavaTiles().get(0));
        assertEquals(new Position(3, 0), world.entities(Exit.class).get(0).position());
        assertThrows(InvalidMapException.class, () -> factory.populate(world, List.of("P.HX")));
    }

    @Test
    void populateRejectsInvalidLayoutsDimensionsAndPlayerPositionsBeforeConstruction() {
        AtomicInteger calls = new AtomicInteger();
        EntityFactory counting = withEnemy(position -> { calls.incrementAndGet(); return MapFixtures.enemy(position); });
        assertThrows(InvalidMapException.class, () -> counting.populate(freshWorld(3, new Position(0, 0)), List.of("PE?")));
        assertThrows(InvalidMapException.class, () -> counting.populate(freshWorld(2, new Position(0, 0)), List.of("PE.")));
        assertThrows(InvalidMapException.class, () -> counting.populate(freshWorld(3, new Position(1, 0)), List.of("PE.")));
        assertEquals(0, calls.get());
    }

    @Test
    void aFailedPopulationLeavesTerrainAndEntityCollectionsUntouched() {
        GameWorld world = freshWorld(4, new Position(0, 0));
        Entity originalTerrain = world.map().tileAt(new Position(1, 0));
        EntityFactory failing = new EntityFactory(4, MapFixtures::enemy,
                position -> { throw new IllegalArgumentException("Boss is unavailable"); }, MapFixtures.TestLava::new);
        assertThrows(InvalidMapException.class, () -> failing.populate(world, List.of("PHXB")));
        assertEquals(List.of(world.player()), world.entities());
        assertSame(originalTerrain, world.map().tileAt(new Position(1, 0)));
        assertTrue(world.map().lavaTiles().isEmpty());
    }

    @ParameterizedTest
    @ValueSource(doubles = {0, -1, Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY})
    void playerSpeedMustBeFiniteAndPositive(double speed) {
        assertTrue(assertThrows(IllegalArgumentException.class, () -> MapFixtures.factory(speed))
                .getMessage().contains("playerSpeed"));
    }

    private static EntityFactory withEnemy(Function<Position, ? extends Entity> creator) {
        return new EntityFactory(4, creator,
                position -> new MapFixtures.TestHostile(position, EntityKind.BOSS),
                MapFixtures.TestLava::new);
    }

    private GameWorld freshWorld(int width, Position spawn) {
        return new GameWorld("Population", width, 1, factory.createPlayer(spawn), 300, new Random(1));
    }
}
