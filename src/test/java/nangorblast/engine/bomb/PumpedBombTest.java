package nangorblast.engine.bomb;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import nangorblast.TestWorlds;
import nangorblast.core.BombType;
import nangorblast.core.EntityKind;
import nangorblast.core.GameState;
import nangorblast.core.Position;
import nangorblast.engine.PlayerSystem;
import nangorblast.input.Action;
import nangorblast.input.Command;
import nangorblast.model.Damage;
import nangorblast.model.Damageable;
import nangorblast.model.Entity;
import nangorblast.model.GameWorld;
import nangorblast.model.block.Block;
import nangorblast.model.block.ReinforcedBlock;
import nangorblast.model.bomb.Bomb;
import nangorblast.model.explosion.Explosion;
import org.junit.jupiter.api.Test;

/** B-04: Pumped Bomb (F02.2.2) and bomb type selection (F02.3.2) through the real systems. */
class PumpedBombTest {

    private static final double DT = 0.05;

    /** Stand-in for an enemy: remembers the last damage amount. */
    static final class Target extends Entity implements Damageable {
        int lastAmount;

        Target(Position position) {
            super(position);
        }

        @Override
        public EntityKind kind() {
            return EntityKind.NORMAL_ENEMY;
        }

        @Override
        public boolean takeDamage(Damage damage) {
            lastAmount = damage.amount();
            return true;
        }
    }

    private final PlayerSystem players = new PlayerSystem();
    private final BombSystem bombs = new BombSystem();
    private final ExplosionSystem explosions = new ExplosionSystem();

    private static GameWorld world(String... rows) {
        return TestWorlds.fromAscii(Map.of('R', ReinforcedBlock::new, 'T', Target::new), rows);
    }

    private void command(GameWorld world, Action action) {
        players.handle(world, new Command(0, action));
        bombs.handle(world, new Command(0, action));
    }

    private void ticks(GameWorld world, int count) {
        for (int i = 0; i < count; i++) {
            bombs.update(world, DT);
            explosions.update(world, DT);
        }
    }

    /** Selects the Pumped Bomb, places it, and steps the player out of the blast. */
    private Bomb placePumped(GameWorld world, Position escape) {
        command(world, Action.SELECT_PUMPED);
        command(world, Action.PLACE_BOMB);
        world.player().moveTo(escape);
        return world.entities(Bomb.class).get(0);
    }

    private static Set<Position> area(GameWorld world) {
        return world.entities(Explosion.class).stream().map(Entity::position).collect(Collectors.toSet());
    }

    @Test
    void explodesFourAndAHalfSecondsAfterPlacement() {
        GameWorld world = world("P........", ".........");
        Bomb bomb = placePumped(world, new Position(0, 1));

        ticks(world, 89); // 4.45 s
        assertTrue(bomb.isActive());
        assertTrue(world.entities(Explosion.class).isEmpty());
        ticks(world, 1);  // 4.50 s
        assertFalse(bomb.isActive());
        assertFalse(world.entities(Explosion.class).isEmpty());
    }

    @Test
    void reachesBlastRangePlusTwo() {
        GameWorld world = world(".....P.....", "...........");
        placePumped(world, new Position(0, 1));
        ticks(world, 90);

        // Starting blast range 1, so the arms reach 3 tiles.
        assertTrue(area(world).contains(new Position(2, 0)));
        assertTrue(area(world).contains(new Position(8, 0)));
        assertFalse(area(world).contains(new Position(1, 0)));
        assertFalse(area(world).contains(new Position(9, 0)));
    }

    @Test
    void rangeIsCappedAtSeven() {
        GameWorld world = world("P....");
        for (int i = 0; i < 10; i++) {
            world.player().increaseBlastRange();
        }
        command(world, Action.SELECT_PUMPED);
        command(world, Action.PLACE_BOMB);

        assertEquals(7, world.entities(Bomb.class).get(0).blastRange());
    }

    @Test
    void destroysAReinforcedBlock() {
        GameWorld world = world("P.R", "...");
        placePumped(world, new Position(0, 1));
        ticks(world, 90);

        assertTrue(world.firstAt(new Position(2, 0), Block.class).isEmpty());
    }

    @Test
    void dealsTwoDamage() {
        GameWorld world = world("P.T", "...");
        placePumped(world, new Position(0, 1));
        ticks(world, 90);

        assertEquals(2, world.firstAt(new Position(2, 0), Target.class).orElseThrow().lastAmount);
    }

    @Test
    void selectionChangesOnlyLaterBombs() {
        GameWorld world = world("P..");
        world.player().increaseBombCapacity();
        command(world, Action.SELECT_PUMPED);
        command(world, Action.PLACE_BOMB);
        command(world, Action.SELECT_NORMAL);
        world.player().moveTo(new Position(1, 0));
        command(world, Action.PLACE_BOMB);

        assertEquals(BombType.PUMPED, world.firstAt(new Position(0, 0), Bomb.class).orElseThrow().type());
        assertEquals(BombType.NORMAL, world.firstAt(new Position(1, 0), Bomb.class).orElseThrow().type());
    }

    @Test
    void selectionIgnoredUnlessPlaying() {
        GameWorld world = world("P..");
        world.setState(GameState.PAUSED);
        command(world, Action.SELECT_PUMPED);

        assertEquals(BombType.NORMAL, world.player().selectedBomb());
    }
}
