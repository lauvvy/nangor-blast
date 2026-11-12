package nangorblast.model.block;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import nangorblast.TestWorlds;
import nangorblast.core.BombType;
import nangorblast.core.EntityKind;
import nangorblast.core.Position;
import nangorblast.engine.PlayerSystem;
import nangorblast.engine.bomb.BombSystem;
import nangorblast.input.Action;
import nangorblast.input.Command;
import nangorblast.model.GameWorld;
import nangorblast.model.bomb.Bomb;
import org.junit.jupiter.api.Test;

class BlockTest {

    private static final Position EAST = new Position(1, 0);

    private static GameWorld world(String... rows) {
        return TestWorlds.fromAscii(Map.of(
                'D', DestructibleBlock::new, 'R', ReinforcedBlock::new), rows);
    }

    private static Block blockAt(GameWorld world, Position position) {
        return world.firstAt(position, Block.class).orElseThrow();
    }

    @Test
    void everyTypeBlocksThePlayer() {
        for (String row : new String[] {"P#", "PD", "PR"}) {
            GameWorld world = world(row);
            assertTrue(world.isBlockedFor(world.player(), EAST), row);
        }
    }

    @Test
    void everyTypeBlocksEveryMover() {
        GameWorld world = world("P#DR");
        Block[] blocks = world.entities(Block.class).toArray(Block[]::new);
        assertEquals(3, blocks.length);
        for (Block block : blocks) {
            assertTrue(block.blocksMovementOf(blocks[0]), block.kind().name()); // any entity, not only the player
        }
    }

    @Test
    void indestructibleIsNeverDestroyed() {
        Block block = new IndestructibleBlock(EAST);
        for (BombType type : BombType.values()) {
            assertFalse(block.isDestroyedBy(type), type.name());
        }
    }

    @Test
    void destructibleIsDestroyedByEveryBomb() {
        Block block = new DestructibleBlock(EAST);
        for (BombType type : BombType.values()) {
            assertTrue(block.isDestroyedBy(type), type.name());
        }
    }

    @Test
    void reinforcedIsDestroyedOnlyByPumpedBomb() {
        Block block = new ReinforcedBlock(EAST);
        assertFalse(block.isDestroyedBy(BombType.NORMAL));
        assertFalse(block.isDestroyedBy(BombType.REMOTE));
        assertTrue(block.isDestroyedBy(BombType.PUMPED));
    }

    @Test
    void destroyedBlockTileBecomesWalkable() {
        for (String row : new String[] {"PD", "PR"}) {
            GameWorld world = world(row);
            blockAt(world, EAST).deactivate();
            world.removeInactive();

            assertFalse(world.isBlockedFor(world.player(), EAST), row);
            new PlayerSystem().handle(world, new Command(0, Action.MOVE_RIGHT));
            assertEquals(EAST, world.player().position(), row);
        }
    }

    @Test
    void indestructibleTileCannotHoldABomb() {
        GameWorld world = world("P#");
        new PlayerSystem().handle(world, new Command(0, Action.MOVE_RIGHT));
        new BombSystem().handle(world, new Command(0, Action.PLACE_BOMB));

        assertTrue(world.firstAt(EAST, Bomb.class).isEmpty());
    }

    @Test
    void eachTypeReportsItsKind() {
        assertEquals(EntityKind.INDESTRUCTIBLE_BLOCK, new IndestructibleBlock(EAST).kind());
        assertEquals(EntityKind.DESTRUCTIBLE_BLOCK, new DestructibleBlock(EAST).kind());
        assertEquals(EntityKind.REINFORCED_BLOCK, new ReinforcedBlock(EAST).kind());
    }
}
