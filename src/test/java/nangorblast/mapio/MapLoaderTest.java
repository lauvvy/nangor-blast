package nangorblast.mapio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import nangorblast.core.EntityKind;
import nangorblast.core.GameState;
import nangorblast.core.InvalidMapException;
import nangorblast.core.Position;
import nangorblast.engine.LifeSystem;
import nangorblast.model.Damage;
import nangorblast.model.DamageSource;
import nangorblast.model.Entity;
import nangorblast.model.Exit;
import nangorblast.model.GameWorld;
import nangorblast.model.Player;
import nangorblast.model.block.DestructibleBlock;
import nangorblast.model.enemy.NormalEnemy;
import nangorblast.model.map.FloorTile;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class MapLoaderTest {

    private static final List<String> RED_MOUNTAIN = List.of(
            "##############",
            "#P..D...H...X#",
            "#.##.##.H##E.#",
            "#...H...H....#",
            "###.###.###..#",
            "#DE.H...D....#",
            "#.##.###.##H.#",
            "#...HE...B...#",
            "#E..D....H..E#",
            "##############");

    private final MapLoader loader = new MapLoader(MapFixtures.factory(4.0), 300.0);

    @TempDir
    Path temporary;

    @Test
    void shippedRedMountainFileMatchesTheSpecificationAndLoadsEveryTile() throws IOException {
        Path file = Path.of("config", "maps", "red-mountain.txt");
        assertEquals(RED_MOUNTAIN, Files.readAllLines(file, StandardCharsets.UTF_8));
        Random random = new Random(42);
        GameWorld world = loader.load(file, random);
        Map<Character, EntityKind> legend = Map.of(
                '#', EntityKind.INDESTRUCTIBLE_BLOCK, 'D', EntityKind.DESTRUCTIBLE_BLOCK,
                'P', EntityKind.PLAYER, 'E', EntityKind.NORMAL_ENEMY, 'B', EntityKind.BOSS,
                'H', EntityKind.LAVA_DORMANT, 'X', EntityKind.EXIT_LOCKED);
        int expectedCount = 0;
        for (int row = 0; row < RED_MOUNTAIN.size(); row++) {
            for (int col = 0; col < RED_MOUNTAIN.get(row).length(); col++) {
                char symbol = RED_MOUNTAIN.get(row).charAt(col);
                List<Entity> entities = world.entitiesAt(new Position(col, row));
                if (symbol == '.') {
                    assertTrue(entities.isEmpty(), "Floor at " + col + "," + row);
                } else {
                    expectedCount++;
                    assertEquals(1, entities.size(), "Tile at " + col + "," + row);
                    assertEquals(legend.get(symbol), entities.get(0).kind());
                }
            }
        }
        assertEquals(expectedCount, world.entities().size());
        assertEquals(14, world.width());
        assertEquals(10, world.height());
        assertEquals("red-mountain", world.mapName());
        assertEquals(new Position(1, 1), world.player().spawn());
        assertEquals(1, world.entities(Player.class).size());
        assertEquals(5, world.entities(NormalEnemy.class).size());
        assertEquals(1, count(world, EntityKind.BOSS));
        assertEquals(8, count(world, EntityKind.LAVA_DORMANT));
        assertEquals(8, world.map().lavaTiles().size());
        for (Entity lava : world.map().lavaTiles()) {
            assertSame(lava, world.entitiesAt(lava.position()).get(0));
        }
        assertInstanceOf(FloorTile.class, world.map().tileAt(world.player().spawn()));
        assertEquals(4, count(world, EntityKind.DESTRUCTIBLE_BLOCK));
        assertEquals(1, world.entities(Exit.class).size());
        assertEquals(0, world.score());
        assertEquals(300.0, world.timeLeft());
        assertEquals(GameState.PLAYING, world.state());
        assertSame(random, world.random());
    }

    @Test
    void supportsReinforcedBlocksAndCallerSuppliedTimerAndSpeed() {
        MapLoader configured = new MapLoader(MapFixtures.factory(6.5), 123.75);
        GameWorld world = configured.parse("Small fixture", List.of("PRDX"));
        assertEquals(4, world.width());
        assertEquals(1, world.height());
        assertEquals(6.5, world.player().speed());
        assertEquals(123.75, world.timeLeft());
        assertEquals(EntityKind.REINFORCED_BLOCK, world.entitiesAt(new Position(1, 0)).get(0).kind());
        assertTrue(world.isBlockedFor(world.player(), new Position(1, 0)));
        assertFalse(world.isBlockedFor(world.player(), new Position(3, 0)));
    }

    @Test
    void readsUtf8BomCrLfAndAFinalNewlineWithoutChangingCoordinates() throws IOException {
        Path file = temporary.resolve("level-one.txt");
        Files.writeString(file, "\uFEFFP.X\r\n###\r\n", StandardCharsets.UTF_8);
        GameWorld world = loader.load(file);
        assertEquals("level-one", world.mapName());
        assertEquals(3, world.width());
        assertEquals(2, world.height());
        assertEquals(new Position(0, 0), world.player().spawn());
        assertEquals(new Position(2, 0), world.entities(Exit.class).get(0).position());
    }

    @Test
    void readRowsOnlyDecodesTheFileAndDoesNotConstructOrValidateEntities() throws IOException {
        Path file = temporary.resolve("raw.txt");
        Files.writeString(file, "\uFEFF?\r\ninvalid\r\n", StandardCharsets.UTF_8);
        assertEquals(List.of("\uFEFF?", "invalid"), loader.readRows(file));
        assertThrows(InvalidMapException.class, () -> loader.load(file));
    }

    @Test
    void terrainDoesNotPreventRespawningOnTheNearestUnoccupiedFloor() {
        GameWorld world = loader.parse("Respawn", List.of("PX.", "..."));
        Player player = world.player();
        world.add(new DestructibleBlock(player.spawn()));
        player.takeDamage(new Damage(1, Damage.newActivationId(), DamageSource.ENEMY_CONTACT));
        new LifeSystem().update(world, 0.05);
        assertEquals(new Position(0, 1), player.position());
        assertEquals(2, player.lives());
        assertTrue(player.isInvulnerable());
        assertInstanceOf(FloorTile.class, world.map().tileAt(player.position()));
        assertEquals(List.of(player), world.entitiesAt(player.position()));
    }

    @Test
    void repeatedLoadsCreateFreshWorldsAndResetInitialState() throws IOException {
        Path file = temporary.resolve("restart.txt");
        Files.writeString(file, "PDEBHX\n", StandardCharsets.UTF_8);
        GameWorld first = loader.load(file, new Random(7));
        Set<Long> oldIds = first.entities().stream().map(Entity::id).collect(Collectors.toSet());
        first.player().moveTo(new Position(1, 0));
        first.addScore(500);
        first.reduceTime(200);
        first.entities(Exit.class).get(0).open();
        first.entities().forEach(Entity::deactivate);

        GameWorld second = loader.load(file, new Random(7));
        assertNotSame(first, second);
        assertNotSame(first.player(), second.player());
        assertNotSame(first.map(), second.map());
        assertNotSame(first.map().tileAt(new Position(0, 0)), second.map().tileAt(new Position(0, 0)));
        assertNotSame(first.bus(), second.bus());
        assertEquals(new Position(0, 0), second.player().position());
        assertEquals(6, second.entities().size());
        assertTrue(second.entities().stream().noneMatch(entity -> oldIds.contains(entity.id())));
        assertEquals(3, second.player().lives());
        assertEquals(0, second.player().activeBombs());
        assertEquals(0, second.score());
        assertEquals(300, second.timeLeft());
        assertFalse(second.entities(Exit.class).get(0).isOpen());
        assertEquals(first.random().nextLong(), second.random().nextLong());
    }

    @ParameterizedTest
    @MethodSource("invalidLayouts")
    void rejectsMalformedTextBeforeCallingAnyDomainConstructor(List<String> rows, List<String> details) {
        AtomicInteger calls = new AtomicInteger();
        Function<Position, Entity> creator = position -> {
            calls.incrementAndGet();
            throw new AssertionError("Invalid text must fail before entity construction");
        };
        MapLoader validating = new MapLoader(new EntityFactory(4, creator, creator, creator), 300);
        InvalidMapException error = assertThrows(InvalidMapException.class,
                () -> validating.parse("Invalid fixture", rows));
        assertTrue(error.getMessage().contains("Invalid fixture"));
        for (String detail : details) {
            assertTrue(error.getMessage().contains(detail), error.getMessage());
        }
        assertEquals(0, calls.get());
    }

    static Stream<Arguments> invalidLayouts() {
        return Stream.of(
                arguments(null, List.of("at least one row")),
                arguments(List.of(), List.of("at least one row")),
                arguments(List.of(""), List.of("nonempty first row")),
                arguments(List.of("\uFEFF"), List.of("nonempty first row")),
                arguments(List.of("\uFEFF\uFEFFP"), List.of("column 1", "U+FEFF")),
                arguments(Arrays.asList("P", null), List.of("row 2", "null")),
                arguments(List.of("P..", ".."), List.of("row 2", "width 2", "expected 3")),
                arguments(List.of("P..", ""), List.of("row 2", "width 0")),
                arguments(List.of("..."), List.of("exactly one P", "found none")),
                arguments(List.of("P.P"), List.of("row 1", "column 3", "exactly one P")),
                arguments(List.of("PEBH", "...Q"), List.of("row 2", "column 4", "'Q'")),
                arguments(List.of("P "), List.of("column 2", "' '")),
                arguments(List.of("P\t"), List.of("column 2", "U+0009")),
                arguments(List.of("P.", "\uFEFF."), List.of("row 2", "column 1", "U+FEFF")));
    }

    @Test
    void missingOrUnreadableFilesNameThePathAndPreserveTheIoCause() {
        Path missing = temporary.resolve("missing.txt");
        for (Path path : List.of(missing, temporary)) {
            InvalidMapException error = assertThrows(InvalidMapException.class, () -> loader.load(path));
            assertTrue(error.getMessage().contains("Cannot read map"));
            assertTrue(error.getMessage().contains(path.toString()));
            assertInstanceOf(IOException.class, error.getCause());
        }
    }

    @Test
    void rejectsInvalidUtf8RatherThanReplacingItsBytes() throws IOException {
        Path file = temporary.resolve("invalid-utf8.txt");
        Files.write(file, new byte[] {(byte) 0xC3, (byte) 0x28});
        InvalidMapException error = assertThrows(InvalidMapException.class, () -> loader.load(file));
        assertTrue(error.getMessage().contains(file.toString()));
        assertInstanceOf(IOException.class, error.getCause());
    }

    @Test
    void errorsInAFileIncludeTheFullPathRowAndColumn() throws IOException {
        Path file = temporary.resolve("bad-map.txt");
        Files.writeString(file, "P..\n.#?\n", StandardCharsets.UTF_8);
        InvalidMapException error = assertThrows(InvalidMapException.class, () -> loader.load(file));
        assertTrue(error.getMessage().contains(file.toString()));
        assertTrue(error.getMessage().contains("row 2, column 3"));
        assertTrue(error.getMessage().contains("'?'"));
    }

    @Test
    void domainConstructionFailuresIncludeTheMapNameAndCoordinates() {
        IllegalArgumentException cause = new IllegalArgumentException("No patrol path at this spawn");
        EntityFactory failing = new EntityFactory(4, position -> { throw cause; },
                position -> new MapFixtures.TestHostile(position, EntityKind.BOSS),
                MapFixtures.TestLava::new);
        InvalidMapException error = assertThrows(InvalidMapException.class,
                () -> new MapLoader(failing, 300).parse("Broken patrol", List.of("PE")));
        assertTrue(error.getMessage().contains("Broken patrol"));
        assertTrue(error.getMessage().contains("row 1, column 2"));
        assertTrue(error.getMessage().contains(cause.getMessage()));
        assertSame(cause, error.getCause().getCause());
    }

    @Test
    void rejectsAFactoryReusingOneEntityForMultipleSpawns() {
        NormalEnemy shared = MapFixtures.enemy(new Position(1, 0));
        EntityFactory reusing = new EntityFactory(4, position -> { shared.moveTo(position); return shared; },
                position -> new MapFixtures.TestHostile(position, EntityKind.BOSS),
                MapFixtures.TestLava::new);
        InvalidMapException error = assertThrows(InvalidMapException.class,
                () -> new MapLoader(reusing, 300).parse("Duplicate entity", List.of("PEE")));
        assertTrue(error.getMessage().contains("row 1, column 3"));
        assertTrue(error.getMessage().contains("fresh entity"));
    }

    @Test
    void usesACopyOfTheValidatedRowsDuringConstruction() {
        List<String> rows = new ArrayList<>(List.of("PEBHX"));
        EntityFactory changing = new EntityFactory(4, position -> {
            rows.set(0, "?");
            return MapFixtures.enemy(position);
        }, position -> new MapFixtures.TestHostile(position, EntityKind.BOSS),
                MapFixtures.TestLava::new);
        GameWorld world = new MapLoader(changing, 300).parse("Snapshot", rows);
        assertEquals(5, world.width());
        assertEquals(5, world.entities().size());
        assertEquals(EntityKind.EXIT_LOCKED, world.entitiesAt(new Position(4, 0)).get(0).kind());
    }

    @ParameterizedTest
    @ValueSource(doubles = {0, -1, Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY})
    void timeLimitMustBeFiniteAndPositive(double seconds) {
        assertTrue(assertThrows(IllegalArgumentException.class,
                () -> new MapLoader(MapFixtures.factory(4), seconds)).getMessage().contains("timeLimit"));
    }

    private static long count(GameWorld world, EntityKind kind) {
        return world.entities().stream().filter(entity -> entity.kind() == kind).count();
    }
}
