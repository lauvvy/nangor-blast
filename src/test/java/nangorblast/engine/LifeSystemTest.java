package nangorblast.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import nangorblast.TestWorlds;
import nangorblast.core.EntityKind;
import nangorblast.core.GameState;
import nangorblast.core.PlayerDamaged;
import nangorblast.core.Position;
import nangorblast.model.Damage;
import nangorblast.model.DamageSource;
import nangorblast.model.Entity;
import nangorblast.model.GameWorld;
import nangorblast.model.Player;
import org.junit.jupiter.api.Test;

class LifeSystemTest {

    private static final double DT = 0.05;

    /** Stand-in for anything that stands on a tile without blocking, such as a Lava Tile. */
    static final class Puddle extends Entity {
        Puddle(Position position) {
            super(position);
        }

        @Override
        public EntityKind kind() {
            return EntityKind.LAVA_DORMANT;
        }
    }

    private final LifeSystem system = new LifeSystem();
    private final List<PlayerDamaged> damaged = new ArrayList<>();

    private GameWorld world(String... rows) {
        GameWorld world = TestWorlds.fromAscii(Map.of(
                'E', PlayerSystemTest.Foe::new, 'L', Puddle::new), rows);
        world.bus().subscribe(PlayerDamaged.class, damaged::add);
        return world;
    }

    private static Damage explosion() {
        return new Damage(1, Damage.newActivationId(), DamageSource.EXPLOSION);
    }

    @Test
    void aLostLifeRespawnsThePlayerAtTheSpawnPointAndPublishesOnce() {
        GameWorld world = world("P....");
        Player player = world.player();
        player.moveTo(new Position(4, 0));

        player.takeDamage(explosion());
        system.update(world, DT);

        assertEquals(new Position(0, 0), player.position());
        assertEquals(2, player.lives());
        assertEquals(List.of(new PlayerDamaged(0, 2)), damaged);
        system.update(world, DT);
        assertEquals(1, damaged.size());
    }

    @Test
    void oneActivationAndOneMomentCostOneLife() {
        GameWorld world = world("P..");
        Damage blast = explosion();
        world.player().takeDamage(blast);
        world.player().takeDamage(blast);          // the same explosion next tick
        world.player().takeDamage(explosion());    // another bomb in the same tick
        system.update(world, DT);
        assertEquals(2, world.player().lives());
    }

    @Test
    void invulnerabilityLastsExactly15SecondsAfterRespawn() {
        GameWorld world = world("P..");
        Player player = world.player();
        player.takeDamage(explosion());
        system.update(world, DT);                   // respawn tick: a full 1.5 s starts

        for (int i = 0; i < 29; i++) {
            system.update(world, DT);
        }
        assertTrue(player.isInvulnerable(), "1.45 s after the respawn");
        assertFalse(player.takeDamage(explosion()));

        system.update(world, DT);
        assertFalse(player.isInvulnerable(), "1.5 s after the respawn");
        assertTrue(player.takeDamage(explosion()));
    }

    @Test
    void anOccupiedSpawnPointSendsThePlayerToTheNearestFreeTile() {
        GameWorld world = world(
                "#####",
                "#P..#",
                "#####");
        Player player = world.player();
        world.add(new PlayerSystemTest.Foe(new Position(1, 1))); // an enemy walked onto the spawn
        player.moveTo(new Position(3, 1));

        player.takeDamage(explosion());
        system.update(world, DT);

        assertEquals(new Position(2, 1), player.position(), "first free tile in walking distance");
    }

    @Test
    void neverRespawnsOnTilesHoldingAnEntity() {
        GameWorld world = world(
                "#####",
                "#PL.#",
                "#####");
        world.add(new PlayerSystemTest.Foe(new Position(1, 1)));
        world.player().moveTo(new Position(3, 1));

        world.player().takeDamage(explosion());
        system.update(world, DT);

        assertEquals(new Position(3, 1), world.player().position(), "skips the lava stand-in at (2, 1)");
    }

    @Test
    void noRespawnAtZeroLivesAndWinLoseEndsTheGame() {
        GameWorld world = world("P....");
        Player player = world.player();
        player.moveTo(new Position(4, 0));
        for (int i = 0; i < 3; i++) {
            player.takeDamage(explosion());
            system.update(world, DT);
            player.moveTo(new Position(4, 0));
            for (int t = 0; t < 30; t++) {
                system.update(world, DT);
            }
        }
        assertEquals(0, player.lives());
        assertEquals(new Position(4, 0), player.position(), "the last hit leaves the player where it fell");
        assertEquals(List.of(new PlayerDamaged(0, 2), new PlayerDamaged(0, 1), new PlayerDamaged(0, 0)), damaged);

        new WinLoseSystem().update(world, DT);
        assertEquals(GameState.LOSE, world.state());
    }
}
