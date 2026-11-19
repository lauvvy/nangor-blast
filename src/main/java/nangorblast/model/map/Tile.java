package nangorblast.model.map;

import java.util.Objects;
import nangorblast.core.Position;
import nangorblast.model.Entity;

/** Stationary terrain beneath the blocks, actors, and items on a map. */
public abstract class Tile extends Entity {

    protected Tile(Position position) {
        super(position);
    }

    public boolean isWalkable() {
        return true;
    }

    @Override
    public boolean blocksMovementOf(Entity mover) {
        return !isWalkable();
    }

    /** A tile's position must continue to agree with its cell in GameMap's grid. */
    @Override
    public final void moveTo(Position target) {
        Objects.requireNonNull(target, "target");
        if (!position().equals(target)) {
            throw new IllegalArgumentException("Terrain tiles cannot move from " + position() + " to " + target);
        }
    }
}
