package nangorblast.engine.bomb;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import nangorblast.TestWorlds;
import nangorblast.core.BombType;
import nangorblast.core.GameState;
import nangorblast.core.Position;
import nangorblast.engine.PlayerSystem;
import nangorblast.input.Action;
import nangorblast.input.Command;
import nangorblast.model.GameWorld;
import nangorblast.model.Player;
import nangorblast.model.bomb.Bomb;
import org.junit.jupiter.api.Test;

class BombSystemTest {

    private static final double DT = 0.05;

    private final BombSystem system = new BombSystem();

    private void place(GameWorld world) {
        system.handle(world, new Command(0, Action.PLACE_BOMB));
    }

    private void ticks(GameWorld world, int count) {
        for (int i = 0; i < count; i++) {
            system.update(world, DT);
        }
    }

    @Test
    void placesANormalBombOnThePlayersTileAndUsesOneCapacity() {
        GameWorld world = TestWorlds.fromAscii("P..");
        place(world);

        List<Bomb> bombs = world.entities(Bomb.class);
        assertEquals(1, bombs.size());
        assertEquals(BombType.NORMAL, bombs.get(0).type());
        assertEquals(new Position(0, 0), bombs.get(0).position());
        assertEquals(world.player(), bombs.get(0).owner());
        assertEquals(1, world.player().activeBombs());
    }

    @Test
    void normalBombIsDueAfterThreeSeconds() {
        GameWorld world = TestWorlds.fromAscii("P..");
        place(world);
        Bomb bomb = world.entities(Bomb.class).get(0);

        ticks(world, 59); // 2.95 s
        assertFalse(bomb.isDue());
        ticks(world, 1);  // 3.00 s
        assertTrue(bomb.isDue());
    }

    @Test
    void placementRefusedAtFullCapacity() {
        GameWorld world = TestWorlds.fromAscii("P..");
        place(world);
        world.player().moveTo(new Position(1, 0));
        place(world);

        assertEquals(1, world.entities(Bomb.class).size());
        assertEquals(1, world.player().activeBombs());
    }

    @Test
    void placementRefusedOnATileWithAnActiveBomb() {
        GameWorld world = TestWorlds.fromAscii("P..");
        world.player().increaseBombCapacity();
        place(world);
        place(world);

        assertEquals(1, world.entities(Bomb.class).size());
        assertEquals(1, world.player().activeBombs());
    }

    @Test
    void blastRangeIsFixedWhenPlaced() {
        GameWorld world = TestWorlds.fromAscii("P..");
        place(world);
        world.player().increaseBlastRange();

        assertEquals(Player.START_BLAST_RANGE, world.entities(Bomb.class).get(0).blastRange());
    }

    @Test
    void placementIgnoredUnlessPlaying() {
        GameWorld world = TestWorlds.fromAscii("P..");
        world.setState(GameState.PAUSED);
        place(world);
        system.handle(world, new Command(1, Action.PLACE_BOMB)); // no second player yet
        world.setState(GameState.PLAYING);
        system.handle(world, new Command(1, Action.PLACE_BOMB));

        assertTrue(world.entities(Bomb.class).isEmpty());
    }

    @Test
    void playerCanLeaveTheBombTileButNotReEnterIt() {
        GameWorld world = TestWorlds.fromAscii("P..");
        PlayerSystem players = new PlayerSystem();
        place(world);

        players.handle(world, new Command(0, Action.MOVE_RIGHT));
        assertEquals(new Position(1, 0), world.player().position());

        players.update(world, 1.0); // let the step cooldown pass
        players.handle(world, new Command(0, Action.MOVE_LEFT));
        assertEquals(new Position(1, 0), world.player().position());
    }
}
