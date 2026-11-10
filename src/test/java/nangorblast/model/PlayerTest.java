package nangorblast.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import nangorblast.core.BombType;
import nangorblast.core.InvalidActionException;
import nangorblast.core.Position;
import org.junit.jupiter.api.Test;

class PlayerTest {

    private final Player player = new Player(new Position(1, 1), 0, 4.0);

    private static Damage hit() {
        return new Damage(1, Damage.newActivationId(), DamageSource.EXPLOSION);
    }

    @Test
    void startsWithSpecificationValues() {
        assertEquals(3, player.lives());
        assertEquals(1, player.bombCapacity());
        assertEquals(1, player.blastRange());
        assertEquals(BombType.NORMAL, player.selectedBomb());
        assertFalse(player.isRemoteUnlocked());
    }

    @Test
    void powerUpsStopAtTheirMaximum() {
        for (int i = 0; i < 10; i++) {
            player.addLife();
            player.increaseBombCapacity();
            player.increaseBlastRange();
            player.increaseSpeed();
        }
        assertEquals(5, player.lives());
        assertEquals(3, player.bombCapacity());
        assertEquals(5, player.blastRange());
        assertEquals(8.0, player.speed(), 1e-9);
    }

    @Test
    void speedUpAddsHalfOfBaseSpeed() {
        player.increaseSpeed();
        assertEquals(6.0, player.speed(), 1e-9);
    }

    @Test
    void oneActivationHurtsOnce() {
        Damage damage = hit();
        assertTrue(player.takeDamage(damage));
        assertFalse(player.takeDamage(damage));
        assertEquals(2, player.lives());
    }

    @Test
    void invulnerablePlayerTakesNoDamage() {
        player.makeInvulnerable(1.5);
        assertFalse(player.takeDamage(hit()));

        player.tickInvulnerability(1.5);
        assertTrue(player.takeDamage(hit()));
    }

    @Test
    void livesNeverGoBelowZero() {
        for (int i = 0; i < 5; i++) {
            player.takeDamage(hit());
        }
        assertEquals(0, player.lives());
        assertFalse(player.isAlive());
    }

    @Test
    void bombCapacityLimitsActiveBombs() {
        player.bombPlaced();
        assertFalse(player.canPlaceBomb());
        assertThrows(InvalidActionException.class, player::bombPlaced);

        player.bombFinished();
        assertTrue(player.canPlaceBomb());
    }

    @Test
    void remoteBombNeedsDetonator() {
        assertThrows(InvalidActionException.class, () -> player.selectBomb(BombType.REMOTE));

        player.unlockRemote();
        player.selectBomb(BombType.REMOTE);
        assertEquals(BombType.REMOTE, player.selectedBomb());
    }
}
