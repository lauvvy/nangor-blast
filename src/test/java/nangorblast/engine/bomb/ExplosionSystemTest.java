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
import nangorblast.core.Position;
import nangorblast.model.Damage;
import nangorblast.model.Damageable;
import nangorblast.model.Entity;
import nangorblast.model.GameWorld;
import nangorblast.model.Player;
import nangorblast.model.block.Block;
import nangorblast.model.block.DestructibleBlock;
import nangorblast.model.block.ReinforcedBlock;
import nangorblast.model.bomb.Bomb;
import nangorblast.model.bomb.NormalBomb;
import nangorblast.model.explosion.Explosion;
import org.junit.jupiter.api.Test;

class ExplosionSystemTest {

    private static final double DT = 0.05;

    /** Stand-in for a Normal Enemy: counts every takeDamage call. */
    static final class Target extends Entity implements Damageable {
        int calls;

        Target(Position position) {
            super(position);
        }

        @Override
        public EntityKind kind() {
            return EntityKind.NORMAL_ENEMY;
        }

        @Override
        public boolean takeDamage(Damage damage) {
            calls++;
            return true;
        }
    }

    /** Stand-in for a power-up: on the map but not Damageable. */
    static final class Pickup extends Entity {
        Pickup(Position position) {
            super(position);
        }

        @Override
        public EntityKind kind() {
            return EntityKind.POWER_UP_HEART;
        }
    }

    private final ExplosionSystem system = new ExplosionSystem();

    private static GameWorld world(String... rows) {
        return TestWorlds.fromAscii(Map.of(
                'D', DestructibleBlock::new, 'R', ReinforcedBlock::new,
                'T', Target::new, 'U', Pickup::new), rows);
    }

    /** Places a Normal Bomb of the player at {@code (col, row)} and makes it explode this tick. */
    private Bomb explode(GameWorld world, int col, int row) {
        return explode(world, new NormalBomb(new Position(col, row), world.player()));
    }

    private Bomb explode(GameWorld world, Bomb bomb) {
        world.add(bomb);
        world.player().bombPlaced();
        bomb.trigger();
        system.update(world, DT);
        return bomb;
    }

    private static Bomb pumped(Position position, Player owner) {
        return new Bomb(position, owner, owner.blastRange(), 4.5) {
            @Override
            public BombType type() {
                return BombType.PUMPED;
            }

            @Override
            public EntityKind kind() {
                return EntityKind.BOMB_PUMPED;
            }
        };
    }

    private static Set<Position> area(GameWorld world) {
        return world.entities(Explosion.class).stream().map(Entity::position).collect(Collectors.toSet());
    }

    private static Position at(int col, int row) {
        return new Position(col, row);
    }

    @Test
    void coversTheCenterAndFourArmsOfBlastRange() {
        GameWorld world = world(
                "P....",
                ".....",
                ".....",
                ".....",
                ".....");
        world.player().increaseBlastRange(); // range 2
        Bomb bomb = explode(world, 2, 2);

        assertFalse(bomb.isActive());
        assertEquals(Set.of(at(2, 2), at(2, 1), at(2, 0), at(2, 3), at(2, 4),
                at(1, 2), at(0, 2), at(3, 2), at(4, 2)), area(world));
    }

    @Test
    void armsStopAtTheMapEdge() {
        GameWorld world = world("P..", "...");
        world.player().increaseBlastRange();
        explode(world, 0, 1);

        assertEquals(Set.of(at(0, 1), at(0, 0), at(1, 1), at(2, 1)), area(world));
    }

    @Test
    void lastsHalfASecond() {
        GameWorld world = world("P..");
        explode(world, 2, 0);

        for (int i = 0; i < 9; i++) {
            system.update(world, DT);
        }
        assertFalse(area(world).isEmpty()); // 0.45 s
        system.update(world, DT);
        assertTrue(area(world).isEmpty());  // 0.50 s
    }

    @Test
    void indestructibleBlockStopsTheArmAndIsNotHit() {
        GameWorld world = world("P.#.");
        world.player().increaseBlastRange();
        explode(world, 1, 0);

        assertEquals(Set.of(at(1, 0), at(0, 0)), area(world));
        assertTrue(world.firstAt(at(2, 0), Block.class).isPresent());
    }

    @Test
    void destructibleBlockIsDestroyedStopsTheArmAndBecomesWalkable() {
        GameWorld world = world("P.DT");
        world.player().increaseBlastRange();
        explode(world, 1, 0);
        world.removeInactive();

        assertEquals(Set.of(at(1, 0), at(0, 0)), area(world));
        assertTrue(world.firstAt(at(2, 0), Block.class).isEmpty());
        assertEquals(0, world.firstAt(at(3, 0), Target.class).orElseThrow().calls);
        assertFalse(world.isBlockedFor(world.player(), at(2, 0)));
    }

    @Test
    void reinforcedBlockStopsANormalBombAndSurvives() {
        GameWorld world = world("P.R.");
        world.player().increaseBlastRange();
        explode(world, 1, 0);

        assertEquals(Set.of(at(1, 0), at(0, 0)), area(world));
        assertTrue(world.firstAt(at(2, 0), Block.class).isPresent());
    }

    @Test
    void reinforcedBlockIsDestroyedByAPumpedBombButStillStopsTheArm() {
        GameWorld world = world("P.R.");
        world.player().increaseBlastRange();
        explode(world, pumped(at(1, 0), world.player()));

        assertEquals(Set.of(at(1, 0), at(0, 0)), area(world));
        assertTrue(world.firstAt(at(2, 0), Block.class).isEmpty());
    }

    @Test
    void hurtsEachTargetOnceAndDoesNotTouchPowerUps() {
        GameWorld world = world("P.TU");
        world.player().increaseBlastRange();
        explode(world, 1, 0);
        for (int i = 0; i < 9; i++) {
            system.update(world, DT);
        }

        assertEquals(1, world.firstAt(at(2, 0), Target.class).orElseThrow().calls);
        assertTrue(world.firstAt(at(3, 0), Pickup.class).orElseThrow().isActive());
    }

    @Test
    void ownerIsNotImmune() {
        GameWorld world = world("P..");
        explode(world, 1, 0);

        assertEquals(Player.START_LIVES - 1, world.player().lives());
    }

    @Test
    void invulnerablePlayerIsNotHurt() {
        GameWorld world = world("P..");
        world.player().makeInvulnerable(1.0);
        explode(world, 1, 0);

        assertEquals(Player.START_LIVES, world.player().lives());
    }

    @Test
    void capacityReturnsWhenTheExplosionEnds() {
        GameWorld world = world("P..");
        explode(world, 2, 0);
        for (int i = 0; i < 9; i++) {
            system.update(world, DT);
        }
        assertEquals(1, world.player().activeBombs()); // explosion still burning

        system.update(world, DT);
        assertEquals(0, world.player().activeBombs());
    }

    @Test
    void anotherBombInTheAreaIsTriggered() {
        GameWorld world = world("P...");
        world.player().increaseBombCapacity();
        Bomb second = new NormalBomb(at(3, 0), world.player());
        world.add(second);
        world.player().bombPlaced();
        explode(world, 2, 0);

        assertTrue(second.isDue());
        system.update(world, DT);
        assertFalse(second.isActive());
    }
}
