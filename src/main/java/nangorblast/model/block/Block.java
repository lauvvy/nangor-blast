package nangorblast.model.block;

import nangorblast.core.BombType;
import nangorblast.core.Position;
import nangorblast.model.Entity;

/**
 * A map object that blocks movement and stops explosions (F04). Attributes from F04.1:
 * position and active. A destroyed block is deactivated, and its tile becomes walkable
 * once the loop removes it.
 */
public abstract class Block extends Entity {

    protected Block(Position position) {
        super(position);
    }

    /** Every block stops players and enemies while it is on the map. */
    @Override
    public final boolean blocksMovementOf(Entity mover) {
        return true;
    }

    /**
     * Whether an explosion from a bomb of {@code type} destroys this block. Either way the
     * explosion arm stops at this tile (F03.2).
     */
    public abstract boolean isDestroyedBy(BombType type);
}
