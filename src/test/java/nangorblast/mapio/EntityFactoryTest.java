package nangorblast.mapio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import nangorblast.core.EntityKind;
import nangorblast.core.InvalidMapException;
import nangorblast.core.Position;
import nangorblast.model.Entity;
import nangorblast.model.Exit;
import nangorblast.model.Player;
import nangorblast.model.block.DestructibleBlock;
import nangorblast.model.block.IndestructibleBlock;
import nangorblast.model.block.ReinforcedBlock;
import nangorblast.model.enemy.NormalEnemy;
import nangorblast.model.enemy.PatrolBehavior;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class EntityFactoryTest {

    private final Position at = new Position(3, 2);
    private final EntityFactory factory = MapFixtures.factory(4.0);

    @Test
    void createsProductionBlocksPlayerAndLockedExitAtTheRequestedTile() {
        Player player = assertInstanceOf(Player.class, factory.create('P', at).orElseThrow());
        IndestructibleBlock wall = assertInstanceOf(IndestructibleBlock.class, factory.create('#', at).orElseThrow());
        DestructibleBlock block = assertInstanceOf(DestructibleBlock.class, factory.create('D', at).orElseThrow());
        ReinforcedBlock reinforced = assertInstanceOf(ReinforcedBlock.class, factory.create('R', at).orElseThrow());
        Exit exit = assertInstanceOf(Exit.class, factory.create('X', at).orElseThrow());
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
    void floorCreatesNoEntityAndDoesNotCallDomainFactories() {
        AtomicInteger calls = new AtomicInteger();
        Function<Position, Entity> creator = position -> {
            calls.incrementAndGet();
            throw new AssertionError("Floor must not create an entity");
        };
        EntityFactory floorFactory = new EntityFactory(4, creator, creator, creator);
        assertTrue(floorFactory.create('.', at).isEmpty());
        assertEquals(0, calls.get());
    }

    @Test
    void delegatesEnemyBossAndLavaToConfiguredConstructors() {
        PatrolBehavior behavior = new PatrolBehavior(List.of(at, new Position(4, 2)));
        NormalEnemy enemy = new NormalEnemy(at, behavior);
        Entity boss = new MapFixtures.TestHostile(at, EntityKind.BOSS);
        Entity lava = new MapFixtures.TestEntity(at, EntityKind.LAVA_DORMANT);
        EntityFactory configured = new EntityFactory(6.5,
                position -> { assertEquals(at, position); return enemy; },
                position -> { assertEquals(at, position); return boss; },
                position -> { assertEquals(at, position); return lava; });

        assertSame(enemy, configured.create('E', at).orElseThrow());
        assertSame(behavior, enemy.getBehavior());
        assertSame(boss, configured.create('B', at).orElseThrow());
        assertSame(lava, configured.create('H', at).orElseThrow());
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

    @ParameterizedTest
    @ValueSource(doubles = {0, -1, Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY})
    void playerSpeedMustBeFiniteAndPositive(double speed) {
        assertTrue(assertThrows(IllegalArgumentException.class, () -> MapFixtures.factory(speed))
                .getMessage().contains("playerSpeed"));
    }

    private static EntityFactory withEnemy(Function<Position, ? extends Entity> creator) {
        return new EntityFactory(4, creator,
                position -> new MapFixtures.TestHostile(position, EntityKind.BOSS),
                position -> new MapFixtures.TestEntity(position, EntityKind.LAVA_DORMANT));
    }
}
