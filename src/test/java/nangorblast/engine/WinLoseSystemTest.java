package nangorblast.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;

import nangorblast.TestWorlds;
import nangorblast.core.GameState;
import nangorblast.model.Damage;
import nangorblast.model.DamageSource;
import nangorblast.model.GameWorld;
import org.junit.jupiter.api.Test;

class WinLoseSystemTest {

    private final WinLoseSystem system = new WinLoseSystem();

    @Test
    void loseWhenTheTimerReachesZero() {
        GameWorld world = TestWorlds.fromAscii("P.");
        world.reduceTime(299.99);
        system.update(world, 0.05);
        assertEquals(GameState.PLAYING, world.state());

        world.reduceTime(0.05);
        system.update(world, 0.05);
        assertEquals(GameState.LOSE, world.state());
    }

    @Test
    void loseWhenThePlayerHasNoLives() {
        GameWorld world = TestWorlds.fromAscii("P.");
        for (int i = 0; i < 3; i++) {
            world.player().takeDamage(new Damage(1, Damage.newActivationId(), DamageSource.HAZARD));
        }
        system.update(world, 0.05);
        assertEquals(GameState.LOSE, world.state());
    }

    @Test
    void doesNothingOutsidePlaying() {
        GameWorld world = TestWorlds.fromAscii("P.");
        world.reduceTime(300);
        world.setState(GameState.MENU);
        system.update(world, 0.05);
        assertEquals(GameState.MENU, world.state());
    }
}
