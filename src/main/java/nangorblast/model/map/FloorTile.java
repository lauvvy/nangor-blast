package nangorblast.model.map;

import nangorblast.core.EntityKind;
import nangorblast.core.Position;

/** Ordinary walkable terrain, also present beneath a block or an actor. */
public final class FloorTile extends Tile {

    public FloorTile(Position position) {
        super(position);
    }

    @Override
    public EntityKind kind() {
        return EntityKind.FLOOR;
    }
}
