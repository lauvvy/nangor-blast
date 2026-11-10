package nangorblast.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;

import nangorblast.TestWorlds;
import nangorblast.model.GameWorld;
import org.junit.jupiter.api.Test;

/** The sample for every member: a GameSystem tested by calling update directly, with no game loop. */
class TimerSystemTest {

    @Test
    void reducesTimerByDtEachTick() {
        GameWorld world = TestWorlds.fromAscii("P");
        GameSystem timer = new TimerSystem();

        for (int i = 0; i < 20; i++) {
            timer.update(world, 0.05);
        }

        assertEquals(299.0, world.timeLeft(), 1e-9);
    }
}
