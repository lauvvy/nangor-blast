package nangorblast.engine.bomb;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import nangorblast.TestWorlds;
import nangorblast.app.GameFactory;
import nangorblast.core.BombType;
import nangorblast.core.GameState;
import nangorblast.core.Position;
import nangorblast.engine.PlayerSystem;
import nangorblast.input.Action;
import nangorblast.input.Command;
import nangorblast.model.GameWorld;
import nangorblast.model.Player;
import nangorblast.model.bomb.Bomb;
import nangorblast.model.bomb.PumpedBomb;
import nangorblast.model.bomb.RemoteBomb;
import nangorblast.model.explosion.Explosion;
import org.junit.jupiter.api.Test;

class BombRevisionTest {
    private final PlayerSystem players = new PlayerSystem();
    private final BombSystem bombs = new BombSystem();

    private void command(GameWorld world, Action action) {
        players.handle(world, new Command(0, action));
        bombs.handle(world, new Command(0, action));
    }

    @Test
    void productionSystemsActuallyExplodeBombs() {
        GameWorld world = TestWorlds.fromAscii("P....");
        command(world, Action.SELECT_PUMPED);
        command(world, Action.PLACE_BOMB);
        world.player().moveTo(new Position(4, 0));
        var systems = GameFactory.systems(players);
        for (int tick = 0; tick < 90; tick++) {
            for (var system : systems) {
                system.update(world, 0.05);
            }
            world.removeInactive();
        }
        assertTrue(world.entities(Bomb.class).isEmpty());
        assertEquals(1, world.entities(Explosion.class).stream().filter(Explosion::isCenter).count());
    }

    @Test
    void lockedRemoteSelectionDoesNotStopPlay() {
        GameWorld world = TestWorlds.fromAscii("P..");
        assertDoesNotThrow(() -> command(world, Action.SELECT_REMOTE));
        assertEquals(BombType.NORMAL, world.player().selectedBomb());
        command(world, Action.SELECT_PUMPED);
        command(world, Action.PLACE_BOMB);
        assertInstanceOf(PumpedBomb.class, world.entities(Bomb.class).get(0));
        command(world, Action.SELECT_NORMAL);
        assertEquals(BombType.PUMPED, world.entities(Bomb.class).get(0).type());
    }

    @Test
    void pumpedFuseRangeAndDamageFollowRevision() {
        GameWorld world = TestWorlds.fromAscii("P..");
        PumpedBomb bomb = new PumpedBomb(world.player().position(), world.player());
        assertEquals(3, bomb.blastRange());
        assertEquals(2, bomb.damage());
        bomb.tickFuse(4.45);
        assertFalse(bomb.isDue());
        bomb.tickFuse(0.05);
        assertTrue(bomb.isDue());
        for (int i = 0; i < 10; i++) {
            world.player().increaseBlastRange();
        }
        assertEquals(3, bomb.blastRange());
        assertEquals(7, new PumpedBomb(world.player().position(), world.player()).blastRange());
    }

    @Test
    void remoteDetonatesAllOwnedBombsOnlyAndPauseIgnoresCommands() {
        GameWorld world = TestWorlds.fromAscii("P....");
        world.player().unlockRemote();
        world.player().increaseBombCapacity();
        command(world, Action.SELECT_REMOTE);
        command(world, Action.PLACE_BOMB);
        world.player().moveTo(new Position(2, 0));
        command(world, Action.PLACE_BOMB);
        RemoteBomb foreign = new RemoteBomb(new Position(4, 0),
                new Player(new Position(4, 0), 1, 4));
        world.add(foreign);
        bombs.update(world, 1000);
        assertTrue(world.entities(Bomb.class).stream().noneMatch(Bomb::isDue));
        world.setState(GameState.PAUSED);
        command(world, Action.DETONATE);
        command(world, Action.SELECT_NORMAL);
        assertEquals(BombType.REMOTE, world.player().selectedBomb());
        assertTrue(world.entities(Bomb.class).stream().noneMatch(Bomb::isDue));
        world.setState(GameState.PLAYING);
        command(world, Action.DETONATE);
        assertEquals(2, world.entities(Bomb.class).stream().filter(Bomb::isDue).count());
        assertFalse(foreign.isDue());
        assertEquals(2, world.player().activeBombs());
    }

    @Test
    void chainReactionCompletesOnceInSameTickAndRestoresCapacityAfterFlames() {
        GameWorld world = TestWorlds.fromAscii("P....");
        world.player().increaseBombCapacity();
        RemoteBomb remote = new RemoteBomb(new Position(2, 0), world.player());
        PumpedBomb pumped = new PumpedBomb(new Position(1, 0), world.player());
        // Put the target before the source to test insertion-order independence.
        world.add(remote);
        world.add(pumped);
        world.player().bombPlaced();
        world.player().bombPlaced();
        pumped.trigger();
        ExplosionSystem explosions = new ExplosionSystem();
        explosions.update(world, 0.05);
        assertTrue(world.entities(Bomb.class).isEmpty());
        assertEquals(2, world.entities(Explosion.class).stream().filter(Explosion::isCenter).count());
        assertEquals(2, world.player().activeBombs());
        explosions.update(world, 0.5);
        assertEquals(0, world.player().activeBombs());
        assertTrue(world.entities(Explosion.class).isEmpty());
    }
}
